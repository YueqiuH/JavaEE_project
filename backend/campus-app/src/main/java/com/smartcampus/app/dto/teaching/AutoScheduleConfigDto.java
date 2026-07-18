package com.smartcampus.app.dto.teaching;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 自动排课策略配置 DTO —— 教务人员调控算法倾向的参数面板。
 */
@Data
public class AutoScheduleConfigDto {

    /** 学期 */
    private String semester;

    // ==================== 1. 约束权重 (0-100) ====================

    /** 教师期望时段尽量满足权重 */
    private int teacherPreferenceWeight = 80;

    /** 班级课表集中度优先权重 */
    private int classCompactnessWeight = 100;

    /** 晚上不安排核心必修课权重 */
    private int noEveningCoreWeight = 60;

    /** 课表均匀分布权重 */
    private int distributionUniformityWeight = 85;

    /** 学生空闲节最小化权重 */
    private int minimizeGapsWeight = 70;

    // ==================== 2. 锁定与排除 ====================

    /** 是否锁定所有已手动排定的课程 */
    private boolean lockExistingSchedules = true;

    /** 排除的节假日教学周列表 */
    private List<Integer> holidayWeeks;

    /** 指定不参与自动排课的课程ID列表 */
    private List<Long> excludedCourseIds;

    /** 指定不参与自动排课的教师ID列表 */
    private List<Long> excludedTeacherIds;

    // ==================== 3. 算法控制 ====================

    /** 最大迭代/回溯次数（默认 10000） */
    private int maxIterations = 10000;

    /** 是否启用自适应回溯置换 */
    private boolean enableBacktracking = true;

    /** 回溯最大深度（默认 5 层） */
    private int maxBacktrackDepth = 5;

    /** 是否在完成后自动发布排课结果 */
    private boolean autoPublish = false;

    /** 默认教师ID（当前操作用户，用于没有历史排课的课程） */
    private Long defaultTeacherId;

    // ==================== 4. 返回诊断报告 ====================
    // 这些字段由算法回填

    /** 排课结果详情 */
    private transient List<Map<String, Object>> results;

    /** 诊断报告 */
    private transient Map<String, Object> diagnostic;
}
