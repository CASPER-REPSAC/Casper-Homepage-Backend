package com.example.newsper.service;

import com.example.newsper.dto.FileDto;
import com.example.newsper.entity.FileEntity;
import com.example.newsper.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class FileService {

    private final String uploadPath = "/home/casper/";
    @Autowired
    private FileRepository fileRepository;
    @Value("${custom.server.url}")
    private String serverUrl;

    public void save(FileDto fileDto) {
        fileRepository.save(fileDto.toEntity());
    }

    public void modify(FileEntity fileEntity) {
        fileRepository.save(fileEntity);
    }

    public FileEntity findById(String id) {
        return fileRepository.findById(id).orElse(null);
    }

    public List<String> getUrls(String id, String type) {
        return fileRepository.getUrls(id, type);
    }

//    public void update(String requestId, String id){
//        fileRepository.update(requestId, id);
//    }


    public List<Map<String, String>> getFileNames(Long id, String type) {
        List<String> files = fileRepository.getUrls(String.valueOf(id), type);
        List<Map<String, String>> ret = new ArrayList<>();
        for (String file : files) {
            Map<String, String> map = new HashMap<>();
            String fileName = file.substring(file.lastIndexOf("/") + 1);
            String extractedFileName = fileName.substring(fileName.indexOf("_") + 1);
            map.put("name", extractedFileName);
            map.put("src", file);

            ret.add(map);
        }
        return ret;
    }

    public String fileUpload(MultipartFile file, String fileType) throws IOException {
        fileType = fileType.toLowerCase().replace(File.separator, "_");
        String uploadFolder = uploadPath + File.separator + fileType;
        String datePath = new SimpleDateFormat("yyyy/MM/dd").format(new Date());

        File uploadPath = new File(uploadFolder, datePath);

        if (!uploadPath.exists()) {
            boolean ignored = uploadPath.mkdirs();
        }

        /* 파일 이름 */
        String uploadFileName = file.getOriginalFilename();
        if(uploadFileName != null) {
            uploadFileName = uploadFileName.trim().replace(File.separator, "_");
        }
        /* UUID 설정 */
        String uuid = UUID.randomUUID().toString();
        uploadFileName = uuid + "_" + uploadFileName;

        /* 파일 위치, 파일 이름을 합친 File 객체 */
        File saveFile = new File(uploadPath, uploadFileName);
        log.info("저장할 경로: {}", saveFile.getAbsolutePath());

        file.transferTo(saveFile);

        return serverUrl + ("/" + fileType + "/" + datePath + "/" + uploadFileName).replace("/", File.separator);
    }

    public void delete(String path) {
        FileEntity fileEntity = fileRepository.findById(path).orElse(null);
        if (fileEntity == null) {
            log.info("파일이 존재하지 않습니다. {}", path);
            return;
        }
        String filePath = uploadPath;
        String result = path.substring(serverUrl.length());

        // 파일 객체 생성
        File file = new File(filePath + result);
        log.info(filePath);
        log.info(result);
        log.info("{}: {}", filePath, result);

        fileRepository.delete(fileEntity);
        if (!file.exists()) {
            log.info("파일이 존재하지 않습니다. {}", file.getAbsolutePath());
            return;
        }

        // 파일 삭제
        boolean ignored = file.delete();
    }
}
