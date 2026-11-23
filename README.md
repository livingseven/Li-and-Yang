# Task API（Week-02 · scaffold-v1）

在 Week-01 的 5 个 REST 接口之上，补齐**统一响应、错误码、全局异常、参数校验、接口文档、自动化测试**。这个目录就是团队要选定的「统一脚手架」候选版本。

> 本包只含源码与文档，**没有预先编译的 jar**。请在那台 Linux 电脑上解压后自行构建、测试、运行；打包方没有运行过 Maven，也没有启动过服务。

---

## 一、环境要求

| 工具 | 版本 | 说明 |
|------|------|------|
| JDK | 17+ | Spring Boot 3.4.0 最低要求 Java 17 |
| Maven | 3.8+ | 若 `mvn -v` 报「找不到命令 mvn」，Ubuntu/Debian 执行 `sudo apt install maven` |
| 网络 | 首次需要 | 第一次构建会下载 Spring Boot、validation、springdoc 依赖 |

## 二、快速开始（在那台电脑上）

```bash
unzip task-api-week02.zip
cd task-api-week02
bash verify.sh                 # 一次跑完测试 + 打包，看到 BUILD SUCCESS 即通过
java -jar target/task-api-0.2.0-SNAPSHOT.jar
```

`verify.sh` 等价于 `mvn -B -ntp clean verify` + 列出 jar。看到控制台 `Tomcat started on port 8080` 就可以发请求了。

- 接口文档：<http://localhost:8080/swagger-ui.html>
- OpenAPI JSON：<http://localhost:8080/v3/api-docs>
- 请求集合：`api-test.http`（IDEA 里逐行点绿色 ▶）

## 三、统一返回结构

**所有接口**（成功和失败）都返回这 4 个字段：

```json
{
  "code": 0,
  "message": "success",
  "data": { "id": 1, "title": "写周报", "description": "周五前完成", "completed": false },
  "timestamp": 1763870000000
}
```

失败示例（参数错误）：

```json
{
  "code": 40002,
  "message": "title: 标题不能为空",
  "data": null,
  "timestamp": 1763870000000
}
```

| 字段 | 含义 |
|------|------|
| `code` | 业务码，0 成功；非 0 见错误码表 |
| `message` | 提示信息；校验失败时会带上具体字段和原因 |
| `data` | 业务数据，失败时为 `null` |
| `timestamp` | 服务端时间戳（毫秒），便于排查 |

对应代码：`common/ApiResponse.java`、`common/ErrorCode.java`。

## 四、错误码表

| code | HTTP | 含义 | 触发场景 |
|------|------|------|----------|
| 0 | 200 / 201 | 成功 | 正常请求 |
| 40001 | 400 | 请求格式不正确 | JSON 写错、路径参数类型不对 |
| 40002 | 400 | 参数校验未通过 | 标题为空、标题超 100 字 |
| 40401 | 404 | 数据不存在 | 按不存在的 id 查询 / 修改 / 删除 |
| 50000 | 500 | 服务器内部错误 | 未预期异常兜底 |

业务码和 HTTP 状态码是**两套**：HTTP 给网关和浏览器看，业务码给前端和日志看，可以分得更细。全局异常处理在 `common/GlobalExceptionHandler.java`（`@RestControllerAdvice`）。

## 五、接口列表

基础地址 `http://localhost:8080`

| 方法 | 路径 | 成功状态 | 说明 |
|------|------|----------|------|
| GET | `/api/health` | 200 | 健康检查，`data.status = UP` |
| POST | `/api/tasks` | 201 | 新增任务，`title` 必填 |
| GET | `/api/tasks` | 200 | 任务列表 |
| GET | `/api/tasks/{id}` | 200 / 404 | 任务详情 |
| PUT | `/api/tasks/{id}` | 200 / 400 / 404 | 局部修改，只改非 null 字段 |
| DELETE | `/api/tasks/{id}` | 200 / 404 | 删除任务 |

> 删除成功返回 200 而不是 204，是为了让响应体也带上统一结构；如果团队约定用 204，把 `TaskController.delete` 改成 `ResponseEntity.noContent()` 即可，但那样就没有响应体了。

## 六、参数校验（Bean Validation）

规则写在 `dto/TaskRequest.java` 的字段上，Controller 用 `@Validated(分组)` 触发：

| 字段 | 规则 | 新增(Create) | 修改(Update) |
|------|------|--------------|--------------|
| `title` | `@NotBlank` 标题不能为空 | 生效 | 不生效（允许只改别的字段） |
| `title` | `@Size(max=100)` | 生效 | 生效 |
| `description` | `@Size(max=500)` | 生效 | 生效 |
| `completed` | 无约束，可不传 | — | — |

**为什么分组**：新增必须给标题，但修改时常常只想 `{"completed":true}`；如果不分组，`@NotBlank` 会把这种正常请求也挡掉。

## 七、接口文档（OpenAPI）

只加了一个依赖 `springdoc-openapi-starter-webmvc-ui:2.8.15`，再加 `config/OpenApiConfig.java` 设置标题版本。Controller 上的 `@Tag`、`@Operation`，DTO / 实体上的 `@Schema` 会出现在文档里。

打开 <http://localhost:8080/swagger-ui.html> 能看到 6 个接口，还能直接点「Try it out」发请求。

## 八、自动化测试（JUnit 5 + MockMvc）

```bash
mvn test
```

`src/test/java/.../TaskControllerTest.java` 共 **10 个用例**，另有 `TaskApiApplicationTests.contextLoads()`：

| 类型 | 用例 |
|------|------|
| 正常请求 | 健康检查、新增 201、列表、分组校验下的局部修改、增删改查全流程 |
| 参数错误 | 标题为空 400/40002、标题超长 400/40002 |
| 数据不存在 | 查询 404、修改 404、删除 404（均为 40401） |

MockMvc 不起真实 Tomcat、不发真实网络请求，直接把请求喂给 Spring MVC，因此很快。每个用例前用 `taskService.clear()` 重置内存数据，互不干扰。

## 九、项目结构

```
task-api-week02
├── pom.xml                         # Boot 3.4.0 / Java 17 / validation / springdoc 2.8.15
├── api-test.http                   # 9 组请求（含参数错误、数据不存在）
├── verify.sh                       # Linux 一键测试 + 打包
├── README.md / LEARNING.md         # 使用说明 / 知识点讲解
├── SCAFFOLD.md / TEAM_SETUP.md     # 脚手架验收 / 团队仓库流程
├── .github/workflows/ci.yml        # 推 main / PR 时自动跑 mvn verify
└── src
    ├── main
    │   ├── java/com/example/taskapi
    │   │   ├── TaskApiApplication.java
    │   │   ├── common
    │   │   │   ├── ApiResponse.java          # 统一响应
    │   │   │   ├── ErrorCode.java            # 错误码表
    │   │   │   ├── BusinessException.java   # 业务异常
    │   │   │   └── GlobalExceptionHandler.java  # @RestControllerAdvice
    │   │   ├── config/OpenApiConfig.java
    │   │   ├── controller/HealthController.java
    │   │   ├── controller/TaskController.java
    │   │   ├── dto/TaskRequest.java          # 校验注解 + 分组
    │   │   ├── model/Task.java
    │   │   └── service/TaskService.java
    │   └── resources/application.yml
    └── test/java/com/example/taskapi
        ├── TaskApiApplicationTests.java
        └── controller/TaskControllerTest.java
```

## 十、本周任务对照

| 任务 | 交付物 | 位置 |
|------|--------|------|
| 统一响应、错误码、全局异常 | `ApiResponse`、`BusinessException`、`@RestControllerAdvice` | `common/` |
| Bean Validation + OpenAPI | DTO 校验注解与分组；Swagger UI 可访问 | `dto/`、`config/`、`application.yml` |
| JUnit 5 + MockMvc | 正常 / 参数错误 / 数据不存在，10 个自动化测试 | `src/test/` |
| 脚手架验收 + 团队仓库 | 验收清单、比较表、分支规则、CI | `SCAFFOLD.md`、`TEAM_SETUP.md`、`ci.yml` |
| 团队基线 / 标签 | `scaffold-v1`、`week-02` 推送命令 | `TEAM_SETUP.md` |

## 常见问题

- **找不到命令 mvn**：`sudo apt install maven`，装完 `mvn -v` 应显示 Maven 3.x + Java 17。
- **端口 8080 被占用**：改 `application.yml` 里的 `server.port`，请求地址跟着改。
- **重启后任务没了**：数据存在内存 `Map` 里，本来就会清空，不是 bug。
- **第一次构建很慢**：Maven 在下载依赖，等它跑完，不要中断。
- **Swagger 页面打开但空**：先看 `/v3/api-docs` 有没有 JSON，再确认服务是 Week-02 这个包启动的。
