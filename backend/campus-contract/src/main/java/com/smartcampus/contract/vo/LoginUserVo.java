package com.smartcampus.contract.vo;

import com.smartcampus.contract.entity.MenuEntity;
import com.smartcampus.contract.entity.UserEntity;
import lombok.Data; import lombok.ToString;
import java.util.List;

@Data @ToString
public class LoginUserVo {
    private UserEntity user;
    private List<MenuEntity> menus;
    private String token;

    public LoginUserVo() {}
    public LoginUserVo(UserEntity userEntity, List<MenuEntity> lists, String token) {
        this.user = userEntity; this.menus = lists; this.token = token;
    }
}
