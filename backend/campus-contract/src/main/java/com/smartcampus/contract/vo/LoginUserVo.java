package com.smartcampus.contract.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUserVo {

    private String token;
    private String tokenType;
    private long expiresIn;
    private CurrentUserVo currentUser;
}
