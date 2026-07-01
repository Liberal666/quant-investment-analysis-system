package com.example.quant.controller;

import com.example.quant.entity.TradeStrategy;
import com.example.quant.entity.SysUser;
import com.example.quant.service.AuthSessionService;
import com.example.quant.service.TradeStrategyService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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
    private final AuthSessionService authSessionService;

    public TradeStrategyController(TradeStrategyService service, AuthSessionService authSessionService) {
        this.service = service;
        this.authSessionService = authSessionService;
    }

    @GetMapping
    public List<TradeStrategy> list(@RequestHeader(value = "Authorization", required = false) String authorization) {
        SysUser user = authSessionService.requireUser(authorization);
        return service.list(user.getUsername());
    }

    @PostMapping
    public Map<String, String> save(@RequestHeader(value = "Authorization", required = false) String authorization,
                                    @RequestBody TradeStrategy strategy) {
        SysUser user = authSessionService.requireUser(authorization);
        strategy.setUsername(user.getUsername());
        service.save(strategy);
        return Map.of("message", "strategy saved");
    }
}
