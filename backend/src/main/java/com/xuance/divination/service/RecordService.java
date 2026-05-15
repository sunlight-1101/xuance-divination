package com.xuance.divination.service;

import com.xuance.divination.entity.DivinationRecord;
import com.xuance.divination.vo.RecordVO;
import java.util.List;

public interface RecordService {
    List<RecordVO> list(Long userId, String type, String keyword);
    RecordVO get(Long id);
    void delete(Long id);
}
