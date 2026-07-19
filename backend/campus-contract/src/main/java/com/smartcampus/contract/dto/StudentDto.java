package com.smartcampus.contract.dto;

import com.smartcampus.contract.entity.StudentEntity;
import lombok.Data; import lombok.EqualsAndHashCode; import lombok.ToString;

@Data @ToString
@EqualsAndHashCode(callSuper = true)
public class StudentDto extends StudentEntity {
    private Integer pageNo;
    private Integer pageSize;
}
