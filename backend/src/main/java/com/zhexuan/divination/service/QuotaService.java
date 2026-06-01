package com.zhexuan.divination.service;

import com.zhexuan.divination.dto.RechargeDTO;
import com.zhexuan.divination.vo.PayOrderVO;
import com.zhexuan.divination.vo.QuotaVO;
import java.util.Map;

public interface QuotaService {
    QuotaVO getQuota(Long userId);
    QuotaVO recharge(RechargeDTO dto);
    void consumeForAnalysis(Long userId, String type);
    PayOrderVO createAlipayOrder(RechargeDTO dto);
    String handleAlipayNotify(Map<String, String> params);
}
