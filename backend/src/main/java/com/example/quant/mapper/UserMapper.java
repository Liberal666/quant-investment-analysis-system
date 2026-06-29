package com.example.quant.mapper;

import com.example.quant.entity.SysUser;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface UserMapper {
    @Select("SELECT id, username, display_name, role, can_view_data, can_manage_users FROM sys_user ORDER BY id")
    List<SysUser> findAll();

    @Select("SELECT * FROM sys_user WHERE username = #{username}")
    SysUser findByUsername(String username);

    @Insert("""
            INSERT INTO sys_user(username, display_name, role, password_hash, can_view_data, can_manage_users)
            VALUES(#{username}, #{displayName}, 'USER', #{passwordHash}, 1, 0)
            """)
    int insertUser(@Param("username") String username,
                   @Param("displayName") String displayName,
                   @Param("passwordHash") String passwordHash);

    @Update("""
            UPDATE sys_user
            SET role = #{role},
                can_view_data = #{canViewData},
                can_manage_users = #{canManageUsers}
            WHERE username = #{username}
            """)
    int updatePermission(@Param("username") String username,
                         @Param("role") String role,
                         @Param("canViewData") boolean canViewData,
                         @Param("canManageUsers") boolean canManageUsers);
}
