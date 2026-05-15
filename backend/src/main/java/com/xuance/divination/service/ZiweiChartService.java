package com.xuance.divination.service;

import com.xuance.divination.dto.ZiweiChartDTO;
import java.util.Map;

public interface ZiweiChartService {
    Map<String, Object> buildChart(ZiweiChartDTO dto);
}
