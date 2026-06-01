package com.zhexuan.divination.service;

import com.zhexuan.divination.common.PageResult;
import com.zhexuan.divination.entity.DivinationRecord;
import com.zhexuan.divination.vo.RecordVO;
import java.util.List;

public interface RecordService {
    PageResult<RecordVO> list(Long userId, String type, String keyword, long page, long size);
    PageResult<RecordVO> listAll(Long adminUserId, String type, String keyword, long page, long size);
    RecordVO get(Long id);
    void delete(Long id);
}
