package com.example.newsper.entity;

import java.util.Date;

public interface ArticleList {
    Date getCreatedAt();
    String getTitle();
    String getNickname();
    Long getArticleId();
    Long getView();
    Boolean getFile();
    Boolean getHide();
    Long getNumOfComments();
}
