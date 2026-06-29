package com.example.quant.controller;

import com.example.quant.entity.SysUser;
import com.example.quant.service.UserService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
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

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<SysUser> users() {
        return userService.listUsers();
    }

    @GetMapping("/current")
    public SysUser current(@RequestParam(defaultValue = "viewer") String username) {
        SysUser user = userService.getUser(username);
        user.setPasswordHash(null);
        return user;
    }

    @PostMapping("/permission")
    public Map<String, String> updatePermission(@RequestParam String currentUser,
                                                @RequestParam String username,
                                                @RequestParam String role,
                                                @RequestParam boolean canViewData,
                                                @RequestParam boolean canManageUsers) {
        userService.updatePermission(currentUser, username, role, canViewData, canManageUsers);
        return Map.of("message", "permission updated");
    }
}
