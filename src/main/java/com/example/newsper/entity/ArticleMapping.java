package com.example.newsper.entity;

import java.util.Date;

public interface ArticleMapping {
    Date getCreated_at();
    String getTitle();
    String getNickname();
    Long getArticle_id();
    Long getView();
    Boolean getFile();
    Boolean getHide();
    Long getNumOfComments();
    String photo();
}
