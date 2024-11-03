package com.example.newsper.service;

import com.example.newsper.dto.BoardDto;
import com.example.newsper.entity.ArticleEntity;
import com.example.newsper.entity.BoardEntity;
import com.example.newsper.entity.BoardNameKey;
import com.example.newsper.repository.BoardRepository;
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
public class BoardService {

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private ArticleService articleService;

    public BoardEntity save(BoardEntity entity) {
        return boardRepository.save(entity);
    }

    public BoardEntity delete(BoardEntity boardEntity) {
        boardRepository.delete(boardEntity);
        List<ArticleEntity> articles = articleService.findByBoardName(boardEntity);
        for (ArticleEntity article : articles) {
            articleService.delete(article);
        }
        return boardEntity;
    }

    public void update(BoardEntity boardEntity, BoardDto dto) {
        boardRepository.update(boardEntity.getBoardNameKey().getBoardName(), boardEntity.getBoardNameKey().getSubBoardName(), dto.getBoardName(), dto.getSubBoardName());
    }

    public BoardEntity find(BoardNameKey boardNameKey) {
        return boardRepository.findById(boardNameKey).orElse(null);
    }

    public List<String> findCategory(String id) {
        return boardRepository.findCategory(id);
    }

    public List<BoardEntity> findAll() {
        return boardRepository.findAll();
    }
}
