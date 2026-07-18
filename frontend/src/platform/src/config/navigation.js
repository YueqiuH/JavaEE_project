export const domains = [
  { key: 'teaching', label: '教务教学', icon: 'Reading', color: '#3973b7', softColor: '#eaf2fb', description: '课程、成绩、考试与培养过程' },
  { key: 'student', label: '学生事务', icon: 'User', color: '#16865b', softColor: '#e8f5ef', description: '学籍、资助、评教与实践服务' },
  { key: 'office', label: '协同办公', icon: 'Briefcase', color: '#c77800', softColor: '#fff4df', description: '财务、资产、公文与会议协同' },
  { key: 'base', label: '基础数据', icon: 'DataAnalysis', color: '#8a4d8f', softColor: '#f5ecf5', description: '师生档案、统计分析与内容服务' },
]

export const services = [
  // userType: 1=学生, 2=辅导员, 3=教职工, 4=教务处
  { key: 'course-schedule', routeName: 'courseSchedule', title: '排课与课表', description: '查看课程安排、教室与调课信息', domain: 'teaching', icon: 'Calendar', template: 'schedule', summary: '今日 4 节课程', status: '今日', roles: [1,2,3,4] },
  { key: 'course-selection', routeName: 'courseSelection', title: '选课与容量', description: '课程检索、容量控制与选课办理', domain: 'teaching', icon: 'Select', template: 'transaction', summary: '第二轮选课进行中', status: '开放中', roles: [1,3,4] },
  { key: 'score-management', routeName: 'scoreManagement', title: '成绩评定与预警', description: '成绩录入、绩点计算与学业预警', domain: 'teaching', icon: 'TrendCharts', template: 'table', summary: '8 条待关注预警', status: '待处理', roles: [1,2,3,4] },
  { key: 'exam-arrangement', routeName: 'examArrangement', title: '考试与补考', description: '考试安排、监考与补考报名', domain: 'teaching', icon: 'Tickets', template: 'schedule', summary: '下场考试 7月18日', status: '临近', roles: [1,3,4] },
  { key: 'ai-learning', routeName: 'aiLearning', title: 'AI 智能学习助理', description: '材料总结、复习题与学习路径建议', domain: 'teaching', icon: 'MagicStick', template: 'ai', summary: '基于课程材料智能辅学', status: '演示模式', ai: true, roles: [1,2,3,4] },

  { key: 'student-status', routeName: 'studentStatus', title: '学籍变动', description: '休复学、转专业等申请与审核', domain: 'student', icon: 'Refresh', template: 'approval', summary: '1 项申请审核中', status: '审核中', roles: [1,2,4] },
  { key: 'scholarship', routeName: 'scholarship', title: '奖助贷评审', description: '资助申请、材料审核与名额评定', domain: 'student', icon: 'Medal', template: 'approval', summary: '本学年申请已开放', status: '开放中', roles: [1,2,4] },
  { key: 'teaching-evaluation', routeName: 'teachingEvaluation', title: '评教反馈', description: '课程评教、反馈与质量分析', domain: 'student', icon: 'ChatLineSquare', template: 'transaction', summary: '2 门课程待评教', status: '待完成', roles: [1,4] },
  { key: 'competition', routeName: 'competition', title: '学科竞赛', description: '竞赛发现、组队报名与成果管理', domain: 'student', icon: 'Trophy', template: 'transaction', summary: '6 项竞赛报名中', status: '报名中', roles: [1,3,4] },
  { key: 'lab-booking', routeName: 'labBooking', title: '实验室预约', description: '实验室当日容量预约', domain: 'student', icon: 'OfficeBuilding', template: 'schedule', summary: '按今日容量开放', status: '可预约', roles: [1,3,4] },
  { key: 'ai-psychology', routeName: 'aiPsychology', title: 'AI 心理预警', description: '心理陪伴、问卷分析与分级预警', domain: 'student', icon: 'Sunny', template: 'ai', summary: '隐私保护的智能关怀服务', status: '演示模式', ai: true, roles: [1,2,4] },

  { key: 'fee-payment', routeName: 'feePayment', title: '学杂费交纳', description: '账单查询、在线缴费与流水查看', domain: 'office', icon: 'Wallet', template: 'transaction', summary: '当前无待缴账单', status: '已完成', roles: [1,3,4] },
  { key: 'asset-management', routeName: 'assetManagement', title: '固定资产管理', description: '资产台账、申领、盘点与处置', domain: 'office', icon: 'Box', template: 'table', summary: '5 项资产待盘点', status: '待处理', roles: [3,4] },
  { key: 'work-plan', routeName: 'workPlan', title: '工作计划', description: '计划制定、任务协同与进度跟踪', domain: 'office', icon: 'Checked', template: 'schedule', summary: '今日 3 项任务', status: '进行中', roles: [2,3,4] },
  { key: 'document-oa', routeName: 'documentOA', title: '公文流转 OA', description: '收发文、会签、审批与催办', domain: 'office', icon: 'Document', template: 'approval', summary: '4 份公文待处理', status: '待办', roles: [2,3,4] },
  { key: 'meeting-notice', routeName: 'meetingNotice', title: '会议与通知', description: '会议安排、通知发布与反馈', domain: 'office', icon: 'Bell', template: 'schedule', summary: '14:30 教学工作会', status: '今日', roles: [2,3,4] },
  { key: 'ai-approval', routeName: 'aiApproval', title: 'AI 审批助手', description: '公文摘要、要点提取与意见草稿', domain: 'office', icon: 'MagicStick', template: 'ai', summary: '为审批工作提供智能建议', status: '演示模式', ai: true, roles: [2,3,4] },

  { key: 'user-management', routeName: 'userManagement', title: '师生信息库', description: '师生数字档案与组合检索', domain: 'base', icon: 'UserFilled', template: 'table', summary: '数据更新于 10 分钟前', status: '已同步', roles: [3,4] },
  { key: 'enrollment-stats', routeName: 'enrollmentStats', title: '招生统计', description: '招生计划、报到率与生源分析', domain: 'base', icon: 'Histogram', template: 'analytics', summary: '2026 年招生数据', status: '实时', roles: [4] },
  { key: 'student-analytics', routeName: 'studentAnalytics', title: '学生多维统计', description: '院系、年级与学生特征分析', domain: 'base', icon: 'PieChart', template: 'analytics', summary: '支持多维筛选与下钻', status: '实时', roles: [3,4] },
  { key: 'department-major', routeName: 'departmentMajor', title: '院系专业管理', description: '院系、专业与培养资源维护', domain: 'base', icon: 'Management', template: 'table', summary: '18 个院系 · 64 个专业', status: '已同步', roles: [3,4] },
  { key: 'news-forum', routeName: 'newsForum', title: '新闻与论坛', description: '新闻公告发布与校园交流', domain: 'base', icon: 'ChatDotSquare', template: 'table', summary: '7 条内容待审核', status: '待处理', roles: [3,4] },
  { key: 'ai-report', routeName: 'aiReport', title: 'AI 智能报表', description: '自然语言查询与图表生成', domain: 'base', icon: 'DataLine', template: 'ai', summary: '用自然语言探索校园数据', status: '演示模式', ai: true, roles: [3,4] },
]

export const domainMap = Object.fromEntries(domains.map((domain) => [domain.key, domain]))
export const serviceMap = Object.fromEntries(services.map((service) => [service.key, service]))

/** @param {number|null} userType 1=学生,2=辅导员,3=教职工,4=教务处 */
export const getServicesByDomain = (domain, userType = null) => {
  return services.filter(s => {
    if (s.domain !== domain) return false
    if (userType != null && s.roles && !s.roles.includes(userType)) return false
    return true
  })
}
export const getServiceByRouteName = (routeName) => services.find((service) => service.routeName === routeName)
