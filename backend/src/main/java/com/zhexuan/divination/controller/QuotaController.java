package com.zhexuan.divination.controller;

import com.zhexuan.divination.common.Result;
import com.zhexuan.divination.dto.RechargeDTO;
import com.zhexuan.divination.service.QuotaService;
import com.zhexuan.divination.vo.PayOrderVO;
import com.zhexuan.divination.vo.QuotaVO;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/quota")
public class QuotaController {
    private final QuotaService quotaService;

    public QuotaController(QuotaService quotaService) {
        this.quotaService = quotaService;
    }

    @GetMapping
    public Result<QuotaVO> getQuota(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(quotaService.getQuota(userId));
    }

    @PostMapping("/recharge")
    public Result<QuotaVO> recharge(@RequestBody RechargeDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        dto.setUserId(userId);
        return Result.ok(quotaService.recharge(dto));
    }

    @PostMapping("/alipay/create")
    public Result<PayOrderVO> createAlipayOrder(@RequestBody RechargeDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        dto.setUserId(userId);
        return Result.ok(quotaService.createAlipayOrder(dto));
    }

    @PostMapping("/alipay/notify")
    public String alipayNotify(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (values != null && values.length > 0) {
                params.put(key, values[0]);
            }
        });
        return quotaService.handleAlipayNotify(params);
    }
}
