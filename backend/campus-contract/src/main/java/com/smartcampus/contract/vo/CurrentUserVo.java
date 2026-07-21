package com.smartcampus.contract.vo;

import com.smartcampus.contract.entity.Menu;
import com.smartcampus.contract.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUserVo {

    private User user;
    private Set<String> roles;
    private Set<String> permissions;
    private List<Menu> menus;
}
