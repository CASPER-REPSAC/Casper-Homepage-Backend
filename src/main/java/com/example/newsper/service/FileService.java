package com.example.newsper.service;

import com.example.newsper.dto.FileDto;
import com.example.newsper.repository.FileRepository;
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
public class FileService {

    @Autowired
    private FileRepository fileRepository;

    public void save(FileDto fileDto){
        fileRepository.save(fileDto.toEntity());
    }

    public int update(Long requestId, Long articleId){
        return fileRepository.update(requestId,articleId);
    }

    public List<String> getFiles(Long articleId){
        return fileRepository.getFiles(articleId);
    }

}
