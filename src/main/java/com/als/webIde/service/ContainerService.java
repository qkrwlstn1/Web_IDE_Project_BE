package com.als.webIde.service;


import com.als.webIde.DTO.response.FileListResponseDto;
import com.als.webIde.domain.entity.File;
import com.als.webIde.domain.repository.ContainerRepository;
import com.als.webIde.domain.repository.FileRepository;
import com.als.webIde.DTO.global.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class ContainerService {

    private final ContainerRepository containerRepository;
    private final FileRepository fileRepository;

    @Transactional(readOnly = true)
    public ResponseDto<FileListResponseDto> getFileList(Long userId) {
        Long containerId = containerRepository.findByMemberUserPk(userId);
        List<File> fileList = fileRepository.findAllByContainerPk(containerId);

        Map<Long, String> fileMap = new HashMap<>();
        FileListResponseDto fileListResponseDto = new FileListResponseDto();

        if (fileList.size() != 0) {
            for (File f : fileList) {
                Long id = f.getFilePk();
                String filename = f.getFileTitle() + "." + f.getSuffixFile();
                fileMap.put(id, filename);
            }
            fileListResponseDto.setFilelist(fileMap);
        }else{
            //기본파일 생성 로직??
            fileMap.put(1L,"Main.java"); // 실제로는 생성된 팡닐 PK 가져오는 로직으로 변경
            fileListResponseDto.setFilelist(fileMap);
        }

        return new ResponseDto<>("성공",fileListResponseDto);
    }


}