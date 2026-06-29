package com.example.quant.service;

import com.example.quant.entity.SysUser;

import java.util.List;

public interface UserService {
    List<SysUser> listUsers();

    SysUser getUser(String username);

    SysUser login(String username, String password);

    SysUser register(String username, String displayName, String password);

    void updatePermission(String currentUser, String username, String role, boolean canViewData, boolean canManageUsers);
}
