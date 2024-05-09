package com.als.webIde.DTO.request;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {

    private String userId;
    private String nickname;
    private String password;
    private String passwordConfirm;
}
