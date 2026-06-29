package com.example.quant.controller;

import com.example.quant.entity.TradeStrategy;
import com.example.quant.service.TradeStrategyService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/api/strategies")
public class TradeStrategyController {
    private final TradeStrategyService service;

    public TradeStrategyController(TradeStrategyService service) {
        this.service = service;
    }

    @GetMapping
    public List<TradeStrategy> list(@RequestParam(defaultValue = "viewer") String username) {
        return service.list(username);
    }

    @PostMapping
    public Map<String, String> save(@RequestBody TradeStrategy strategy) {
        service.save(strategy);
        return Map.of("message", "strategy saved");
    }
}
