package com.example.newsper.constant;

import lombok.Getter;

@Getter
public enum AssignmentStatus {
    GRADED("채점완료"),
    PROGRESS("진행중"),
    SUBMITTED("제출완료"),
    ENDED("마감됨");
    private final String status;
    AssignmentStatus(String status) {
        this.status = status;
    }
}
