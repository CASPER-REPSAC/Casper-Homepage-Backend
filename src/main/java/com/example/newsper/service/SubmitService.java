package com.example.newsper.service;

import com.example.newsper.dto.CreateSubmitDto;
import com.example.newsper.dto.SubmitListDto;
import com.example.newsper.entity.CommentEntity;
import com.example.newsper.entity.SubmitEntity;
import com.example.newsper.repository.SubmitRepository;
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
public class SubmitService {

    @Autowired
    private SubmitRepository submitRepository;
    public SubmitEntity findById(Long submitId){
        return submitRepository.findById(submitId).orElse(null);
    }

    public SubmitEntity findByUserId(String userId){
        return submitRepository.findByUserId(userId);
    }

    public SubmitEntity save(SubmitEntity submitEntity) { return submitRepository.save(submitEntity); }

    public List<SubmitListDto> findByAssignmentId(Long assignmentId){
        return submitRepository.findByAssignmentId2(assignmentId);
    }
    public SubmitEntity update(SubmitEntity submitEntity, CreateSubmitDto dto){
        submitEntity.setContent(dto.getContent());
        submitRepository.save(submitEntity);
        return submitEntity;
    }

    public void delete(SubmitEntity submitEntity){
        submitRepository.delete(submitEntity);
    }

    public void deleteByAssignment(Long assignmentId){
        List<SubmitEntity> submits = submitRepository.findByAssignmentId(assignmentId);
        submitRepository.deleteAll(submits);
    }
}
