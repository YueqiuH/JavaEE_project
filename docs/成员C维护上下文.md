# 成员 C 开发与排障上下文

> 最后更新：2026-07-17
> 用途：供后续 Codex/开发者对话快速接手成员 C 模块。开始处理前请先阅读本文件，再结合 `docs/分工说明.md` 和 `docs/成员A实现指南.md`。

## 1. 当前状态

- 当前开发分支：`temp-branch`；成员 C 与新版门户/RBAC 的最近集成提交为 `b953e5a`。
- 成员 C 保留 5 个业务模块：
  - C1 学杂费交纳与流水查询
  - C2 固定资产管理与申领
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

### 1.2 C1 管理员与校领导查看学生缴费情况

- `ADMIN` 默认拥有 `fee:overview:read`，可查看全部学生缴费概览；独立 `LEADER`（校领导）角色也拥有该只读权限，默认演示账号为 `leader / 123321`。
- 管理员和校领导可按学号或姓名检索，并筛选欠费、已缴清、未出账；列表展示账单数、应缴金额、已缴金额和欠费金额。
- 概览由现有 `user`、`student`、`fee` 表实时聚合，不展示学生一卡通余额或消费明细；校领导不能导入账单、代缴或修改账单，管理员仍按已有 `fee:manage` 权限管理账单。

### 1.3 C2 固定资产可见性与审批规则

- 资产负责人不是数据库中的部门负责人关系，而是拥有 `asset:manage` 权限的用户；默认包括 `800001` 和 `admin`。
- 公共资产台账只显示非申请记录（`apply_user_id IS NULL`）、`status=1` 且 `quantity>0` 的现有资产，已领用、已报废、无库存和申请记录均不显示。
- 普通教职工通过“我的申请”只能查询 `apply_user_id` 为当前登录用户的申请；资产负责人通过“全部申请”查看所有申请。
- 只有 `asset:manage` 可以审批全部申请、维护和删除资产；禁止负责人审批自己提交的申请。
- 可用资产台账提供“申请领用”，后端复制资产信息并校验申请数量；待审批记录暂用 `user_id` 保存来源资产 ID。
- 领用申请审批通过时以条件更新扣减来源库存；剩余数量为 0 时来源资产转为已领用并自动退出台账，申请记录的 `user_id` 再改为申请人。
- 相关接口：`GET /api/v1/office/asset/inventory`、`GET /api/v1/office/asset/applications/mine`、`GET /api/v1/office/asset/applications`。
- 本次可见性与权限隔离复用现有 `asset` 字段，不修改数据库结构。

### 1.4 C3 工作计划与任务指派规则

- 教职工通过 `work-plan:self` 权限维护本人的周计划和月计划。
- 拥有 `work-plan:manage` 权限的用户可以查看、点评全部计划，并从具备 `work-plan:self` 权限的教师或教职工中选择任务接收人。
- 任务指派不新增数据库表或字段，直接写入现有 `work_plan`：`user_id` 为任务接收人，`plan_type` 固定为 `指派任务`。
- 普通用户不能通过个人计划接口伪造指派任务；接收人只能更新指派任务的进行中/已完成状态，不能修改任务内容和日期。
- 由于数据库结构保持不变，当前记录不单独保存指派人；`work-plan:manage` 也不代表真实部门上下级关系。
- 相关接口：`GET /api/v1/office/work-plan/assignees`、`POST /api/v1/office/work-plan/assign`。

### 1.5 C4 管理员配置的固定多步公文审批规则

- 公文类型固定为：`公文会签`、`请示报告`、`请假申请`。
- `admin` 通过 `document:manage` 权限维护审批资格和流程；审批资格只能授予启用的教师、教职工或管理员，后端明确拒绝学生。
- 管理员按公文类型配置固定的 1-10 步流程，每一步指定唯一审批人；保存时创建新版本，发起人只能查看流程，不能自行更换审批人。
- 流程模板使用 `document_workflow`、`document_workflow_step`，公文发起时在 `document_approval_task` 生成逐步任务快照；管理员后续修改只影响新公文。
- 固定流程可以包含发起人本人；轮到该步骤时，发起人可以审批自己发起的公文，但仍只有当前步骤指定审批人可以操作；审批人存在启用流程引用或未结束任务时不能被停用。
- 中间步骤同意后自动流转并通知下一审批人，末步同意后通过；任一步拒绝即结束，退回后由原发起人修改正文并按原流程快照开启新审批轮次。
- `document_approval` 保存任务、轮次、步骤和意见，历史不会因重提或流程升级而覆盖；`approval_chain` 继续保存有序审批人 ID 快照以兼容旧数据。
- 既有审批中的单步公文由迁移脚本生成“原单步审批”任务继续流转；已完成、已拒绝和已退回历史保持不变。

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
- 已有数据库依次执行 `V20260717120000__office_permissions.sql`、`V20260717153000__single_step_document_approval.sql`、`V20260718110000__configurable_document_workflow.sql`、`V20260718120000__leader_fee_overview.sql` 和 `V20260718130000__remove_ai_document_approval.sql`；全新数据库直接使用最新 `database/baseline/init.sql`。
- 初始化脚本包含 C4 固定流程演示数据：三类流程，以及待第一步审批、待第二步审批、已通过和已退回公文；测试标题统一以 `【演示】` 开头。其他成员 C 表默认不批量灌入测试数据，缴费表格显示 `No Data` 时可使用页面右上角“导入账单”创建数据。
- 数据库用户名和密码以本机配置为准，不要将真实密码写入本文件或对话。

## 6. 工作区提醒

最后检查时发现以下与成员 C 功能无关的未跟踪文件，处理时不要误删或覆盖：

```text
docs/generate_project_documents.py
docs/__pycache__/
```

它们可能属于用户或其他任务。
