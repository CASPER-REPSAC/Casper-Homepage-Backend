package com.example.newsper.service;

import com.example.newsper.entity.ArticleEntity;
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

    public void setCount(String id) {
        if (redisUtil.existData(id)) {
            redisUtil.INCRData(id);
        } else {
            redisUtil.setDataExpire(id, "1", 5 * 60L);
        }
    }

    public boolean validation(String id) {
        if (redisUtil.existData(id)) {
            if (Integer.parseInt(redisUtil.getData(id)) >= 5) {
                redisUtil.INCRData(id);
                return true;
            } else return false;
        } else return false;
    }

    public void deleteCount(String id) {
        redisUtil.deleteData(id);
    }

    public boolean isArticleVisited(UserEntity user, ArticleEntity article) {
        if (redisUtil.existData(user.getId() + "_" + article.getArticleId())) {
            redisUtil.setDataExpire(user.getId() + "_" + article.getArticleId(), "visited", 5 * 60L);
            return true;
        } else {
            redisUtil.setDataExpire(user.getId() + "_" + article.getArticleId(), "visited", 5 * 60L);
            return false;
        }
    }

}