package com.smartcampus.contract.dto.student;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ScholarshipResultRequest {

    @NotEmpty(message = "请至少选择一项已通过的申请")
    @Size(max = 100, message = "单次最多生成100项资助结果")
    private List<Long> applicationIds;
}
