package com.example.newsper.service;

import com.example.newsper.entity.UserEntity;
import com.example.newsper.redis.RedisUtil;
import com.example.newsper.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AccountLockService {
    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JavaMailSender javaMailSender;

    public int countLoginFailed(UserEntity user) {
        int count = 0;
        if (redisUtil.existData(user.getId())) {
            count = Integer.parseInt(redisUtil.getData(user.getId()));
            redisUtil.deleteData(user.getId());
        }
        count++;

        redisUtil.setDataExpire(user.getId(), String.valueOf(count), 60 * 5L);

        log.info("attemptCount : {}", count);
        return count;
    }
}