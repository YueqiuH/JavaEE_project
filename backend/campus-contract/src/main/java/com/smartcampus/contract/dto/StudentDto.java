package com.smartcampus.contract.dto;

import com.smartcampus.contract.entity.Student;
import lombok.Data; import lombok.EqualsAndHashCode; import lombok.ToString;

@Data @ToString
@EqualsAndHashCode(callSuper = true)
public class StudentDto extends Student {
    private Integer pageNo;
    private Integer pageSize;
}
