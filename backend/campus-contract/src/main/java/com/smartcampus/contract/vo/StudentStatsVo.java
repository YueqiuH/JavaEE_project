package com.smartcampus.contract.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * D3 学生特征多维统计聚合视图。
 * 穿透规则：未选院系时 byGroup 按院系分组；选了院系按专业分组；选了专业按班级分组。
 */
@Data
public class StudentStatsVo implements Serializable {

    /** 当前筛选范围内的在读学生总数 */
    private Long total;

    /** 穿透分组统计（院系 → 专业 → 班级） */
    private List<NameValueVo> byGroup;

    /** 分组维度：dept / major / class */
    private String groupBy;

    /** 按入学年份(年级)分布 */
    private List<NameValueVo> byEnrollYear;

    /** 性别分布 */
    private List<NameValueVo> byGender;

    /** 生源地分布 */
    private List<NameValueVo> byOrigin;

    /** 学籍状态分布 */
    private List<NameValueVo> byStatus;

    /** 选课偏好 Top10（读取教务选课数据） */
    private List<NameValueVo> coursePreference;
}
