package com.smartcampus.contract.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;
import java.io.Serializable;

@Data @ToString @TableName(value = "course_capacity")
public class CourseCapacity implements Serializable {
    @TableId(type = IdType.AUTO) private Long capacityId;
    private Long courseId;
    private String semester;
    private Integer maxCapacity;
    private Integer currentCount;
    private Integer minCapacity;
}
