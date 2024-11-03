package com.example.newsper.entity;

import java.util.Date;

public interface ArticleList {
    Date getCreatedAt();

    String getTitle();

    String getNickname();

    Long getArticleId();

    String getBoardId();

    String getCategory();

    Long getView();

    Boolean getFile();

    Boolean getHide();

    Long getNumOfComments();
}