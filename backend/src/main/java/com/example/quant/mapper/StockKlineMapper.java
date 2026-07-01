package com.example.quant.mapper;

import com.example.quant.entity.StockKline;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

public interface StockKlineMapper {
    @Select("SELECT * FROM stock_kline WHERE code = #{code} ORDER BY date")
    List<StockKline> findByCode(String code);

    @Select("SELECT DISTINCT code FROM stock_kline ORDER BY code")
    List<String> findCodes();

    @Select("SELECT MAX(date) FROM stock_kline WHERE code = #{code}")
    LocalDate findLatestDate(String code);

    @Delete("DELETE FROM stock_kline WHERE code = #{code}")
    void deleteByCode(String code);

    @Insert("""
            INSERT INTO stock_kline(code, date, open, high, low, close, volume)
            VALUES(#{code}, #{date}, #{open}, #{high}, #{low}, #{close}, #{volume})
            ON DUPLICATE KEY UPDATE
              open = VALUES(open),
              high = VALUES(high),
              low = VALUES(low),
              close = VALUES(close),
              volume = VALUES(volume)
            """)
    void upsert(StockKline kline);
}
