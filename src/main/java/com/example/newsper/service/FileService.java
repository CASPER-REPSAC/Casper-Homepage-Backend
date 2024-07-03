package com.example.newsper.service;

import com.example.newsper.dto.FileDto;
import com.example.newsper.entity.FileEntity;
import com.example.newsper.entity.UserEntity;
import com.example.newsper.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;

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

    public void modify(FileEntity fileEntity){
        fileRepository.save(fileEntity);
    }

    public FileEntity findById(String id){ return fileRepository.findById(id).orElse(null); }

    public List<String> getUrls(String id){
        return fileRepository.getUrls(id);
    }

//    public void update(String requestId, String id){
//        fileRepository.update(requestId, id);
//    }



    public List<Object> getFileNames(Long articleId){
        List<String> files = fileRepository.getUrls(String.valueOf(articleId));
        List<Object> ret = new ArrayList<>();
        for(String file : files){
            Map<String,Object> map = new HashMap<>();
            map.put("name",file.substring(82));
            map.put("src",file);

            ret.add(map);
        }
        return ret;
    }

    public String fileUpload(MultipartFile file, String fileType) throws IOException {
        String uploadFolder = "/home/casper/newsper_"+fileType;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        Date date = new Date();
        String str = sdf.format(date);
        String datePath = str.replace("-", File.separator);

        File uploadPath = new File(uploadFolder, datePath);

        if (uploadPath.exists() == false) {
            uploadPath.mkdirs();
        }

        /* 파일 이름 */
        String uploadFileName = file.getOriginalFilename();

        /* UUID 설정 */
        String uuid = UUID.randomUUID().toString();
        uploadFileName = uuid + "_" + uploadFileName;

        /* 파일 위치, 파일 이름을 합친 File 객체 */
        File saveFile = new File(uploadPath, uploadFileName);

        file.transferTo(saveFile);

        String serverUrl = "https://build.casper.or.kr";
        return serverUrl + "/"+fileType+"/" + datePath + "/" + uploadFileName;
    }

    public void delete(String path){
        FileEntity fileEntity = fileRepository.findById(path).orElse(null);
        String fileType = fileEntity.getType();

        String filePath = "/home/casper/newsper_"+fileType;
        String result = path.substring("https://build.casper.or.kr/".length());

        // 파일 객체 생성
        File file = new File(filePath+result);

        // 파일 삭제
        file.delete();

        fileRepository.delete(fileEntity);
    }

//    public void delete(String articleId){
//        FileService fileEntity
//        fileRepository.deletebyArticleId(articleId);
//    }

}
