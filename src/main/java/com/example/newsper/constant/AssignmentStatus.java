package com.example.newsper.constant;

import lombok.Getter;

@Getter
public enum AssignmentStatus {
    GRADED("채점완료"),
    SUBMITTED("제출완료"),
    NOT_SUBMITTED("미제출");
    private final String status;
    AssignmentStatus(String status) {
        this.status = status;
    }
}
