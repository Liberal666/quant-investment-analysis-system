package com.example.quant.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

public interface UserStockMapper {
    @Select("""
            SELECT code
            FROM user_stock
            WHERE username = #{username}
            ORDER BY id
            """)
    List<String> findCodesByUsername(String username);

    @Insert("""
            INSERT INTO user_stock(username, code)
            VALUES(#{username}, #{code})
            ON DUPLICATE KEY UPDATE code = VALUES(code)
            """)
    void upsert(@Param("username") String username, @Param("code") String code);

    @Delete("""
            DELETE FROM user_stock
            WHERE username = #{username}
              AND code = #{code}
            """)
    int deleteByUsernameAndCode(@Param("username") String username, @Param("code") String code);
}
