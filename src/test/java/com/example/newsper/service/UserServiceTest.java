package com.example.newsper.service;

import com.example.newsper.dto.UserDto;
import com.example.newsper.entity.UserEntity;
import com.example.newsper.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void newUser() {
        UserDto userDto = new UserDto("1","1","1","1","1",new Date(),"1");
        UserEntity expected = userService.newUser(userDto);
        UserEntity userEntity = userRepository.findById("1").orElse(null);

        assertEquals(expected.toString(),userEntity.toString());

    }
}