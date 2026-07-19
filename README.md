# 智慧校园服务平台

项目采用前后端分离的模块化单体架构。团队协作边界和接口规范以 [`docs/架构基线.md`](docs/架构基线.md) 为准。

## 环境要求

- JDK 25
- Maven 3.8.4 或更高版本
- Node.js 24、npm 11
- MySQL 8
- Redis（认证功能使用）

AI 功能是加分项，默认构建不会加载 Spring AI、DashScope、DeepSeek 或 WebFlux 依赖。

本地演示账号如下，密码统一为 `123321`。这些账号只用于本地开发和课程演示：

| 账号 | 角色 |
| --- | --- |
| `600001` | 学生 |
| `700001` | 辅导员 |
| `800001` | 教师 |
| `admin` | 教务处 |

学生事务审核流程如下：

- 学籍变动、奖助贷：学生提交后由辅导员初审，初审通过后由教务处终审；教师无权进入这两个审核模块。
- 评教反馈：学生可评价授课教师和本人辅导员；辅导员查看所带学生对本人的结果，教师查看本人授课课程，教务处查看全校两类汇总；文字反馈保持匿名。
- 学科竞赛：教师维护并审核本人发布的竞赛；辅导员和教务处只读查看全部竞赛及参赛队伍。
- 实验室预约：辅导员、教师和教务处具有相同的实验室管理能力，学生继续使用预约、签到和签退功能。

## 后端配置

`.env.example` 列出了可用环境变量。Spring Boot 不会自动读取 `.env` 文件，启动前需要在终端、IDE 或部署平台中设置对应变量。

PowerShell 示例：

```powershell
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "your-password"
$env:REDIS_PASSWORD = ""
```

数据库由 Flyway 在后端启动时自动初始化和升级，不再手工执行
`database/migration` 下的历史脚本。`DB_URL` 指向的 MySQL 账号需要具备建库、建表和
`ALTER TABLE` 权限；使用示例连接串时，`school_spring` 不存在也会自动创建。已有数据库
会先登记基线，再按版本补齐结构，迁移不会在每次启动时清空业务数据。

运行后端测试并启动应用：

```powershell
mvn -pl backend/campus-app -am test
mvn -pl backend/campus-app -am install -DskipTests
mvn -pl backend/campus-app spring-boot:run
```

后端默认地址为 `http://localhost:8888`，Swagger UI 地址为 `http://localhost:8888/swagger-ui/index.html`。

## 前端启动

```powershell
Set-Location frontend/src/platform
npm ci
npm run dev
```

前端 API 地址通过 `VITE_API_BASE_URL` 配置，默认值是 `http://localhost:8888`。

## 可选 AI Profile

只有开发加分项时才启用 AI Maven Profile 和 Spring Profile：

```powershell
$env:DASHSCOPE_API_KEY = "your-key"
$env:DEEPSEEK_API_KEY = "your-key"
mvn -Pai -pl backend/campus-app -am package
java -jar backend/campus-app/target/campus-app-0.0.1-SNAPSHOT.jar --spring.profiles.active=ai
```

未配置模型密钥时不要启用 `ai` Profile，核心项目仍可正常构建。

## API 基线

- 路径前缀：`/api/v1/auth`、`/api/v1/teaching`、`/api/v1/student`、`/api/v1/office`、`/api/v1/base`
- 认证请求头：`Authorization: Bearer <token>`
- 成功业务码：`0`
- 分页字段：`records`、`total`、`page`、`size`
- 请求链路头：`X-Request-Id`
- 会话：随机 Token 只返回客户端，Redis 使用 Token 的 SHA-256 摘要作为键，默认滑动有效期为 2 小时

## Git 工作流

- 功能分支从 `develop` 创建。
- 所有变更通过 PR 合并到 `develop`，无需他人批准。
- 稳定版本通过 PR 从 `develop` 合并到 `main`。
- 禁止直接推送、强推或删除 `main` 和 `develop`。
