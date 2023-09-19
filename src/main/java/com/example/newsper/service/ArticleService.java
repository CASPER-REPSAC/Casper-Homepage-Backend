package com.example.newsper.service;

import com.example.newsper.entity.ArticleEntity;
import com.example.newsper.entity.ArticleMapping;
import com.example.newsper.repository.ArticleRepository;
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

    public List<ArticleMapping> boardList(String boardId, String category, Long listNum){
        return articleRepository.findByBoardList(boardId,category,listNum);
    }

    public ArticleEntity show(Long ArticleId){
        return articleRepository.findById(ArticleId).orElse(null);
    }

    public ArticleEntity save(ArticleEntity article){
        return articleRepository.save(article);
    }

}
