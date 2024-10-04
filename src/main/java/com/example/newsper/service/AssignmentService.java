package com.example.newsper.service;

import com.example.newsper.dto.AssignmentDto;
import com.example.newsper.dto.AssignmentListDto;
import com.example.newsper.dto.CreateAssignmentDto;
import com.example.newsper.entity.ArticleEntity;
import com.example.newsper.entity.ArticleList;
import com.example.newsper.entity.AssignmentEntity;
import com.example.newsper.repository.ArticleRepository;
import com.example.newsper.repository.AssignmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AssignmentService {
    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private FileService fileService;

    public AssignmentEntity save(AssignmentEntity assignmentEntity){
        return assignmentRepository.save(assignmentEntity);
    }

    public AssignmentEntity findById(Long assignmentId){
        return assignmentRepository.findById(assignmentId).orElse(null);
    }

    public AssignmentEntity update(AssignmentEntity assignmentEntity, CreateAssignmentDto assignmentDto){
        assignmentEntity.setTitle(assignmentDto.getTitle());
        assignmentEntity.setCategory(assignmentDto.getCategory());
        assignmentEntity.setDescription(assignmentDto.getDescription());
        assignmentEntity.setDeadline(assignmentDto.getDeadline());
        assignmentRepository.save(assignmentEntity);
        return assignmentEntity;
    }

    public void delete(AssignmentEntity assignmentEntity){
        List<String> urls = fileService.getUrls(String.valueOf(assignmentEntity.getAssignmentId()),"article");

        for(String url : urls){
            fileService.delete(url);
        }

        assignmentRepository.delete(assignmentEntity);
    }

    public int getMaxPageNum(){
        return assignmentRepository.assignmentCount();
    }

    public List<AssignmentListDto> assignmentList(Long listNum){
        List<Object[]> obj = assignmentRepository.AssignmentList(listNum);

        return obj.stream()
                .map(row -> new AssignmentListDto(
                        (Long) row[0],      // assignmentId
                        (String) row[1],    // title
                        (String) row[2],    // category
                        (Date) row[3],      // deadline
                        (String) row[4],    // userId
                        (String) row[5],    // name
                        (String) row[6]))   // progress
                .collect(Collectors.toList());
    }
}
