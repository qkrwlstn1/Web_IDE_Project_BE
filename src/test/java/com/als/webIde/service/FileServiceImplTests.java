package com.als.webIde.service;

import com.als.webIde.domain.entity.File;
import com.als.webIde.domain.repository.FileRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test") //application-test.yml
public class FileServiceImplTests {

    @Autowired
    private FileService fileService;

    @MockBean
    private FileRepository fileRepository;

    @MockBean
    private DockerService dockerService;

    // 테스트: 유효한 속성으로 파일 생성.
    @Test
    public void testCreateFile() {
        File file = new File();
        file.setUserPk(1L);
        file.setFileTitle("TestFile.java");
        file.setContentCd("public class TestFile {}");

        when(fileRepository.save(any(File.class))).thenReturn(file);
        File created = fileService.createFile(file);

        assertNotNull(created);
        assertEquals("TestFile.java", created.getFileTitle());
    }

    // 테스트: 파일 저장 기능.
    @Test
    public void testSaveFile() {
        Long userPk = 1L;
        String fileTitle = "TestFile.java";
        String content = "퍼블릭 어쩌구 제발 성공 좀 합시다!";

        File file = new File();
        file.setUserPk(userPk);
        file.setFileTitle(fileTitle);
        file.setSuffixFile("java");
        file.setContentCd(content);
        file.setPath("/workspace/" + userPk + "/" + fileTitle);

        when(fileRepository.save(any(File.class))).thenReturn(file);

        File savedFile = fileService.saveFile(userPk, fileTitle, content);

        ArgumentCaptor<File> fileCaptor = ArgumentCaptor.forClass(File.class);
        verify(fileRepository).save(fileCaptor.capture());
        File capturedFile = fileCaptor.getValue();

        // 저장됐는지, 확장자는 같은지, 제목은 같은지 확인
        assertNotNull(savedFile);
        assertEquals("java",capturedFile.getSuffixFile());
        assertEquals("/workspace/1/TestFile.java", savedFile.getPath(), "경로가 일치합니다.");
        assertEquals(fileTitle, capturedFile.getFileTitle());
    }

    // 테스트: 파일 삭제 기능.
    @Test
    public void testDeleteFile() {
        doNothing().when(fileRepository).deleteById(anyLong());
        assertDoesNotThrow(() -> fileService.deleteFile(1L));
        verify(fileRepository).deleteById(1L);
    }
}
