package com.als.webIde.controller;

import com.als.webIde.domain.entity.File;
import com.als.webIde.service.FileService;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/project")
public class FileController {

    @Autowired
    private FileService fileService;

    // 파일 생성하기
    @PostMapping("/project/files")
    public ResponseEntity<File> createFile(@RequestBody File file){
        File createdFile = fileService.createFile(file);
        return ResponseEntity.ok(createdFile);
    }

    // 파일 아이디 받아오기
    @GetMapping("/{filePK}")
    public ResponseEntity<File> getFileById(@PathVariable Long filePk){
        Optional<File> file = fileService.getFile(filePk);
        return file.map(ResponseEntity::ok)
                .orElseThrow(() -> new RuntimeException("해당 ID의 파일을 찾지 못했습니다.: " + filePk));
    }

    // 파일 수정, 덮어쓰기
    @PutMapping("/{filePk}")
    public ResponseEntity<File> updateFile(@PathVariable Long filePk, @RequestBody File fileDetails) {
        File existingFile = fileService.getFile(filePk).orElse(null);
        if (existingFile == null) {
            return ResponseEntity.notFound().build();
        }
        existingFile.setSuffixFile(fileDetails.getSuffixFile());
        existingFile.setContentCd(fileDetails.getContentCd());
        existingFile.setFileTitle(fileDetails.getFileTitle());
        existingFile.setPath(fileDetails.getPath());
        final File updatedFile = fileService.updateFile(existingFile);
        return ResponseEntity.ok(updatedFile);
    }

    // 파일 삭제
    @DeleteMapping("/project/file/{filePk}")
    public ResponseEntity<Void> deleteFile(@PathVariable Long filePk, Long userPk) {
        if (!fileService.getFile(filePk).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        fileService.deleteFile(filePk);
        return ResponseEntity.ok().build();
    }
}

//    @PosMapping("/{userId}/{filename}")
//    public ResponseEntity<String> createOrUpdateFile(@PathVariable String userId,
//                                                     @PathVariable String filename,
//                                                     @RequestBody String content) {
//        try {
//            fileServiceImp.saveFile(userId, filename, content);
//            return ResponseEntity.ok("파일 저장 성공");
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body("파일을 저장하지 못했습니다. : " + e.getMessage());
//        }
//    }
//
//    @GetMapping("/{userId}/{filename}")
//    public ResponseEntity<?> readFile(@PathVariable String userId,
//                                      @PathVariable String filename) {
//        try {
//            String fileContent = fileServiceImp.readFile(userId, filename);
//            return ResponseEntity.ok(fileContent);
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body("파일을 불러오지 못했습니다: " + e.getMessage());
//        }
//    }
//
//    @DeleteMapping("/{userId}/{filename}")
//    public ResponseEntity<String> deleteFile(@PathVariable String userId,
//                                             @PathVariable String filename) {
//        try {
//            fileServiceImp.deleteFile(userId, filename);
//            return ResponseEntity.ok("파일을 성공적으로 삭제했습니다.");
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body("파일을 삭제하지 못했습니다: " + e.getMessage());
//        }
//    }

