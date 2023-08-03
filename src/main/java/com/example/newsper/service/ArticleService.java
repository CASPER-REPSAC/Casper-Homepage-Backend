package com.example.newsper.service;

import com.example.newsper.entity.ArticleEntity;
import com.example.newsper.repository.ArticleRepository;
import com.example.newsper.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ArticleService {

    @Autowired
    private ArticleRepository articleRepository;

    public List<ArticleEntity> album(){
        return articleRepository.findAll();
    }

    public List<ArticleEntity> articleList(Long BoardId){
        return articleRepository.findByBoardId(BoardId);
    }

    public ArticleEntity show(Long ArticleId){
        return articleRepository.findById(ArticleId).orElse(null);
    }

    public ArticleEntity save(ArticleEntity article){
        return articleRepository.save(article);
    }

}
