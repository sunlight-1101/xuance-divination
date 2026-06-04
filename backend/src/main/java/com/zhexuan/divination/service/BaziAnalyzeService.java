package com.zhexuan.divination.service;

import com.zhexuan.divination.dto.BaziAnalyzeDTO;
import com.zhexuan.divination.dto.BaziCompatibilityDTO;
import com.zhexuan.divination.dto.BaziZiweiHeCanDTO;
import com.zhexuan.divination.vo.BaziAnalyzeVO;

public interface BaziAnalyzeService {
    BaziAnalyzeVO analyze(BaziAnalyzeDTO dto);
    BaziAnalyzeVO analyzeCompatibility(BaziCompatibilityDTO dto);
    BaziAnalyzeVO analyzeHeCan(BaziZiweiHeCanDTO dto);
}
