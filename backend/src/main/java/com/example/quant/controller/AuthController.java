package com.example.quant.controller;

import com.example.quant.entity.AuthSession;
import com.example.quant.entity.SysUser;
import com.example.quant.service.AuthSessionService;
import com.example.quant.service.UserService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final AuthSessionService authSessionService;

    public AuthController(UserService userService, AuthSessionService authSessionService) {
        this.userService = userService;
        this.authSessionService = authSessionService;
    }

    @PostMapping("/login")
    public AuthSession login(@RequestBody AuthRequest request) {
        SysUser user = userService.login(request.username(), request.password());
        return authSessionService.issue(user);
    }

    @PostMapping("/register")
    public AuthSession register(@RequestBody AuthRequest request) {
        SysUser user = userService.register(request.username(), request.displayName(), request.password());
        return authSessionService.issue(user);
    }

    public record AuthRequest(String username, String displayName, String password) {
    }
}
