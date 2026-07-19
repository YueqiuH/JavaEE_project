package com.smartcampus.contract.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 通用「名称-数量」统计项（饼图/柱状图数据源）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NameValueVo implements Serializable {

    private String name;

    private Long value;
}
