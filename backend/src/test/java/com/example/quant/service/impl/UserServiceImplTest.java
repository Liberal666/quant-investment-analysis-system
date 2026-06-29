package com.example.quant.service.impl;

import com.example.quant.entity.SysUser;
import com.example.quant.mapper.UserMapper;
import com.example.quant.mapper.UserStockMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceImplTest {
    @Test
    void loginRejectsWrongPassword() {
        UserMapper userMapper = mock(UserMapper.class);
        UserStockMapper userStockMapper = mock(UserStockMapper.class);
        SysUser user = new SysUser();
        user.setUsername("viewer");
        user.setPasswordHash("bad-hash");
        when(userMapper.findByUsername("viewer")).thenReturn(user);

        UserServiceImpl service = new UserServiceImpl(userMapper, userStockMapper);

        assertThatThrownBy(() -> service.login("viewer", "123456"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("用户名或密码错误");
    }

    @Test
    void registerCreatesUserAndDefaultStocks() {
        UserMapper userMapper = mock(UserMapper.class);
        UserStockMapper userStockMapper = mock(UserStockMapper.class);
        SysUser saved = new SysUser();
        saved.setUsername("new_user");
        saved.setDisplayName("新用户");
        saved.setRole("USER");
        saved.setPasswordHash("8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92");
        saved.setCanViewData(true);
        saved.setCanManageUsers(false);
        when(userMapper.findByUsername("new_user")).thenReturn(null, saved);

        UserServiceImpl service = new UserServiceImpl(userMapper, userStockMapper);
        SysUser result = service.register("new_user", "新用户", "123456");

        assertThat(result.getUsername()).isEqualTo("new_user");
        verify(userMapper).insertUser(anyString(), anyString(), anyString());
        verify(userStockMapper, times(3)).upsert(anyString(), anyString());
    }
}
