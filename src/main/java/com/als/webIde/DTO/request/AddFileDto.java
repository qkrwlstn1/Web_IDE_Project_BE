package com.als.webIde.DTO.request;

import com.als.webIde.domain.entity.File;
import lombok.Getter;
import lombok.Setter;



@Getter
@Setter
public class AddFileDto {
    private Long userPk;
    private String fileName;
    private String path;
    private final String suffixFile = "java";

    public File toEntity(){
        return File.builder()
                .userPk(userPk)
                .fileTitle(fileName)
                .suffixFile(suffixFile)
                .contentCd(makeBasicCode(fileName))
                .path(path != null ? path : "/default/path")
                .build();
    }

    private String makeBasicCode(String fileName){
        return "import java.util.*;\n"
                + "import java.lang.*;\n"
                + "import java.io.*;\n"
                + "\n"
                + "class " + fileName+ "\n"
                + "{\n"
                + "\tpublic static void main (String[] args) throws java.lang.Exception\n"
                + "\t{\n"
                + "System.out.println(\"hello world!\"); \n"
                + "\t}\n"
                + "}";
    }
}