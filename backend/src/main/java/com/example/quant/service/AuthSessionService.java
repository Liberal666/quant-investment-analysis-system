package com.example.quant.service;

import com.example.quant.entity.AuthSession;
import com.example.quant.entity.SysUser;

/**
 * 维护登录会话并校验当前请求的真实用户身份。
 */
public interface AuthSessionService {
    AuthSession issue(SysUser user);

    SysUser requireUser(String authorization);

    SysUser requireAdmin(String authorization);
}
