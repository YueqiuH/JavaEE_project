package com.smartcampus.app.service.teaching.impl;

import com.smartcampus.app.dao.teaching.ScoreMapper;
import com.smartcampus.app.service.teaching.IScoreService;
import com.smartcampus.common.result.CommonResult;
import com.smartcampus.contract.entity.Score;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;

@Service
public class ScoreServiceImpl implements IScoreService {

    @Autowired private ScoreMapper scoreMapper;

    // ==================== 时间窗口 ====================

    @Override
    public int getCurrentWeek() {
        LocalDate now = LocalDate.now();
        int weekOfYear = now.get(WeekFields.of(Locale.getDefault()).weekOfYear());
        int month = now.getMonthValue();
        if (month >= 9 && month <= 12) {
            LocalDate sep1 = LocalDate.of(now.getYear(), 9, 1);
            int sep1Week = sep1.get(WeekFields.of(Locale.getDefault()).weekOfYear());
            return Math.max(1, weekOfYear - sep1Week + 1);
        } else if (month >= 2 && month <= 6) {
            LocalDate feb15 = LocalDate.of(now.getYear(), 2, 15);
            int feb15Week = feb15.get(WeekFields.of(Locale.getDefault()).weekOfYear());
            return Math.max(1, weekOfYear - feb15Week + 1);
        }
        return 21;
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

    // ==================== 教师录入 ====================

    @Override
    @Transactional
    public CommonResult inputScore(Score score) {
        int week = getCurrentWeek();
        if (week < 17) return CommonResult.error(930, "第" + week + "周，成绩录入已锁定");
        if (week > 20) return CommonResult.error(931, "已归档，请联系教务处");
        if (score.getStudentId() == null || score.getCourseId() == null)
            return CommonResult.error(920, "学生ID和课程ID不能为空");
        if (score.getScoreScore() == null || score.getScoreScore() < 0 || score.getScoreScore() > 100)
            return CommonResult.error(921, "分数范围0-100");

        BigDecimal gpa = calcGpa(score.getScoreScore());
        score.setGpa(gpa);
        score.setStatus(score.getScoreScore() >= 60 ? 1 : 0);

        LambdaQueryWrapper<Score> w = new LambdaQueryWrapper<>();
        w.eq(Score::getStudentId, score.getStudentId())
         .eq(Score::getCourseId, score.getCourseId())
         .eq(Score::getSemester, score.getSemester());
        Score exist = scoreMapper.selectOne(w);
        if (exist != null) {
            score.setScoreId(exist.getScoreId());
            scoreMapper.updateById(score);
        } else {
            scoreMapper.insert(score);
        }

        checkWarning(score.getStudentId(), score.getSemester());
        return CommonResult.success(Map.of("scoreId", score.getScoreId(), "gpa", gpa));
    }

    @Override
    public CommonResult saveDraft(Score score) {
        return inputScore(score);
    }

    @Override
    public CommonResult publishScores(Long scheduleId, Long teacherId, String semester) {
        return CommonResult.success(Map.of("published", 0));
    }

    // ==================== 学生查询 ====================

    @Override
    public CommonResult getStudentReport(Long studentId, String semester) {
        LambdaQueryWrapper<Score> w = new LambdaQueryWrapper<>();
        w.eq(Score::getStudentId, studentId);
        if (semester != null) w.eq(Score::getSemester, semester);
        List<Score> list = scoreMapper.selectList(w);

        int pass = 0, fail = 0;
        BigDecimal totalGpa = BigDecimal.ZERO;
        for (Score s : list) {
            if (s.getStatus() != null && s.getStatus() == 1) pass++; else fail++;
            if (s.getGpa() != null) totalGpa = totalGpa.add(s.getGpa());
        }
        BigDecimal gpaAvg = list.isEmpty() ? BigDecimal.ZERO
            : totalGpa.divide(new BigDecimal(list.size()), 2, RoundingMode.HALF_UP);

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("courses", list);
        report.put("total", list.size());
        report.put("pass", pass);
        report.put("fail", fail);
        report.put("gpa", gpaAvg);
        report.put("semester", semester);
        return CommonResult.success(report);
    }

    // ==================== 教师查询 ====================

    @Override
    public CommonResult getCourseScores(Long courseId, String semester) {
        LambdaQueryWrapper<Score> w = new LambdaQueryWrapper<>();
        w.eq(Score::getCourseId, courseId);
        if (semester != null) w.eq(Score::getSemester, semester);
        return CommonResult.success(scoreMapper.selectList(w));
    }

    @Override
    public CommonResult getTeacherClasses(Long teacherId, String semester) {
        return CommonResult.success(scoreMapper.selectTeacherClasses(teacherId, semester));
    }

    // ==================== 辅导员查询 ====================

    @Override
    public CommonResult getCounselorWarnings(Long counselorId, String semester) {
        return CommonResult.success(scoreMapper.selectCounselorWarnings(counselorId, semester));
    }

    @Override
    public CommonResult getStudentFullProfile(Long studentId) {
        LambdaQueryWrapper<Score> w = new LambdaQueryWrapper<>();
        w.eq(Score::getStudentId, studentId);
        List<Score> all = scoreMapper.selectList(w);

        int totalFail = 0, currentFail = 0;
        String currentSemester = getCurrentSemester();
        for (Score s : all) {
            if (s.getStatus() != null && s.getStatus() == 0) {
                totalFail++;
                if (currentSemester.equals(s.getSemester())) currentFail++;
            }
        }
        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("allScores", all);
        profile.put("totalFailCredits", totalFail * 2);
        profile.put("currentSemesterFailCredits", currentFail * 2);
        profile.put("warningLevel", totalFail >= 10 ? "red" : currentFail >= 3 ? "yellow" : "none");
        return CommonResult.success(profile);
    }

    // ==================== 管理员操作 ====================

    @Override
    @Transactional
    public CommonResult adminModifyScore(Long scoreId, Integer newScore, Long adminId, String reason, String docNo) {
        Score score = scoreMapper.selectById(scoreId);
        if (score == null) return CommonResult.error(932, "成绩记录不存在");
        int oldScore = score.getScoreScore() != null ? score.getScoreScore() : 0;
        score.setScoreScore(newScore);
        score.setGpa(calcGpa(newScore));
        score.setStatus(newScore >= 60 ? 1 : 0);
        scoreMapper.updateById(score);
        System.err.println("【管理员修改】scoreId=" + scoreId + " " + oldScore + "→" + newScore + " 公文号=" + docNo);
        return CommonResult.success(Map.of("scoreId", scoreId, "oldScore", oldScore, "newScore", newScore));
    }

    @Override
    public CommonResult getModificationLogs(Long scoreId) {
        return CommonResult.success(Collections.emptyList());
    }

    // ==================== 工具方法 ====================

    private String getCurrentSemester() {
        int month = LocalDate.now().getMonthValue();
        int year = LocalDate.now().getYear();
        return month >= 9 ? year + "-" + (year + 1) + "-1" : (year - 1) + "-" + year + "-2";
    }

    private void checkWarning(Long studentId, String semester) {
        int week = getCurrentWeek();
        if (week < 20) return;

        LambdaQueryWrapper<Score> w = new LambdaQueryWrapper<>();
        w.eq(Score::getStudentId, studentId).eq(Score::getStatus, 0);
        long total = scoreMapper.selectCount(w);

        LambdaQueryWrapper<Score> cur = new LambdaQueryWrapper<>();
        cur.eq(Score::getStudentId, studentId).eq(Score::getSemester, semester).eq(Score::getStatus, 0);
        long current = scoreMapper.selectCount(cur);

        int currentCredits = (int) current * 2;
        int totalCredits = (int) total * 2;
        String level = null;
        if (currentCredits >= 15 || totalCredits >= 20) {
            level = "🔴 红色预警：学期" + currentCredits + "学分，累计" + totalCredits + "学分";
        } else if (currentCredits >= 10) {
            level = "🟡 黄色预警：学期" + currentCredits + "学分";
        }
        if (level != null) System.err.println("【学业预警】学生" + studentId + " → " + level);
    }
}
