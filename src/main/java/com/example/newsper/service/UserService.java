package com.example.newsper.service;

import com.example.newsper.dto.UserDto;
import com.example.newsper.entity.UserEntity;
import com.example.newsper.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor

public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserEntity newUser(UserDto userDto, MultipartFile imgFile){

        UUID uuid = UUID.randomUUID();
        String imageFileName = uuid + "_" + imgFile.getOriginalFilename();
        Path imageFilePath = Paths.get("/users/koko9/downloads"+imageFileName);
        try {
            Files.write(imageFilePath, imgFile.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        UserEntity userEntity = userDto.toEntity();
        userEntity.setPw(passwordEncoder.encode(userEntity.getPw()));
        userEntity.setProfileImgPath(imageFileName);

        return userRepository.save(userEntity);
    }

    public UserEntity newUser(UserDto dto) {
        UserEntity userEntity = dto.toEntity();
        userEntity.setPw(passwordEncoder.encode(userEntity.getPw()));

        return userRepository.save(userEntity);
    }

    public UserEntity show(String id){
        return userRepository.findById(id).orElse(null);
    }

    public UserEntity delete(String id) {
        UserEntity target = userRepository.findById(id).orElse(null);
        if (target == null)
            return null;
        userRepository.delete(target);
        return target;
    }
}
