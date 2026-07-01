package com.example.quant.controller;

import com.example.quant.entity.SysUser;
import com.example.quant.service.AuthSessionService;
import com.example.quant.service.UserService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final AuthSessionService authSessionService;

    public UserController(UserService userService, AuthSessionService authSessionService) {
        this.userService = userService;
        this.authSessionService = authSessionService;
    }

    @GetMapping
    public List<SysUser> users(@RequestHeader(value = "Authorization", required = false) String authorization) {
        authSessionService.requireAdmin(authorization);
        return userService.listUsers();
    }

    @GetMapping("/current")
    public SysUser current(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return authSessionService.requireUser(authorization);
    }

    @PostMapping("/permission")
    public Map<String, String> updatePermission(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                @RequestParam String username,
                                                @RequestParam String role,
                                                @RequestParam boolean canViewData,
                                                @RequestParam boolean canManageUsers) {
        SysUser operator = authSessionService.requireAdmin(authorization);
        userService.updatePermission(operator.getUsername(), username, role, canViewData, canManageUsers);
        return Map.of("message", "permission updated");
    }
}
