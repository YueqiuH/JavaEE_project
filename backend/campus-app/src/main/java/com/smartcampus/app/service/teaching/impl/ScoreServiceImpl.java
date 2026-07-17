package com.smartcampus.app.service.teaching.impl;

import com.smartcampus.app.dao.teaching.ScoreMapper;
import com.smartcampus.app.service.teaching.IScoreService;
import com.smartcampus.common.result.CommonResult;
import com.smartcampus.contract.entity.ScoreEntity;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.*;

@Service
public class ScoreServiceImpl implements IScoreService {

    @Autowired private ScoreMapper scoreMapper;

    // ==================== 时间窗口 ====================

    /** 获取当前教学周（1-21+） */
    @Override
    public int getCurrentWeek() {
        // 假设春季学期从2月中旬开始，秋季从9月初开始
        // 简化实现：以当前日期计算
        LocalDate now = LocalDate.now();
        int weekOfYear = now.get(WeekFields.of(Locale.getDefault()).weekOfYear());
        // 秋季学期：9月第1周到次年1月 ≈ 第36周到次年第5周
        // 春季学期：2月中旬到6月底 ≈ 第8周到第26周
        // 简化：7月 ≈ 暑假后/归档期，9-12月 ≈ 秋季学期
        int month = now.getMonthValue();
        if (month >= 9 && month <= 12) {
            // 秋季学期: 9月第1周=week1
            LocalDate sep1 = LocalDate.of(now.getYear(), 9, 1);
            int sep1Week = sep1.get(WeekFields.of(Locale.getDefault()).weekOfYear());
            return Math.max(1, weekOfYear - sep1Week + 1);
        } else if (month >= 2 && month <= 6) {
            // 春季学期: 2月第3周=week1
            LocalDate feb15 = LocalDate.of(now.getYear(), 2, 15);
            int feb15Week = feb15.get(WeekFields.of(Locale.getDefault()).weekOfYear());
            return Math.max(1, weekOfYear - feb15Week + 1);
        }
        return 21; // 暑假/寒假 → 归档期
    }

    @Override
    public Map<String, Object> getTimeWindowStatus() {
        int week = getCurrentWeek();
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("currentWeek", week);
        status.put("phase", week <= 16 ? "locked" : week <= 19 ? "exam" : week <= 20 ? "buffer" : "archived");
        status.put("teacherCanEdit", week >= 17 && week <= 20);
        status.put("teacherCanPublish", week >= 17);
        status.put("studentCanView", week >= 17);
        status.put("studentCanReview", week == 20);
        status.put("adminOnly", week >= 21);
        return status;
    }

    // ==================== 绩点计算 ====================

    /** 百分制 → 4.0绩点: 90+→4.0, 85+→3.7, 82+→3.3, 78+→3.0, 75+→2.7, 72+→2.3, 68+→2.0, 64+→1.5, 60+→1.0, <60→0 */
    private BigDecimal calcGpa(int score) {
        if (score >= 90) return new BigDecimal("4.0");
        if (score >= 85) return new BigDecimal("3.7");
        if (score >= 82) return new BigDecimal("3.3");
        if (score >= 78) return new BigDecimal("3.0");
        if (score >= 75) return new BigDecimal("2.7");
        if (score >= 72) return new BigDecimal("2.3");
        if (score >= 68) return new BigDecimal("2.0");
        if (score >= 64) return new BigDecimal("1.5");
        if (score >= 60) return new BigDecimal("1.0");
        return BigDecimal.ZERO;
    }

    /** 计算总评成绩 = round(regular*ratio + exam*ratio) */
    private int calcTotal(int regular, int exam, BigDecimal regRatio, BigDecimal examRatio) {
        if (regRatio == null) regRatio = new BigDecimal("0.3");
        if (examRatio == null) examRatio = new BigDecimal("0.7");
        return regRatio.multiply(new BigDecimal(regular))
                .add(examRatio.multiply(new BigDecimal(exam)))
                .setScale(0, RoundingMode.HALF_UP).intValue();
    }

    // ==================== 教师录入 ====================

    @Override
    @Transactional
    public CommonResult inputScore(ScoreEntity score) {
        int week = getCurrentWeek();
        if (week < 17) return CommonResult.error(930, "当前为第" + week + "周（日常授课期），成绩录入功能已锁定。请在第17周后操作。");
        if (week > 20 && score.getPublishStatus() == null) {
            return CommonResult.error(931, "当前为第" + week + "周（归档校对期），教师无法直接修改成绩。请联系教务处管理员。");
        }

        if (score.getStudentId() == null || score.getCourseId() == null)
            return CommonResult.error(920, "学生ID和课程ID不能为空");

        // 计算总评
        if (score.getRegularScore() != null && score.getExamScore() != null) {
            int total = calcTotal(score.getRegularScore(), score.getExamScore(),
                    score.getRegularRatio(), score.getExamRatio());
            if (total < 0 || total > 100) return CommonResult.error(921, "分数范围必须在0-100之间");
            score.setScoreScore(total);
        } else if (score.getScoreScore() != null) {
            if (score.getScoreScore() < 0 || score.getScoreScore() > 100)
                return CommonResult.error(921, "分数范围必须在0-100之间");
        }

        // 计算绩点
        BigDecimal gpa = calcGpa(score.getScoreScore() != null ? score.getScoreScore() : 0);
        score.setGpa(gpa);
        score.setStatus(score.getScoreScore() != null && score.getScoreScore() >= 60 ? 1 : 0);

        if (score.getPublishStatus() == null) score.setPublishStatus(2); // 默认发布
        score.setModifiedTime(LocalDateTime.now());

        // 有则更新，无则新增
        LambdaQueryWrapper<ScoreEntity> w = new LambdaQueryWrapper<>();
        w.eq(ScoreEntity::getStudentId, score.getStudentId())
         .eq(ScoreEntity::getCourseId, score.getCourseId())
         .eq(ScoreEntity::getSemester, score.getSemester());
        ScoreEntity exist = scoreMapper.selectOne(w);
        if (exist != null) {
            score.setScoreId(exist.getScoreId());
            scoreMapper.updateById(score);
        } else {
            scoreMapper.insert(score);
        }

        System.out.println("成绩录入: 学生=" + score.getStudentId() + " 课程=" + score.getCourseId()
                + " 总分=" + score.getScoreScore() + " 绩点=" + gpa + " 发布状态=" + score.getPublishStatus());
        return CommonResult.success(Map.of("scoreId", score.getScoreId(), "total", score.getScoreScore(), "gpa", gpa));
    }

    @Override
    @Transactional
    public CommonResult saveDraft(ScoreEntity score) {
        score.setPublishStatus(1); // 草稿
        return inputScore(score);
    }

    @Override
    @Transactional
    public CommonResult publishScores(Long scheduleId, Long teacherId, String semester) {
        int week = getCurrentWeek();
        if (week < 17) return CommonResult.error(930, "成绩录入期尚未开始");

        LambdaQueryWrapper<ScoreEntity> w = new LambdaQueryWrapper<>();
        w.eq(ScoreEntity::getScheduleId, scheduleId)
         .eq(ScoreEntity::getSemester, semester)
         .eq(ScoreEntity::getPublishStatus, 1); // 仅草稿
        List<ScoreEntity> drafts = scoreMapper.selectList(w);
        for (ScoreEntity d : drafts) {
            d.setPublishStatus(2);
            d.setPublishTime(LocalDateTime.now());
            scoreMapper.updateById(d);
        }
        System.out.println("批量发布: scheduleId=" + scheduleId + " 发布数=" + drafts.size());
        return CommonResult.success(Map.of("published", drafts.size()));
    }

    // ==================== 学生查询 ====================

    @Override
    public CommonResult getStudentReport(Long studentId, String semester) {
        LambdaQueryWrapper<ScoreEntity> w = new LambdaQueryWrapper<>();
        w.eq(ScoreEntity::getStudentId, studentId);
        if (semester != null) w.eq(ScoreEntity::getSemester, semester);
        w.eq(ScoreEntity::getPublishStatus, 2); // 仅已发布
        List<ScoreEntity> list = scoreMapper.selectList(w);

        // 重修覆盖：同一 courseId 保留最新通过的
        Map<Long, ScoreEntity> latestByCourse = new LinkedHashMap<>();
        for (ScoreEntity s : list) {
            Long cid = s.getCourseId();
            ScoreEntity existing = latestByCourse.get(cid);
            if (existing == null || (s.getStatus() == 1 && existing.getStatus() == 0)
                    || (s.getModifiedTime() != null && existing.getModifiedTime() != null
                        && s.getModifiedTime().isAfter(existing.getModifiedTime()))) {
                latestByCourse.put(cid, s);
            }
        }
        List<ScoreEntity> effective = new ArrayList<>(latestByCourse.values());

        int pass = 0, fail = 0, totalCredits = 0;
        BigDecimal totalWeightedGpa = BigDecimal.ZERO;
        for (ScoreEntity s : effective) {
            if (s.getStatus() == 1) pass++; else fail++;
            BigDecimal gpa = s.getGpa() != null ? s.getGpa() : BigDecimal.ZERO;
            // 学分从关联的 course 表获取（此处简化，假设 score 表有 credit 字段或通过 join）
            int credit = 2; // 默认2学分
            totalWeightedGpa = totalWeightedGpa.add(gpa.multiply(new BigDecimal(credit)));
            totalCredits += credit;
        }

        BigDecimal gpaAvg = totalCredits > 0
                ? totalWeightedGpa.divide(new BigDecimal(totalCredits), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("courses", effective);
        report.put("total", effective.size());
        report.put("pass", pass);
        report.put("fail", fail);
        report.put("totalCredits", totalCredits);
        report.put("gpa", gpaAvg);
        report.put("semester", semester);
        return CommonResult.success(report);
    }

    // ==================== 教师查询 ====================

    @Override
    public CommonResult getCourseScores(Long courseId, String semester) {
        LambdaQueryWrapper<ScoreEntity> w = new LambdaQueryWrapper<>();
        w.eq(ScoreEntity::getCourseId, courseId);
        if (semester != null) w.eq(ScoreEntity::getSemester, semester);
        return CommonResult.success(scoreMapper.selectList(w));
    }

    @Override
    public CommonResult getTeacherClasses(Long teacherId, String semester) {
        // 返回教师执教的 schedule 列表（含课程信息）
        List<Map<String, Object>> classes = scoreMapper.selectTeacherClasses(teacherId, semester);
        return CommonResult.success(classes);
    }

    // ==================== 辅导员查询 ====================

    @Override
    public CommonResult getCounselorWarnings(Long counselorId, String semester) {
        // 获取辅导员所辖班级学生的学业预警
        List<Map<String, Object>> warnings = scoreMapper.selectCounselorWarnings(counselorId, semester);
        return CommonResult.success(warnings);
    }

    @Override
    public CommonResult getStudentFullProfile(Long studentId) {
        LambdaQueryWrapper<ScoreEntity> w = new LambdaQueryWrapper<>();
        w.eq(ScoreEntity::getStudentId, studentId);
        // 包含所有状态（草稿+发布+归档）
        List<ScoreEntity> all = scoreMapper.selectList(w);

        int totalFail = 0, currentFail = 0;
        String currentSemester = getCurrentSemester();
        for (ScoreEntity s : all) {
            if (s.getStatus() == 0) {
                totalFail++;
                if (currentSemester.equals(s.getSemester())) currentFail++;
            }
        }

        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("allScores", all);
        profile.put("totalFailCredits", totalFail * 2); // 估算
        profile.put("currentSemesterFailCredits", currentFail * 2);
        profile.put("warningLevel", totalFail >= 10 ? "red" : currentFail >= 3 ? "yellow" : "none");
        return CommonResult.success(profile);
    }

    // ==================== 管理员操作 ====================

    @Override
    @Transactional
    public CommonResult adminModifyScore(Long scoreId, Integer newScore, Long adminId, String reason, String docNo) {
        ScoreEntity score = scoreMapper.selectById(scoreId);
        if (score == null) return CommonResult.error(932, "成绩记录不存在");

        int oldScore = score.getScoreScore() != null ? score.getScoreScore() : 0;
        score.setScoreScore(newScore);
        score.setGpa(calcGpa(newScore));
        score.setStatus(newScore >= 60 ? 1 : 0);
        score.setModifiedTime(LocalDateTime.now());
        scoreMapper.updateById(score);

        // 记录修改日志
        System.err.println("【管理员修改日志】scoreId=" + scoreId
                + " adminId=" + adminId + " 原分数=" + oldScore + " 新分数=" + newScore
                + " 公文号=" + docNo + " 缘由=" + reason);

        return CommonResult.success(Map.of("scoreId", scoreId, "oldScore", oldScore, "newScore", newScore));
    }

    @Override
    public CommonResult getModificationLogs(Long scoreId) {
        // 简化为返回模拟数据；实际应查询 modification_log 表
        return CommonResult.success(Collections.emptyList());
    }

    // ==================== 工具方法 ====================

    private String getCurrentSemester() {
        int month = LocalDate.now().getMonthValue();
        int year = LocalDate.now().getYear();
        return month >= 9 ? year + "-" + (year + 1) + "-1" : (year - 1) + "-" + year + "-2";
    }
}
