package com.smartcampus.app.service.office.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartcampus.app.dao.office.MeetingAttendeeMapper;
import com.smartcampus.app.service.office.IMeetingAttendeeService;
import com.smartcampus.contract.entity.MeetingAttendee;
import org.springframework.stereotype.Service;

@Service
public class MeetingAttendeeServiceImpl extends ServiceImpl<MeetingAttendeeMapper, MeetingAttendee> implements IMeetingAttendeeService {
}
