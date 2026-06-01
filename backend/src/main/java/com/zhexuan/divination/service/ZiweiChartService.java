package com.zhexuan.divination.service;

import com.zhexuan.divination.dto.ZiweiChartDTO;
import java.util.Map;

public interface ZiweiChartService {
    Map<String, Object> buildChart(ZiweiChartDTO dto);
}
