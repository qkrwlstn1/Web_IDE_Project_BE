package com.als.webIde.service;
import com.als.webIde.domain.entity.File;
import com.als.webIde.domain.repository.ContainerRepository;
import com.als.webIde.domain.repository.FileRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FileServiceImpl implements FileService {
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private ContainerRepository containerRepository;
    @Autowired
    private DockerService dockerService;
    @Override
    public File createFile(File file) {
        if (file == null || file.getFileTitle() == null ) {
            throw new IllegalArgumentException("파일 제목은 공백일 수 없습니다.");
        }
        return fileRepository.save(file);
    }

    @Override
    public Optional<File> getFile(Long filePk) {
        if (filePk == null) {
            throw new IllegalArgumentException("File Pk가 Null입니다");
        }
        return fileRepository.findById(filePk);
    }

    @Override
    @Transactional
    public File updateFile(File file) {
        if (file == null || file.getFilePk() == null) {
            throw new IllegalArgumentException("잘못된 파일입니다.");
        }
        return fileRepository.save(file);
    }

    @Override
    @Transactional
    public void deleteFile(Long filePk) {
        if (filePk == null) {
            throw new IllegalArgumentException("존재하지 않는 파일입니다.");
        }
        fileRepository.deleteById(filePk);
    }

    @Override
    @Transactional
    public File saveFile(Long userPk, String fileTitle, String content) {
        if (userPk == null || fileTitle == null || content == null) {
            throw new IllegalArgumentException("필수 필드가 비어있습니다.");
        }

        File file = new File();
        file.setUserPk(userPk);  // userID로
        file.setFileTitle(fileTitle);
        file.setSuffixFile(determineSuffix(fileTitle)); // 파일 확장자 추론 로직 필요
        file.setContentCd(
                "import java.util.*;\n"
                + "import java.lang.*;\n"
                + "import java.io.*;\n"
                + "\n"
                + "class Ideone\n"
                + "{\n"
                + "\tpublic static void main (String[] args) throws java.lang.Exception\n"
                + "\t{\n"
                + "\n"
                + "\t}\n"
                + "}");
        file.setPath("/workspace/" + userPk + "/" + fileTitle); // 경로 설정
        fileRepository.save(file);

        return file;
    }

    private String determineSuffix(String fileTitle) {
        int lastDotIndex = fileTitle.lastIndexOf(".");
        return lastDotIndex != -1 ? fileTitle.substring(lastDotIndex + 1) : "";
    }

    private String escapeShellArg(String arg) {
        return arg.replace("\"", "\\\"");
    }
}