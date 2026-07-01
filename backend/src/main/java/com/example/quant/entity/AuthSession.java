package com.example.quant.entity;

/**
 * 登录成功后返回给前端的会话信息。
 */
public record AuthSession(String token, SysUser user) {
}
