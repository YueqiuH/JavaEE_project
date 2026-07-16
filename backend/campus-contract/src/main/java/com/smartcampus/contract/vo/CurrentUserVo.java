package com.smartcampus.contract.vo;

import com.smartcampus.contract.entity.MenuEntity;
import com.smartcampus.contract.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUserVo {

    private UserEntity user;
    private Set<String> roles;
    private Set<String> permissions;
    private List<MenuEntity> menus;
}
