package com.example.newsper.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ErrorCodeService {
    public Map<String, Object> setErrorCodeBody(int code){
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("code", code);

        if(code == -101) responseBody.put("message", "로그인 아이디를 찾을 수 없음");
        else if(code == -102) responseBody.put("message", "로그인 패스워드 불일치");
        else if(code == -105) responseBody.put("message", "로그인 5회 이상 실패");
        else if(code == -106) responseBody.put("message", "해당 정보로 가입된 계정을 찾을 수 없음");
        else if(code == -201) responseBody.put("message", "회원 가입 파라미터 누락");
        else if(code == -202) responseBody.put("message", "회원 가입 이메일 인증 오류");
        else if(code == -203) responseBody.put("message", "회원 가입 ID 중복");
        else if(code == -301) responseBody.put("message", "게시판 접근 권한 없음");
        else if(code == -302) responseBody.put("message", "게시글 쓰기 권한 없음");
        else if(code == -303) responseBody.put("message", "게시글 수정/삭제 권한 없음");
        else if(code == -401) responseBody.put("message", "파일 용량 10MB 초과");
        else if(code == -402) responseBody.put("message", "파일 이름이 너무 길거나 NULL");
        else if(code == -501) responseBody.put("message", "관리자의 권한은 바꿀 수 없음");
        else if(code == -1) responseBody.put("message", "지정되지 않은 에러");

        return responseBody;
    }
}

