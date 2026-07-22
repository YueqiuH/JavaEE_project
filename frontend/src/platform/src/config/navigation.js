export const domains = [
  { key: 'teaching', label: '教务教学', icon: 'Reading', color: '#3973b7', softColor: '#eaf2fb', description: '课程、成绩、考试与培养过程' },
  { key: 'student', label: '学生事务', icon: 'User', color: '#16865b', softColor: '#e8f5ef', description: '学籍、资助、评教与实践服务' },
  { key: 'office', label: '协同办公', icon: 'Briefcase', color: '#c77800', softColor: '#fff4df', description: '财务、资产、公文与会议协同' },
  { key: 'base', label: '基础数据', icon: 'DataAnalysis', color: '#8a4d8f', softColor: '#f5ecf5', description: '师生档案、统计分析与内容服务' },
]

export const services = [
  // userType: 1=学生, 2=辅导员, 3=教职工, 4=教务处
  { key: 'course-schedule', routeName: 'courseSchedule', title: '排课与课表', description: '查看课程安排、教室与调课信息', domain: 'teaching', icon: 'Calendar', template: 'schedule', summary: '课程时间与教室一览', status: '进行中', roles: [1,2,3,4] },
  { key: 'course-selection', routeName: 'courseSelection', title: '选课与容量', description: '课程检索、容量控制与选课办理', domain: 'teaching', icon: 'Select', template: 'transaction', summary: '课程检索与在线选课', status: '进行中', roles: [1,3,4] },
  { key: 'score-management', routeName: 'scoreManagement', title: '成绩评定与预警', description: '成绩录入、绩点计算与学业预警', domain: 'teaching', icon: 'TrendCharts', template: 'table', summary: '成绩录入与学业分析', status: '进行中', roles: [1,2,3,4] },
  { key: 'exam-arrangement', routeName: 'examArrangement', title: '考试与补考', description: '考试安排、监考与补考报名', domain: 'teaching', icon: 'Tickets', template: 'schedule', summary: '考试日程与补考报名', status: '进行中', roles: [1,3,4] },

  { key: 'ai-learning', routeName: 'aiLearning', title: 'AI 智能学习助理', description: '智能排课、考试编排、数据查询与学习辅导', domain: 'teaching', icon: 'MagicStick', template: 'ai', summary: '校园智能助手，支持排课/考试/查询/学习', status: '在线', ai: true, roles: [1,2,3,4] },

  { key: 'student-status', routeName: 'studentStatus', title: '学籍变动', description: '休复学、转专业等申请与审核', domain: 'student', icon: 'Refresh', template: 'approval', summary: '休复学及转专业办理', status: '进行中' },
  { key: 'scholarship', routeName: 'scholarship', title: '奖助贷评审', description: '资助申请、材料审核与名额评定', domain: 'student', icon: 'Medal', template: 'approval', summary: '资助申请与审核评定', status: '进行中' },
  { key: 'teaching-evaluation', routeName: 'teachingEvaluation', title: '评教反馈', description: '课程评教、反馈与质量分析', domain: 'student', icon: 'ChatLineSquare', template: 'transaction', summary: '课程评教与反馈', status: '进行中' },
  { key: 'competition', routeName: 'competition', title: '学科竞赛', description: '竞赛发现、组队报名与成果管理', domain: 'student', icon: 'Trophy', template: 'transaction', summary: '竞赛发布与组队报名', status: '进行中' },
  { key: 'lab-booking', routeName: 'labBooking', title: '实验室预约', description: '实验室当日容量预约', domain: 'student', icon: 'OfficeBuilding', template: 'schedule', summary: '实验室资源预约', status: '可预约' },

  { key: 'fee-payment', routeName: 'feePayment', title: '学杂费交纳', description: '账单查询、在线缴费与学生缴费概览', domain: 'office', icon: 'Wallet', template: 'transaction', summary: '账单查询与在线缴费', status: '进行中', permissions: ['fee:self:read', 'fee:overview:read', 'fee:manage'] },
  { key: 'asset-management', routeName: 'assetManagement', title: '固定资产审批', description: '购买、添加、借用、损坏报废申请与管理员审批', domain: 'office', icon: 'Box', template: 'table', summary: '购置借用与报废审批', status: '进行中', permissions: ['asset:read'] },
  { key: 'work-plan', routeName: 'workPlan', title: '工作计划与勤工俭学', description: '个人计划、学生勤工俭学与工资结算', domain: 'office', icon: 'Checked', template: 'schedule', summary: '个人计划与勤工俭学', status: '进行中', permissions: ['work-plan:self', 'work-plan:manage'] },
  { key: 'document-oa', routeName: 'documentOA', title: '公文流转 OA', description: '学生请假、公文会签、审批与催办', domain: 'office', icon: 'Document', template: 'approval', summary: '公文审批与流程管理', status: '进行中', permissions: ['document:self', 'document:approve'] },
  { key: 'meeting-notice', routeName: 'meetingNotice', title: '会议与通知', description: '会议安排、通知发布与反馈', domain: 'office', icon: 'Bell', template: 'schedule', summary: '会议发布与消息通知', status: '进行中', permissions: ['meeting:self', 'meeting:manage', 'notification:self:read'] },

  { key: 'user-management', routeName: 'userManagement', title: '师生信息库', description: '师生数字档案与组合检索', domain: 'base', icon: 'UserFilled', template: 'table', summary: '学生与教职工档案管理', status: '进行中', permission: 'base:read' },
  { key: 'enrollment-stats', routeName: 'enrollmentStats', title: '招生统计', description: '招生计划、报到率与生源分析', domain: 'base', icon: 'Histogram', template: 'analytics', summary: '招生计划与报到率分析', status: '进行中', permission: 'base:read' },
  { key: 'student-analytics', routeName: 'studentAnalytics', title: '学生多维统计', description: '院系、年级与学生特征分析', domain: 'base', icon: 'PieChart', template: 'analytics', summary: '院系年级多维度分析', status: '进行中', permission: 'base:read' },
  { key: 'department-major', routeName: 'departmentMajor', title: '院系专业管理', description: '院系、专业与培养资源维护', domain: 'base', icon: 'Management', template: 'table', summary: '院系与专业资源维护', status: '进行中', permission: 'base:read' },
  { key: 'news-forum', routeName: 'newsForum', title: '新闻与论坛', description: '新闻公告发布与校园交流', domain: 'base', icon: 'ChatDotSquare', template: 'table', summary: '公告发布与校园交流', status: '进行中', permission: 'forum:read', broadPermission: 'base:read' },
  { key: 'ai-report', routeName: 'aiReport', title: 'AI 智能报表', description: '自然语言查询与图表生成', domain: 'base', icon: 'DataLine', template: 'ai', summary: '自然语言查询数据', status: '在线', ai: true, permission: 'base:read' },
]

export const domainMap = Object.fromEntries(domains.map((domain) => [domain.key, domain]))

/** 基础数据域动态标签：学生（无 base:read）显示「校园资讯」 */
export function getBaseDomainLabel(permissions) {
  if (!permissions || !permissions.length) return '校园资讯'
  return permissions.includes('base:read') ? '基础数据' : '校园资讯'
}
export const serviceMap = Object.fromEntries(services.map((service) => [service.key, service]))

export const canAccessService = (service, permissions = [], userType) => {
  if (service?.roles?.length && !service.roles.includes(Number(userType))) return false
  const granted = permissions instanceof Set ? permissions : new Set(permissions)
  if (service?.permissions?.length) {
    return service.permissions.some((permission) => granted.has(permission))
  }
  if (service?.permission) {
    return granted.has(service.permission)
      || (service.broadPermission && granted.has(service.broadPermission))
  }
  return true
}
export const getServicesByDomain = (domain, permissions, userType) => services.filter((service) => service.domain === domain && (permissions === undefined || canAccessService(service, permissions, userType)))
export const getServiceByRouteName = (routeName) => services.find((service) => service.routeName === routeName)
