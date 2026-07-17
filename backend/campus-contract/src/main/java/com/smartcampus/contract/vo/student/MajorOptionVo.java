package com.smartcampus.contract.vo.student;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MajorOptionVo {
    private Long majorId;
    private String majorName;
    private String majorCode;
}
