# 成员 C 开发与排障上下文

> 最后更新：2026-07-17
> 用途：供后续 Codex/开发者对话快速接手成员 C 模块。开始处理前请先阅读本文件，再结合 `docs/分工说明.md` 和 `docs/成员A实现指南.md`。

## 1. 当前状态

- 当前开发分支：`temp-branch`；成员 C 与新版门户/RBAC 的最近集成提交为 `b953e5a`。
- 成员 C 保留 5 个业务模块：
  - C1 学杂费交纳与流水查询
  - C2 固定资产申请与审批
  - C3 教职工工作计划与协同
  - C4 官方公文流转 OA
  - C5 校园会议与通知发布
- 后端曾使用 JDK 25 完成 Maven 多模块编译。
- 前端曾完成 Vite 生产构建。
- 前后端需要分别启动；默认端口为前端 `5173`、后端 `8888`。

### 1.1 远端框架合并后的适配

- 集成分支：`integration/member-c-rbac`，已合入 `origin/main` 的统一响应、Redis 会话认证、RBAC 和新版门户。
- 成员 C 后端接口前缀已统一为 `/api/v1/office/**`，会经过 Bearer Token 认证过滤器。
- 个人账单、计划、公文、会议和通知不再接收前端传入的任意用户 ID，而是读取 `CurrentUserContext`。
- 办公权限已细分为缴费、资产、工作计划、公文、会议和通知共 15 个权限码。
- 五个办公页面保留真实业务实现，并接入新版门户、权限菜单和 `src/api/office.js`。
- 验证结果：JDK 25 Maven 编译通过，后端 43 项测试通过，Vite 生产构建通过。

### 1.2 C1 教师查看学生缴费情况

- `TEACHER` 默认拥有 `fee:overview:read`，可查看全部学生缴费概览。
- 教师可按学号或姓名检索，并筛选欠费、已缴清、未出账；列表展示账单数、应缴金额、已缴金额和欠费金额。
- `STAFF` 和 `ADMIN` 均无 `fee:overview:read`，不能查看学生缴费情况；其原有 `fee:manage` 仅用于导入和管理账单，不授予缴费概览查看能力。
- `LEADER` 角色及 `leader` 演示账号已移除。
- 概览由现有 `user`、`student`、`fee` 表实时聚合，不展示学生一卡通余额或消费明细。

### 1.3 C2 固定资产可见性与审批规则

- 资产模块采用单部门模式，不提供部门筛选或选择；保留 `asset.dept_id` 兼容既有结构，所有资产统一固定为 `1`。
- 固定资产申请分为 `PURCHASE`（购买）、`ADD`（添加入库）、`BORROW`（借用）和 `SCRAP`（损坏/报废）四类；后端强制只有教师、教职工可以通过 `asset:apply` 提交申请，管理员和学生即使被误授该权限也不能申请。
- `asset:manage` 只授予 `ADMIN`，后端还会校验 `ADMIN` 角色，避免仅靠手工授权绕过；只有管理员能查看审批中心、审批和维护台账，且不能审批自己提交的申请。
- 公共资产台账只显示非申请记录（`apply_user_id IS NULL`）、审批通过、`status=1` 且 `quantity>0` 的现有资产，已借完、已报废、无库存和申请记录均不显示。
- 购买审批通过仅表示同意采购，不直接生成库存；资产实际到货后须提交添加入库申请，管理员批准后另建台账记录并保留原审批申请。
- 借用申请通过 `source_asset_id` 保存来源资产；审批通过时以条件更新扣减库存，剩余数量为 0 时来源资产自动退出可借台账。
- 损坏/报废申请必须绑定来源资产并填写不超过 512 字的损坏及报废原因；管理员审批通过后扣减对应数量，全部报废时来源台账状态改为 `3=已报废`。
- `approve_user_id`、`approve_time` 和 `approve_remark` 保存审批审计信息；拒绝时必须填写意见，所有申请记录禁止删除。
- 相关接口：`GET /api/v1/office/asset/inventory`、`GET /api/v1/office/asset/applications/mine`、`GET /api/v1/office/asset/applications`、`POST /api/v1/office/asset/applications/purchase`、`POST /api/v1/office/asset/applications/add`、`POST /api/v1/office/asset/applications/borrow/{assetId}`、`POST /api/v1/office/asset/applications/scrap/{assetId}`、`POST /api/v1/office/asset/applications/{assetId}/approve`。
- 三类基础审批及管理员权限收口由 `V20260718190000__admin_asset_approval_flows.sql` 完成，损坏/报废审批字段由 `V20260718200000__asset_scrap_approval.sql` 增加。

### 1.4 C3 工作计划与勤工俭学规则

- 教师和教职工通过 `work-plan:self` 维护本人的周计划和月计划，并通过 `work-plan:manage` 向启用的学生账号指派勤工俭学任务；管理员即使拥有通用管理权限也不能指派或结算。
- 学生通过 `work-plan:self` 仅查看本人收到的勤工俭学任务并提交完成，不能创建周计划、月计划或自行设置任务内容、日期和工资。
- 勤工俭学任务写入 `work_plan`，`plan_type` 固定为 `勤工俭学`，同时保存原指派人、工资、发薪状态和发薪时间。
- 工资必须为 `0.01-10000.00` 元且最多两位小数；学生提交完成后状态为待确认，只有原指派人可以确认并发薪。
- 发薪使用事务执行，任务条件更新防止并发重复结算；`payment.work_plan_id` 唯一索引确保每个任务最多一笔工资流水。
- 工资流水类型为 `勤工俭学工资`，金额计入学生一卡通余额并出现在最近流水中；已结算任务不可修改或删除。
- 旧版 `指派任务` 因未保存原指派人和工资，仅保留为历史兼容任务，不自动转换或补发工资。
- 相关接口：`GET /api/v1/office/work-plan/assignees`、`POST /api/v1/office/work-plan/assign`、`POST /api/v1/office/work-plan/{planId}/settle`。

### 1.5 C4 管理员配置的固定多步公文审批规则

- 公文类型固定为：`公文会签`、`请示报告`、`请假申请`。
- 学生默认拥有 `document:self`，可以发起、查看、催办和重新提交本人的请假申请；后端强制学生只能使用 `请假申请` 类型，不能发起公文会签或请示报告。
- `admin` 通过 `document:manage` 权限维护审批资格和流程；审批资格只能授予启用的教师、教职工或管理员，后端明确拒绝学生。
- 管理员按公文类型配置固定的 1-10 步流程，每一步指定唯一审批人；保存时创建新版本，发起人只能查看流程，不能自行更换审批人。
- 流程模板使用 `document_workflow`、`document_workflow_step`，公文发起时在 `document_approval_task` 生成逐步任务快照；管理员后续修改只影响新公文。
- 固定流程可以包含发起人本人；轮到该步骤时，发起人可以审批自己发起的公文，但仍只有当前步骤指定审批人可以操作；审批人存在启用流程引用或未结束任务时不能被停用。
- 中间步骤同意后自动流转并通知下一审批人，末步同意后通过；任一步拒绝即结束，退回后由原发起人修改正文并按原流程快照开启新审批轮次。
- `document_approval` 保存任务、轮次、步骤和意见，历史不会因重提或流程升级而覆盖；`approval_chain` 继续保存有序审批人 ID 快照以兼容旧数据。
- 既有审批中的单步公文由迁移脚本生成“原单步审批”任务继续流转；已完成、已拒绝和已退回历史保持不变。

### 1.6 C5 会议参会范围

- 发布会议不再手工输入用户 ID，而是同时选择一个或多个参会范围：全体学生、全体辅导员（教师）、全体教职工、全体教务处（领导）。
- 后端分别按 `STUDENT`、`TEACHER`、`STAFF`、`ADMIN` 角色查找当前启用用户；“教务处（领导）”复用现有 `ADMIN` 角色，不恢复已移除的 `LEADER` 角色。
- 多个范围中的重复用户会自动去重，发布时为每名用户生成一条参会记录和一条会议通知。
- 后端不再接受前端传入任意用户 ID，避免发布者绕过固定参会范围指定账号。

## 2. 主要代码位置

### 后端

- Controller：`backend/campus-app/src/main/java/com/smartcampus/app/controller/office/`
- Mapper：`backend/campus-app/src/main/java/com/smartcampus/app/dao/office/`
- Service：`backend/campus-app/src/main/java/com/smartcampus/app/service/office/`
- Entity：`backend/campus-contract/src/main/java/com/smartcampus/contract/entity/`
- 正式配置：`backend/campus-app/src/main/resources/application.yml`
- 数据库脚本：`database/baseline/init.sql`

### 前端

- 页面：`frontend/src/platform/src/views/office/`
- API：`frontend/src/platform/src/api/office.js`
- 路由：`frontend/src/platform/src/router/index.js`

不要编辑 `backend/campus-app/target/classes/application.yml`。它是构建产物，每次编译都会由 `src/main/resources/application.yml` 覆盖。

## 3. 页面路由

| 模块 | 地址 |
|---|---|
| 缴费 | `http://127.0.0.1:5173/home/fee-payment` |
| 资产 | `http://127.0.0.1:5173/home/asset-management` |
| 工作计划 | `http://127.0.0.1:5173/home/work-plan` |
| 公文 OA | `http://127.0.0.1:5173/home/document-oa` |
| 会议通知 | `http://127.0.0.1:5173/home/meeting-notice` |

## 4. 启动方式

### 后端

推荐在 IntelliJ IDEA 中使用 JDK 25 运行：

`backend/campus-app/src/main/java/com/smartcampus/app/SmartCampusApplication.java`

命令行环境需确保 `JAVA_HOME` 指向 JDK 25。后端端口为 `8888`，Swagger 地址为：

`http://127.0.0.1:8888/swagger-ui/index.html`

### 前端

在 CMD 中执行：

```cmd
cd /d D:\huadi\team\JavaEE_project\frontend\src\platform
npm install
npm run dev
```

PowerShell 若因执行策略禁止 `npm.ps1`，使用 `npm.cmd run dev`。

## 5. 数据库注意事项

- 数据库名：`school_spring`
- 完整初始化脚本：`database/baseline/init.sql`
- 脚本已包含成员 C 使用的表：`fee`、`payment`、`asset`、`work_plan`、`document`、`document_approval`、`document_approver`、`meeting`、`meeting_attendee`、`notification`。
- 最新基线共 51 张表；移除 AI 审批权限后当前数据库有 24 个权限项，其中 15 个为成员 C 细粒度权限。
- 已有数据库依次执行 `V20260717120000__office_permissions.sql`、`V20260717153000__single_step_document_approval.sql`、`V20260718110000__configurable_document_workflow.sql`、`V20260718120000__leader_fee_overview.sql`、`V20260718130000__remove_ai_document_approval.sql`、`V20260718140000__teacher_fee_overview.sql`、`V20260718150000__remove_leader_role.sql`、`V20260718160000__student_leave_documents.sql`、`V20260718170000__single_asset_department.sql`、`V20260718180000__work_study_wages.sql`、`V20260718190000__admin_asset_approval_flows.sql` 和 `V20260718200000__asset_scrap_approval.sql`；全新数据库直接使用最新 `database/baseline/init.sql`。
- 初始化脚本包含 C4 固定流程演示数据：三类流程，以及待第一步审批、待第二步审批、已通过和已退回公文；测试标题统一以 `【演示】` 开头。其他成员 C 表默认不批量灌入测试数据，缴费表格显示 `No Data` 时可使用页面右上角“导入账单”创建数据。
- 数据库用户名和密码以本机配置为准，不要将真实密码写入本文件或对话。

## 6. 工作区提醒

最后检查时发现以下与成员 C 功能无关的未跟踪文件，处理时不要误删或覆盖：

```text
docs/generate_project_documents.py
docs/__pycache__/
```

它们可能属于用户或其他任务。
