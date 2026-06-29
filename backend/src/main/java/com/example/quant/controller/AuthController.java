package com.example.quant.controller;

import com.example.quant.entity.SysUser;
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

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public SysUser login(@RequestBody AuthRequest request) {
        return userService.login(request.username(), request.password());
    }

    @PostMapping("/register")
    public SysUser register(@RequestBody AuthRequest request) {
        return userService.register(request.username(), request.displayName(), request.password());
    }

    public record AuthRequest(String username, String displayName, String password) {
    }
}
