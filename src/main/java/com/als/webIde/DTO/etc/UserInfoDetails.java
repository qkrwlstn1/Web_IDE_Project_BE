package com.als.webIde.DTO.etc;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDetails {
    private Long id;
    private String userId;
    private String userPassword;
    private String userNickName;
    private String userTheme;
}
