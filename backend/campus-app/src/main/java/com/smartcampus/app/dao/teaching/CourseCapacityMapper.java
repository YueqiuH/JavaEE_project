package com.smartcampus.app.dao.teaching;

import com.smartcampus.contract.entity.CourseCapacity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Param;

public interface CourseCapacityMapper extends BaseMapper<CourseCapacity> {

    /** 漏洞14: 原子增加选课人数。返回受影响行数，0表示已满 */
    int incrementCount(@Param("capacityId") Long capacityId);

    /** 漏洞14: 原子减少选课人数。返回受影响行数，0表示已空 */
    int decrementCount(@Param("capacityId") Long capacityId);
}
