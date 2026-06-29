package com.example.quant.service.impl;

import com.example.quant.entity.SysUser;
import com.example.quant.mapper.UserMapper;
import com.example.quant.mapper.UserStockMapper;
import com.example.quant.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
public class UserServiceImpl implements UserService {
    private static final List<String> DEFAULT_STOCKS = List.of("000001", "000858", "600519");

    private final UserMapper userMapper;
    private final UserStockMapper userStockMapper;

    public UserServiceImpl(UserMapper userMapper, UserStockMapper userStockMapper) {
        this.userMapper = userMapper;
        this.userStockMapper = userStockMapper;
    }

    @Override
    public List<SysUser> listUsers() {
        return userMapper.findAll().stream().peek(user -> user.setPasswordHash(null)).toList();
    }

    @Override
    public SysUser getUser(String username) {
        SysUser user = userMapper.findByUsername(username);
        if (user == null) {
            throw new ResponseStatusException(NOT_FOUND, "user not found");
        }
        return user;
    }

    @Override
    public SysUser login(String username, String password) {
        SysUser user = userMapper.findByUsername(username);
        if (user == null || !hashPassword(password).equals(user.getPasswordHash())) {
            throw new ResponseStatusException(UNAUTHORIZED, "用户名或密码错误");
        }
        user.setPasswordHash(null);
        return user;
    }

    @Override
    public SysUser register(String username, String displayName, String password) {
        validateAccount(username, password);
        if (userMapper.findByUsername(username) != null) {
            throw new ResponseStatusException(BAD_REQUEST, "用户名已存在");
        }
        String resolvedDisplayName = displayName == null || displayName.isBlank() ? username : displayName.trim();
        userMapper.insertUser(username.trim(), resolvedDisplayName, hashPassword(password));
        for (String code : DEFAULT_STOCKS) {
            userStockMapper.upsert(username.trim(), code);
        }
        return login(username, password);
    }

    @Override
    public void updatePermission(String currentUser, String username, String role, boolean canViewData, boolean canManageUsers) {
        SysUser operator = getUser(currentUser);
        if (!Boolean.TRUE.equals(operator.getCanManageUsers())) {
            throw new ResponseStatusException(FORBIDDEN, "only administrator can manage user permissions");
        }
        int updated = userMapper.updatePermission(username, role, canViewData, canManageUsers);
        if (updated == 0) {
            throw new ResponseStatusException(NOT_FOUND, "user not found");
        }
    }

    private static void validateAccount(String username, String password) {
        if (username == null || !username.matches("[A-Za-z0-9_]{3,32}")) {
            throw new ResponseStatusException(BAD_REQUEST, "用户名必须为3-32位字母、数字或下划线");
        }
        if (password == null || password.length() < 6) {
            throw new ResponseStatusException(BAD_REQUEST, "密码至少需要6位");
        }
    }

    private static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(bytes.length * 2);
            for (byte item : bytes) {
                builder.append(String.format("%02x", item));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 unavailable", ex);
        }
    }
}
