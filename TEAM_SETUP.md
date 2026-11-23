# 团队基线仓库搭建步骤

**前提**：每人都已按 `SCAFFOLD.md` 跑通验收清单，并已比较个人版本、选定了统一脚手架。

> 下面所有 Git / GitHub 操作请在**你自己的电脑**上做。打包方没有创建仓库、没有提交、没有推送、没有打标签，只提供了命令清单。

---

## 一、建团队仓库

在 GitHub 网页右上角 `+ → New repository`：

- 仓库名建议 `task-api-team`（或组内约定的名字）
- 可见性按组内要求（Public / Private）
- **不要**勾选 `Add a README file` / `.gitignore` / license，保持空仓库，避免第一次 push 冲突
- 建好后复制仓库地址，形如 `https://github.com/<组织或用户名>/task-api-team.git`

## 二、把选定的脚手架推到 main

在**选定那份**项目目录下执行（把地址换成你自己的）：

```bash
cd task-api-week02
git init
git config user.name "你的名字"
git config user.email "你的邮箱"
git add .
git commit -m "scaffold-v1: 统一响应/错误码/全局异常/参数校验/OpenAPI/MockMvc 测试"
git branch -M main
git remote add origin https://github.com/<组织或用户名>/task-api-team.git
git push -u origin main
```

几个上次踩过的坑，这次提前避开：

- `git init` 一定要在项目目录里执行，否则会提示「不是 git 仓库」或 `bad revision`。
- 路径里带单引号（例如 `LivingSeven'work`）时，`cd` 要用双引号把整段包住：`cd ~/"LivingSeven'work/..."`，shell 才不会以为引号没闭合。
- 先 `commit` 再 `branch -M main`；没提交就改名会报 `bad revision refs/heads/master`。
- 没配 `user.name` / `user.email` 会提交失败，上面两条已包含。
- GitHub 早已禁用密码推送，HTTPS 方式要用 **Personal Access Token** 当密码（Settings → Developer settings → Personal access tokens）。
- `.gitignore` 已忽略 `target/`、`.idea/`，别把构建产物和 IDE 配置传上去。

推送后用 `git log --oneline` 和网页确认 `main` 分支上有这次提交。

## 三、设置 main 分支规则

GitHub 仓库页 → **Settings → Branches → Add branch ruleset**（或旧版 `Add classic branch protection rule`），针对 `main` 勾选：

| 规则 | 作用 | 建议 |
|------|------|------|
| Restrict deletions / Block force pushes | 不让谁手滑删库或强推覆盖历史 | 必开 |
| Require a pull request before merging | 不许直接 push 到 main，必须开 PR | 必开，审批人数设 1 |
| Require status checks to pass | CI 没过不许合并 | 必开，勾选 `verify` |
| Require conversation resolution | 评论没处理完不许合并 | 可选 |
| Do not allow bypass | 管理员也走同一套规则 | 组内 strict 时开 |

**状态检查从哪来**：包里已经放了 `.github/workflows/ci.yml`，推上去后 GitHub Actions 会自动跑 `mvn -B -ntp clean verify`。第一次跑完，回到分支规则里就能搜到 `verify` 这个检查项。若列表是空的，先手动触发一次 Actions，再回来勾。

日常协作流程：成员从 `main` 拉 `feat/xxx` 分支 → 提交 → 开 PR → CI 绿 + 1 人 approve → 合并。

## 四、打两个标签

在**基线定版**那次提交上：

```bash
git tag scaffold-v1
git push origin scaffold-v1
```

完成本周全部内容后，在最终那次提交上：

```bash
git tag week-02
git push origin week-02
```

一次推多个标签也可以：`git push origin scaffold-v1 week-02`。

验证：

```bash
git tag                      # 本地列出 scaffold-v1 / week-02
git ls-remote --tags origin  # 远端也能看到
```

网页上：仓库首页 → 分支下拉切到 **Tags**，应能看到 `scaffold-v1` 和 `week-02`。注意右侧 About 栏里的文字是仓库描述，不是标签。

## 五、本轮该留的截图证据

- [ ] `mvn clean verify` 的 `BUILD SUCCESS` + `Tests run: 11`（或更多）
- [ ] Swagger UI 页面（能看到 6 个接口）
- [ ] `/v3/api-docs` 的 JSON 或 `api-test.http` 里 400 / 404 的响应
- [ ] 团队仓库首页：分支 `main` + 文件列表 + 最新 commit
- [ ] Settings → Branches 里的规则页面（能看到 PR / status checks 已勾）
- [ ] Tags 页面：`scaffold-v1` 与 `week-02`
- [ ] Actions 里 `verify` 的绿色对勾（可选但很有说服力）

## 六、对应关系（任务表 → 交付物）

| 任务表要求 | 本包 / 本步骤的对应物 |
|------------|----------------------|
| 团队基线仓库 | 第一、二步建仓并推送 `main` |
| 选定统一脚手架 | `SCAFFOLD.md` 的验收清单与比较表 |
| 设置分支规则 | 第三步 + `.github/workflows/ci.yml` |
| `scaffold-v1` | 第四步第一个标签 |
| Git 标签 `week-02` | 第四步第二个标签 |
