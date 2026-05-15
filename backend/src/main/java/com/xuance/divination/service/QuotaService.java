package com.xuance.divination.service;

import com.xuance.divination.dto.RechargeDTO;
import com.xuance.divination.vo.PayOrderVO;
import com.xuance.divination.vo.QuotaVO;
import java.util.Map;

public interface QuotaService {
    QuotaVO getQuota(Long userId);
    QuotaVO recharge(RechargeDTO dto);
    void consumeForAnalysis(Long userId, String type);
    PayOrderVO createAlipayOrder(RechargeDTO dto);
    String handleAlipayNotify(Map<String, String> params);
}
