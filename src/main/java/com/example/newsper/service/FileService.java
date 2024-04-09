package com.example.newsper.service;

import com.example.newsper.dto.FileDto;
import com.example.newsper.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class FileService {

    @Autowired
    private FileRepository fileRepository;

    public void save(FileDto fileDto){
        fileRepository.save(fileDto.toEntity());
    }

    public void update(Long requestId, Long articleId){
        fileRepository.update(requestId,articleId);
    }

}
