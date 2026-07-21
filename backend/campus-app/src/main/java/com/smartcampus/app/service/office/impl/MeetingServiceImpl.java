package com.smartcampus.app.service.office.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartcampus.app.dao.office.MeetingMapper;
import com.smartcampus.app.service.office.IMeetingService;
import com.smartcampus.contract.entity.Meeting;
import org.springframework.stereotype.Service;

@Service
public class MeetingServiceImpl extends ServiceImpl<MeetingMapper, Meeting> implements IMeetingService {
}
