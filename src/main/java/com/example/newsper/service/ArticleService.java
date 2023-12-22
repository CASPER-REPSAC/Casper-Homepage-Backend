package com.example.newsper.service;

import com.example.newsper.dto.ArticleDto;
import com.example.newsper.entity.ArticleEntity;
import com.example.newsper.entity.ArticleList;
import com.example.newsper.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ArticleService {

    @Autowired
    private ArticleRepository articleRepository;

    public List<ArticleList> boardList(String boardId, String category, Long listNum){
        return articleRepository.findByBoardList(boardId,category,listNum);
    }

    public int getMaxPageNum(String boardId, String category){
        return articleRepository.findAllBoardListCount(boardId,category);
    }

    public ArticleEntity show(Long articleId){
        return articleRepository.findById(articleId).orElse(null);
    }

    public String getBoardId(Long articleId){ return articleRepository.findById(articleId).get().getBoardId(); }

    public String getCreater(Long articleId){ return articleRepository.findById(articleId).get().getUserId(); }

    public ArticleEntity save(ArticleEntity article){
        return articleRepository.save(article);
    }

    public ArticleEntity delete(Long articleId) {
        ArticleEntity target = articleRepository.findById(articleId).orElse(null);
        if (target == null)
            return null;
        articleRepository.delete(target);
        return target;
    }

    public ArticleEntity update(Long id, ArticleDto dto) {
        ArticleEntity article = dto.toEntity();
        ArticleEntity target = articleRepository.findById(id).orElse(null);
        if (target == null || !id.equals(article.getArticleId())){
            return null;
        }
        target.patch(article);
        log.info(target.toString());
        ArticleEntity updated = articleRepository.save(target);
        return updated;
    }
}
