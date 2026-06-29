package com.example.quant.mapper;

import com.example.quant.entity.TradeStrategy;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface TradeStrategyMapper {
    @Select("""
            SELECT *
            FROM trade_strategy
            WHERE username = #{username}
            ORDER BY trade_date DESC, id DESC
            """)
    List<TradeStrategy> findByUsername(String username);

    @Insert("""
            INSERT INTO trade_strategy(username, trade_date, code, type, content)
            VALUES(#{username}, #{tradeDate}, #{code}, #{type}, #{content})
            """)
    void insert(TradeStrategy strategy);
}
