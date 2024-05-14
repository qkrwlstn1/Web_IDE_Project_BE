package com.als.webIde.service;

import com.als.webIde.domain.entity.File;
import java.util.Optional;

public interface FileService {

    File createFile(File file);
    Optional<File> getFile(Long filePk);

    File updateFile(File file);
    void deleteFile(Long filePk);
    File saveFile(Long userPk, String filename, String content);
}