# 脚手架验收清单（scaffold-v1 候选）

**这个 zip 就是脚手架候选版本**：定好了目录结构、依赖版本、统一响应、错误码、参数校验、接口文档和测试写法。每人先在自己环境跑通下面这张清单，再比较各人版本，选出一份作为团队基线。

> 全部操作在**你自己的那台电脑**上完成。打包方没有运行过 Maven、没有启动过服务、没有执行过任何 Git 命令，所以下面每一项都需要你亲自验证并截图留证。

---

## 一、每人先跑这 5 步

```bash
unzip task-api-week02.zip
cd task-api-week02
bash verify.sh                              # 测试 + 打包一步到位
java -jar target/task-api-0.2.0-SNAPSHOT.jar
# 另开一个终端或浏览器，做下面的接口检查
```

若 `mvn -v` 提示找不到命令：Ubuntu/Debian 执行 `sudo apt install maven`，再重跑 `verify.sh`。第一次构建会下载依赖，等它跑完。

## 二、验收清单（逐项打勾，建议每项留一张图）

- [✓] 1. `java -version` 显示 17；`mvn -v` 显示 Maven 3.x + Java 17
- [✓] 2. `mvn -B -ntp clean verify` 出现 **BUILD SUCCESS**，无测试失败
- [✓] 3. 测试报告里 **Tests run ≥ 5**（本包是 10 + 1），正常 / 参数错误 / 数据不存在三类都有
- [✓] 4. `target/` 下生成 `task-api-0.2.0-SNAPSHOT.jar`
- [✓] 5. `java -jar ...jar` 启动，控制台出现 `Tomcat started on port 8080`
- [✓] 6. `GET /api/health` 返回 `{"code":0,...,"data":{"status":"UP",...}}`
- [✓] 7. `POST /api/tasks` 正确请求返回 **201**，`data.id = 1`
- [✓] 8. `POST` 空标题 / 超长标题返回 **400 + code 40002**，`message` 带字段原因
- [✓] 9. `GET /api/tasks/999` 返回 **404 + code 40401**（修改、删除同理）
- [✓] 10. `PUT /api/tasks/1` 只传 `{"completed":true}` 能成功（分组校验没误杀）
- [✓] 11. 浏览器打开 `http://localhost:8080/swagger-ui.html` 能看到 6 个接口
- [✓] 12. `http://localhost:8080/v3/api-docs` 返回 JSON，包含标题 `Task API（Week-02）`

第 6–10 项可以直接用 `api-test.http` 在 IDEA 里点绿色 ▶ 完成；第 11–12 项用浏览器。

**验收没过怎么办**：别改基线，先记下「哪一步、报什么错、环境是什么」，在组会上对比后再决定改哪一份。常见原因见 `README.md` 最后一节。

## 三、比较个人版本（开会前各自填好）

| 比较项 | 我的版本 | 张三 | 李四 | 备注 |
|--------|----------|------|------|------|
| 目录结构 / 包名 | `com.example.taskapi` + common/config | | | 是否一致决定能不能直接合并 |
| Spring Boot 版本 | 3.4.0 | | | 版本不同最容易出兼容问题 |
| springdoc 版本 | 2.8.15 | | | 必须匹配 Boot 3.4.x |
| 统一响应字段 | code / message / data / timestamp | | | 字段名不同前端要改两遍 |
| 错误码 | 0 / 40001 / 40002 / 40401 / 50000 | | | 是否够用、有没有重复 |
| 校验规则 | `@NotBlank` + `@Size`，Create/Update 分组 | | | 修改接口是否允许只传部分字段 |
| 自动化测试数 | 10（+1 冒烟） | | | 覆盖三类场景 |
| 接口文档地址 | `/swagger-ui.html` | | | 是否可直接访问 |
| CI | `.github/workflows/ci.yml` | | | 分支规则要勾它 |
| 验收清单是否全过 | ☐ 是 ☐ 否 | | | 只从全过的版本里选 |

## 四、选定基线的三条规则

1. **只从验收清单全过的版本里选**，跑不起来的直接排除。
2. **优先选依赖版本统一、字段命名统一、测试最全的那份**，不要按「谁写得最多」选。
3. 选定后在基线版本上打 `scaffold-v1` 标签，之后所有人从这个标签拉分支；改动一律走 PR，不直接改 `main`。

选定后的推送与分支规则步骤见 `TEAM_SETUP.md`。
