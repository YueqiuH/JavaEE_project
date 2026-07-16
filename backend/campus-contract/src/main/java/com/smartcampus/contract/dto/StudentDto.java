package com.smartcampus.contract.dto;

import com.smartcampus.contract.entity.StudentEntity;
import lombok.Data; import lombok.ToString;

@Data @ToString
public class StudentDto extends StudentEntity {
    private Integer pageNo;
    private Integer pageSize;
}
