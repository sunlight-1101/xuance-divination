package com.xuance.divination.service;

import com.xuance.divination.dto.BaziAnalyzeDTO;
import com.xuance.divination.dto.BaziCompatibilityDTO;
import com.xuance.divination.vo.BaziAnalyzeVO;

public interface BaziAnalyzeService {
    BaziAnalyzeVO analyze(BaziAnalyzeDTO dto);
    BaziAnalyzeVO analyzeCompatibility(BaziCompatibilityDTO dto);
}
