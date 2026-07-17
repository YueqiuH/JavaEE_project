package com.smartcampus.app.controller.teaching;

import com.smartcampus.app.dao.teaching.GraduationTopicMapper;
import com.smartcampus.app.dao.teaching.GraduationSelectionMapper;
import com.smartcampus.app.dao.teaching.GraduationReportMapper;
import com.smartcampus.common.result.CommonResult;
import com.smartcampus.contract.entity.GraduationReport;
import com.smartcampus.contract.entity.GraduationSelection;
import com.smartcampus.contract.entity.GraduationTopic;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/teaching/graduation")
@Tag(name = "毕业设计", description = "课题发布、选题、报告提交")
public class GraduationController {

    @Autowired private GraduationTopicMapper topicMapper;
    @Autowired private GraduationSelectionMapper selectionMapper;
    @Autowired private GraduationReportMapper reportMapper;

    @PostMapping("/topic/create")
    @Operation(summary = "教师发布课题")
    public CommonResult createTopic(@RequestBody GraduationTopic topic) {
        topic.setCurrentStudent(0);
        topic.setStatus(1);
        topicMapper.insert(topic);
        return CommonResult.success();
    }

    @GetMapping("/topic/list")
    @Operation(summary = "课题列表")
    public CommonResult listTopics(@RequestParam String semester) {
        LambdaQueryWrapper<GraduationTopic> w = new LambdaQueryWrapper<>();
        w.eq(GraduationTopic::getSemester, semester);
        return CommonResult.success(topicMapper.selectList(w));
    }

    @PostMapping("/select")
    @Operation(summary = "学生选题")
    public CommonResult selectTopic(@RequestBody GraduationSelection sel) {
        GraduationTopic topic = topicMapper.selectById(sel.getTopicId());
        if (topic == null) return CommonResult.error(940, "课题不存在");
        if (topic.getStatus() != 1) return CommonResult.error(941, "该课题当前不可选");
        if (topic.getCurrentStudent() >= topic.getMaxStudent())
            return CommonResult.error(942, "课题名额已满");
        sel.setStatus(0);
        sel.setSelectTime(new Date());
        selectionMapper.insert(sel);
        topic.setCurrentStudent(topic.getCurrentStudent() + 1);
        if (topic.getCurrentStudent() >= topic.getMaxStudent()) topic.setStatus(0);
        topicMapper.updateById(topic);
        return CommonResult.success();
    }

    @PostMapping("/report/submit")
    @Operation(summary = "提交报告")
    public CommonResult submitReport(@RequestBody GraduationReport report) {
        report.setSubmitTime(new Date());
        reportMapper.insert(report);
        return CommonResult.success();
    }

    @GetMapping("/report/list/{studentId}")
    @Operation(summary = "学生报告列表")
    public CommonResult listReports(@PathVariable Long studentId) {
        LambdaQueryWrapper<GraduationReport> w = new LambdaQueryWrapper<>();
        w.eq(GraduationReport::getStudentId, studentId);
        return CommonResult.success(reportMapper.selectList(w));
    }
}
