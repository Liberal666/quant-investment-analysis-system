package com.example.quant.service.impl;

import com.example.quant.entity.AuthSession;
import com.example.quant.entity.SysUser;
import com.example.quant.service.AuthSessionService;
import com.example.quant.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

/**
 * 课程设计场景下的轻量会话实现，使用内存保存 token 与用户名映射。
 */
@Service
public class InMemoryAuthSessionService implements AuthSessionService {
    private static final String BEARER_PREFIX = "Bearer ";

    private final Map<String, String> sessions = new ConcurrentHashMap<>();
    private final UserService userService;

    public InMemoryAuthSessionService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public AuthSession issue(SysUser user) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, user.getUsername());
        user.setPasswordHash(null);
        return new AuthSession(token, user);
    }

    @Override
    public SysUser requireUser(String authorization) {
        String token = parseToken(authorization);
        String username = sessions.get(token);
        if (username == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "请先登录");
        }
        SysUser user = userService.getUser(username);
        user.setPasswordHash(null);
        return user;
    }

    @Override
    public SysUser requireAdmin(String authorization) {
        SysUser user = requireUser(authorization);
        if (!Boolean.TRUE.equals(user.getCanManageUsers())) {
            throw new ResponseStatusException(FORBIDDEN, "普通用户不能管理权限");
        }
        return user;
    }

    private static String parseToken(String authorization) {
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new ResponseStatusException(UNAUTHORIZED, "请先登录");
        }
        return authorization.substring(BEARER_PREFIX.length()).trim();
    }
}
