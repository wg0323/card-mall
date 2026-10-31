# 次卡商城 · 商家小程序

店员侧小程序：登录 → 扫码核销 → 扣次确认 → 核销记录 → 查会员。微信原生框架 + TDesign 组件库，与客户小程序同一套设计令牌。

## 环境要求

| 项 | 要求 |
| --- | --- |
| 微信开发者工具 | 稳定版 1.06 以上 |
| 基础库 | 2.32.3 起（`app.json` 的 `lazyCodeLoading` 依赖 2.11.1+） |
| AppID | 扫码核销需要真实 AppID；当前是 `touristappid`（游客模式），只能用「手动输入码」 |
| Node.js | 18+，仅用于安装 npm 组件包 |
| 后端 | 同仓库 `server/`，本机 `localhost:8080` |

## 快速开始

```bash
cd miniapp-staff
npm install
```

1. 微信开发者工具「导入项目」，目录选 `miniapp-staff/`，AppID 填自己的测试号。
2. 工具栏「工具 → 构建 npm」，生成 `miniprogram_npm/`（TDesign 从这里加载，不构建会白屏）。
3. 详情 → 本地设置，勾选「不校验合法域名、web-view、TLS 版本以及 HTTPS 证书」，否则连不上 `http://localhost:8080`。
4. 确认后端已启动（`server` 目录 `mvn spring-boot:run`）。默认账号 `13800000000 / 123456`，首次登录后应到后台改密。

## 配置说明

| 配置 | 位置 | 说明 |
| --- | --- | --- |
| 接口地址 | `config/index.js` 的 `ENV_MAP` | `dev` 指向 `http://localhost:8080/api`，`prod` 需换成正式域名 |
| 当前环境 | `config/index.js` 的 `ENV` | 默认 `dev` |
| 品牌名 | `config/index.js` 的 `BRAND_NAME` | 同时要改 `app.json` 的 `navigationBarTitleText`（JSON 不支持变量） |
| AppID | `project.config.json` 的 `appid` | 上传与真机扫码前必须换成真实 AppID |

## 项目结构

```text
miniapp-staff/
├── app.js / app.json / app.wxss      应用入口、路由与 TabBar、全局样式
├── project.config.json               开发者工具配置（appid/编译设置）
├── api/staffApi.js                   登录、首页、核销、记录、会员查询
├── config/index.js                   环境、接口地址、品牌名
├── utils/
│   ├── request.js                    注入店员 token、剥 Result 外壳、2000 回登录页
│   ├── auth.js                       登录态读写（token + 姓名/角色/门店）
│   └── dict.js                       状态文案、配色与时间解析
├── styles/theme.wxss                 设计令牌（与客户小程序同源）
├── custom-tab-bar/                   TDesign tabBar：核销 / 记录 / 我的
└── pages/
    ├── login/          手机号 + 密码登录
    ├── home/           门店名、今日核销、扫码核销、手动输入码、查会员
    ├── verify-confirm/ 预览 + 扣减次数步进器 + 备注 + 确认
    ├── verify-result/  核销成功结果页
    ├── records/        核销记录（日期筛选 + 触底加载）
    ├── member-search/  手机号精确查会员与持卡
    └── profile/        姓名、门店、角色、退出登录
```

## 接口地址与环境切换

只调 `/api/staff/**`，三端 token 互不通用：

| 方法 | 路径 | 用途 |
| --- | --- | --- |
| POST | `/api/staff/auth/login` | 手机号 + 密码登录，返回 token 与角色 |
| GET | `/api/staff/home` | 门店名 + 今日核销次数 |
| POST | `/api/staff/verify/preview` | 扫码预览，不扣次不删码 |
| POST | `/api/staff/verify/confirm` | 确认核销，`requestId` 幂等 |
| GET | `/api/staff/verify/page` | 核销记录分页，支持日期区间 |
| GET | `/api/staff/verify/{id}` | 核销记录详情 |
| GET | `/api/staff/member/search` | 按手机号精确查会员与持卡 |
| GET | `/api/staff/card/{id}` | 按卡查详情 |

联合调试时后端完整接口文档在 `http://localhost:8080/doc.html`（Knife4j）。

## 已知限制

- **游客 AppID 不能真机扫码**：`wx.scanCode` 在 `touristappid` 下不可用，开发者工具里会直接失败。页面给了明确提示并引导走「手动输入码」，两条路径共用同一套预览与确认逻辑，所以核销链路本身不受影响；真机验收前必须换成真实 AppID。
- 相机授权被拒时会弹「去设置 / 手动输码」二选一：选「去设置」调 `wx.openSetting`，选「手动输码」直接进手输流程；开发者工具里不会真的弹授权框，所以这条分支只能真机验证。
- 核销码 60 秒失效且一次性，顾客超时后需要重新出示，页面不做本地倒计时。
- `miniprogram_npm/` 是「构建 npm」的产物，不入版本库，拉下代码后需要自己构建一次。
- M1 不做撤销核销、退款、真实支付：撤销与退款属 M2，支付在 M1 走 mock。

## 验证记录（2026-10-31）

接口级全链路（脚本调用后端，16 项全通过）：

| 环节 | 实际结果 |
| --- | --- |
| 店员登录 | `code=0`，返回 `role=1`（老板） |
| 会员 mock 登录 + 绑定手机号 | 得到 `13900000001` |
| 下单 + mock 支付 + 发卡 | 洗车5次卡，余 5 次 |
| 开核销码 | token 32 位，TTL 60 秒 |
| 店员预览 | 余次 5、可扣上限 5，预览不扣次 |
| 确认核销 2 次 | 记录号 `202610310903007636`，余 3 |
| 同一 token 再核销 | `2001 核销码已失效` |
| 同一 requestId 重放 | 返回同一记录号，不重复扣次 |
| 核销记录 / 查会员 / 今日核销数 | 均与操作一致 |

页面级冒烟（微信开发者工具自动化 + `miniprogram-automator`）：登录页、首页（门店 + 今日核销）、核销确认（余次与可扣上限）、步进器调到 2 次后的「扣后剩余」、核销记录列表、会员查询持卡、我的页（姓名/角色/门店）均通过，控制台无 error。

尚未覆盖：真机扫码（需真实 AppID）、相机授权拒绝后的系统设置引导（开发者工具无真实授权弹窗，代码已实现，等真机验证）。

## 自测清单

1. 未登录时打开小程序，直接停在登录页；密码错误提示「手机号或密码错误」。
2. 登录后进首页，显示门店名与今日核销次数；下拉可刷新。
3. 点「手动输入码」输入顾客的 32 位数字码，进入核销确认页，显示会员、卡名、剩余次数、有效期。若第 2 步点扫码时拒绝了相机授权，应先弹「去设置 / 手动输码」。
4. 把扣减次数调到 2，确认按钮按下后进入结果页：本次扣 2 次、剩余次数、核销时间。
5. 同一个码再核销一次，提示「核销码已失效」。
6. 「记录」页能看到刚才的记录；按日期筛选后条数随之变化。
7. 「查会员」输入手机号，能看到该会员与其持卡。
8. 「我的」页显示姓名、角色、门店；退出登录后回到登录页。

## 上传与发布

1. 换真实 AppID 打开项目，确认「构建 npm」已执行。
2. 工具栏「上传」，填写版本号与项目备注，生成开发版。
3. 微信公众平台 → 版本管理 → 提交审核（类目通常为生活服务/到店服务）。
4. 审核通过后发布。上线前把 `config/index.js` 的 `ENV` 改为 `prod` 并填好正式域名。
