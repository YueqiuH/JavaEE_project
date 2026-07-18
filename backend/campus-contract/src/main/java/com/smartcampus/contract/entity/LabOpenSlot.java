package com.smartcampus.contract.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@ToString
@TableName("lab_open_slot")
public class LabOpenSlot implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long slotId;
    private Long labId;
    private LocalDate openDate;
    private Integer startPeriod;
    private Integer endPeriod;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
