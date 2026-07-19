package com.smartcampus.app.service.base;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartcampus.common.result.PageParam;
import com.smartcampus.contract.dto.MajorSaveRequest;
import com.smartcampus.contract.entity.Major;
import com.smartcampus.contract.vo.MajorVo;

public interface MajorService {

    IPage<MajorVo> pageVo(PageParam pageParam, Long deptId, String keyword);

    Major create(MajorSaveRequest request);

    Major update(Long majorId, MajorSaveRequest request);

    void delete(Long majorId);
}
