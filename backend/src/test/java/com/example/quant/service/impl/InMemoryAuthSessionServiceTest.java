package com.example.quant.service.impl;

import com.example.quant.entity.AuthSession;
import com.example.quant.entity.SysUser;
import com.example.quant.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InMemoryAuthSessionServiceTest {
    @Test
    void issuedTokenResolvesCurrentUser() {
        UserService userService = mock(UserService.class);
        SysUser viewer = user("viewer", false);
        when(userService.getUser("viewer")).thenReturn(viewer);

        InMemoryAuthSessionService service = new InMemoryAuthSessionService(userService);
        AuthSession session = service.issue(viewer);

        SysUser current = service.requireUser("Bearer " + session.token());

        assertThat(current.getUsername()).isEqualTo("viewer");
    }

    @Test
    void normalUserCannotPassAdminCheck() {
        UserService userService = mock(UserService.class);
        SysUser viewer = user("viewer", false);
        when(userService.getUser("viewer")).thenReturn(viewer);

        InMemoryAuthSessionService service = new InMemoryAuthSessionService(userService);
        AuthSession session = service.issue(viewer);

        assertThatThrownBy(() -> service.requireAdmin("Bearer " + session.token()))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(error -> ((ResponseStatusException) error).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void missingTokenIsRejected() {
        InMemoryAuthSessionService service = new InMemoryAuthSessionService(mock(UserService.class));

        assertThatThrownBy(() -> service.requireUser(null))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(error -> ((ResponseStatusException) error).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private static SysUser user(String username, boolean canManageUsers) {
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setCanManageUsers(canManageUsers);
        user.setCanViewData(true);
        return user;
    }
}
