package com.example.quant.service;

import com.example.quant.entity.TradeStrategy;

import java.util.List;

public interface TradeStrategyService {
    List<TradeStrategy> list(String username);

    void save(TradeStrategy strategy);
}
