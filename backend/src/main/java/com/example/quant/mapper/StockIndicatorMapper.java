package com.example.quant.mapper;

import com.example.quant.entity.StockIndicator;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface StockIndicatorMapper {
    @Select("SELECT * FROM stock_indicator WHERE code = #{code} ORDER BY date")
    List<StockIndicator> findByCode(String code);

    @Delete("DELETE FROM stock_indicator WHERE code = #{code}")
    void deleteByCode(String code);

    @Insert("""
            INSERT INTO stock_indicator(code, date, ma5, ma10, ma20, dif, dea, macd, rsi)
            VALUES(#{code}, #{date}, #{ma5}, #{ma10}, #{ma20}, #{dif}, #{dea}, #{macd}, #{rsi})
            ON DUPLICATE KEY UPDATE
              ma5 = VALUES(ma5),
              ma10 = VALUES(ma10),
              ma20 = VALUES(ma20),
              dif = VALUES(dif),
              dea = VALUES(dea),
              macd = VALUES(macd),
              rsi = VALUES(rsi)
            """)
    void upsert(StockIndicator indicator);
}
