package com.zhexuan.divination.service;

import com.zhexuan.divination.dto.ZiweiAnalyzeDTO;
import com.zhexuan.divination.vo.ZiweiAnalyzeVO;

public interface ZiweiAnalyzeService {
    ZiweiAnalyzeVO analyze(ZiweiAnalyzeDTO dto);
}
