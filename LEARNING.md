# Week-02 知识点（大白话版）

按这周任务表的顺序讲，每个都对应包里具体的文件，边看代码边读最省事。

---

## 1. 为什么要统一响应、错误码、全局异常

Week-01 里，有的接口返回 Task，有的返回 List，有的 404 直接空着，前端得为每个接口单独写一套判断。统一响应就是**约好一个盒子**，不管成功失败都装进同一个盒子：

```
code      业务码，0 成功
message   给人看的提示
data      真正的数据，失败时为 null
timestamp 时间戳
```

对应 `common/ApiResponse.java`。`success(...)` 和 `fail(...)` 两个静态方法是造盒子的快捷方式，Controller 里只写一行 `ApiResponse.success(task)`。

**错误码和 HTTP 状态码为什么要分开**：HTTP 只有 400、404、500 这么粗的几档，但业务上「标题为空」和「标题超长」都想区分，于是自己定一套 `code`（40001、40002、40401…），写在 `common/ErrorCode.java`。前端先看 HTTP 判断成不成功，再看 code 决定具体提示。

**全局异常**：Service 查不到数据时不能返回 `null` 让 Controller 到处判空，直接 `throw BusinessException.notFound("任务 1")`。这些异常不用每个 Controller 都 try-catch，`@RestControllerAdvice` 标注的 `GlobalExceptionHandler` 会**统一接住所有 Controller 抛出的异常**，翻译成对应的 HTTP 状态 + `ApiResponse`。一条链：

```
Controller 收到请求 → Service 抛 BusinessException
      → GlobalExceptionHandler 接住 → 返回 {"code":40401, ...}
```

它顺带把「参数校验失败」「JSON 写错」「未知异常」也都兜住了，所以 Controller 里看不到一句 try-catch。

---

## 2. Bean Validation：把校验写在字段上

以前要在 Controller 里手写 `if (title == null || title.isBlank())`。现在只在 DTO 字段上加注解：

```java
@NotBlank(message = "标题不能为空", groups = Create.class)
@Size(max = 100, message = "标题最多 100 个字符", groups = {Create.class, Update.class})
private String title;
```

Controller 参数上写 `@Validated(TaskRequest.Create.class)`，Spring 会在**进入方法之前**自动校验，不通过就抛异常，由全局异常处理器变成 400 + `code=40002`，`message` 里还带上具体字段名和原因。

**分组是这周最容易绕的一点**：新增时标题必填，但修改时只想传 `{"completed":true}`，如果 `@NotBlank` 不分场合一直生效，这种正常请求会被误杀。所以定义 `Create` / `Update` 两个空接口当标签：必填规则只挂 `Create`，长度规则两个组都挂。POST 用 `@Validated(Create.class)`，PUT 用 `@Validated(Update.class)`。

依赖别忘了：Spring Boot 3 起 `spring-boot-starter-validation` 不再随 web 自动带入，`pom.xml` 里要显式加。

---

## 3. OpenAPI / Swagger：加依赖就出文档

接口写完，别人怎么知道有哪些地址、参数怎么填？OpenAPI 是一套描述接口的标准格式，springdoc 会在程序运行时**自动扫描 Controller 和 DTO**，生成这份描述，再附一个网页界面。

只需要两步：

1. `pom.xml` 加 `springdoc-openapi-starter-webmvc-ui:2.8.15`（版本要和 Spring Boot 3.4.x 对上，2.8.x 才兼容，Boot 2 时代的 `springdoc-openapi-ui` 不能用）；
2. 可选：`config/OpenApiConfig.java` 把文档标题版本换成自己的。

然后打开：
- `http://localhost:8080/swagger-ui.html` — 可视化界面，能直接点「Try it out」发请求
- `http://localhost:8080/v3/api-docs` — 原始 JSON，给机器读

`@Tag` 给接口分组，`@Operation` 写接口说明，`@Schema` 描述字段含义和示例值。这些注解不改任何逻辑，只让文档更好读。

---

## 4. JUnit 5 + MockMvc：不起服务也能测接口

JUnit 5 是写测试的框架，`@Test` 标一个方法就是一个用例，`@DisplayName` 是给人看的名字，`@BeforeEach` 在每个用例前执行。

MockMvc 的妙处：**不用真的启动 Tomcat、不用真的走网络**，直接把一条 HTTP 请求交给 Spring MVC 处理，然后检查返回。所以一个用例几十毫秒，跑 10 个也就几秒。

一次测试通常三步：

```java
mockMvc.perform(get("/api/tasks/999"))        // 1. 发什么请求
       .andExpect(status().isNotFound())      // 2. 期望什么状态
       .andExpect(jsonPath("$.code").value(40401));  // 3. 期望响应体里有什么
```

这周要求覆盖三类：**正常请求**（201/200 + `code=0`）、**参数错误**（400 + `code=40002`）、**数据不存在**（404 + `code=40401`）。包里 10 个用例就是按这三类写的。

有个坑：Service 用内存 Map 存数据，测试之间会互相污染。所以给 `TaskService` 加了 `clear()`，`@BeforeEach` 里调一次，每个用例都从空库开始。

---

## 5. 脚手架、团队基线、分支规则

**脚手架**＝大家一起开工前先定好的那套底子：目录怎么分、依赖用哪个版本、响应长什么样、错误码怎么编、文档放哪、测试怎么写。有了它，各人写的代码才能拼得起来，也不会出现「你返回 Map 我返回 List」这种问题。

**为什么先验收再建团队仓库**：如果基线本身跑不起来，全组都会踩同一个坑。所以每人先在自己的环境跑一遍验收清单（`SCAFFOLD.md`），确认 `mvn verify` 绿了、Swagger 能开、6 个接口都对，再比较各人版本选出一份推到 `main`。

**分支规则**是保护 `main` 的：不许直接 push，必须开 PR、CI 跑过、有人 review 才能合并。这样基线不会被谁手滑改坏。具体步骤和命令都在 `TEAM_SETUP.md`，`.github/workflows/ci.yml` 就是给 CI 用的检查（每次 push/PR 自动 `mvn clean verify`）。

**两个标签**：`scaffold-v1` 标在「脚手架定版」那次提交上，`week-02` 标在本周最终状态上。标签相当于给某次提交起个好记的名字，以后想回到这个版本随时能找到。
