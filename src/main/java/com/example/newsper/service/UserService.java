package com.example.newsper.service;

import com.example.newsper.dto.UserDto;
import com.example.newsper.entity.UserEntity;
import com.example.newsper.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserEntity newUser(UserDto dto) {
        UserEntity userEntity = dto.toEntity();
        userEntity.setPw(passwordEncoder.encode(userEntity.getPw()));

        return userRepository.save(userEntity);
    }

    public UserEntity modify(UserEntity user){
        return userRepository.save(user);
    }

    public UserEntity findById(String id){
        return userRepository.findById(id).orElse(null);
    }

    public UserEntity findByEmail(String email) { return userRepository.findByEmail(email); }

    public String getAuth(String id){
        return userRepository.findById(id).get().getRole().toString();
    }

    public List<UserEntity> showall() {
        return userRepository.findAll();
    }

    public UserEntity delete(String id) {
        UserEntity target = userRepository.findById(id).orElse(null);
        if (target == null)
            return null;
        userRepository.delete(target);
        return target;
    }
}
