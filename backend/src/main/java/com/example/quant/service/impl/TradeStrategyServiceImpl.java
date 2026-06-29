package com.example.quant.service.impl;

import com.example.quant.entity.TradeStrategy;
import com.example.quant.mapper.TradeStrategyMapper;
import com.example.quant.service.TradeStrategyService;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
public class TradeStrategyServiceImpl implements TradeStrategyService {
    private final TradeStrategyMapper mapper;

    public TradeStrategyServiceImpl(TradeStrategyMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<TradeStrategy> list(String username) {
        return mapper.findByUsername(username);
    }

    @Override
    public void save(TradeStrategy strategy) {
        if (strategy.getCode() == null || !strategy.getCode().matches("\\d{6}")) {
            throw new ResponseStatusException(BAD_REQUEST, "股票代码必须是6位数字");
        }
        if (strategy.getContent() == null || strategy.getContent().isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "策略信息不能为空");
        }
        mapper.insert(strategy);
    }
}
