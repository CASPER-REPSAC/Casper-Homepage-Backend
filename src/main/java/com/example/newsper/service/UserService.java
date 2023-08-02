package com.example.newsper.service;

import com.example.newsper.dto.UserDto;
import com.example.newsper.entity.UserEntity;
import com.example.newsper.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserEntity newUser(UserDto userDto){
        UserEntity userEntity = userDto.toEntity();
        userEntity.setPw(passwordEncoder.encode(userEntity.getPw()));
        if (validateDuplicateUser(userEntity)){
            return null;
        }
        else {
            return userRepository.save(userEntity);
        }
    }


    private boolean validateDuplicateUser(UserEntity userEntity){
        UserEntity findUser = userRepository.findById(userEntity.getId()).orElse(null);
        if (findUser != null){
            return true;
        }
        else {
            return false;
        }
    }
}
