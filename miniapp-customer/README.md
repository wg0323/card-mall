# 次卡商城 · 客户小程序

顾客侧小程序：看店 → 看卡 → 下单支付 → 我的卡包 → 出示核销码 → 看使用记录。微信原生框架 + TDesign 组件库。

## 环境要求

| 项 | 要求 |
| --- | --- |
| 微信开发者工具 | 稳定版 1.06 以上 |
| 基础库 | 2.32.3 起（`app.json` 的 `lazyCodeLoading` 依赖 2.11.1+） |
| AppID | 先在 `project.config.json` 填自己的 AppID，当前是 `touristappid`（游客模式） |
| Node.js | 18+，仅用于安装 npm 组件包 |

## 快速开始

```bash
cd miniapp-customer
npm install
```

1. 微信开发者工具「导入项目」，目录选 `miniapp-customer/`，AppID 填自己的测试号。
2. 工具栏「工具 → 构建 npm」，生成 `miniprogram_npm/`（TDesign 与二维码库都从这里加载，不构建会白屏）。
3. 详情 → 本地设置，勾选「不校验合法域名、web-view（业务域名）、TLS 版本以及 HTTPS 证书」，否则连不上 `http://localhost:8080`。
4. 确认后端已启动（`server` 目录 `mvn spring-boot:run`），本机 `card-mall.mock.enabled=true` 才能走 mock 支付。

## 配置说明

| 配置 | 位置 | 说明 |
| --- | --- | --- |
| 接口地址 | `config/index.js` 的 `ENV_MAP` | `dev` 指向 `http://localhost:8080/api`，`prod` 需换成正式域名 |
| 当前环境 | `config/index.js` 的 `ENV` | 默认 `dev` |
| 品牌名 | `config/index.js` 的 `BRAND_NAME` | 同时要改 `app.json` 的 `navigationBarTitleText`（JSON 不支持变量） |
| AppID | `project.config.json` 的 `appid` | 上传前必须换成真实 AppID |

## 项目结构

```text
miniapp-customer/
├── app.js / app.json / app.wxss      应用入口、路由与 TabBar、全局样式
├── project.config.json               开发者工具配置（appid/编译设置）
├── api/                              按模块拆的接口封装
│   ├── authApi.js / homeApi.js / cardApi.js / orderApi.js / memberApi.js
├── config/index.js                   环境、接口地址、品牌名
├── utils/
│   ├── request.js                    注入 token、剥 Result 外壳、统一提示
│   ├── auth.js                       wx.login 换 token，并发去重
│   └── dict.js                       状态文案与配色（与后台一致）
├── styles/theme.wxss                 设计令牌（色板、圆角、阴影）
└── pages/
    ├── index/          首页：门店 + 在售卡
    ├── detail/         卡详情
    ├── order-confirm/  确认订单
    ├── pay-result/     支付结果
    ├── cards/          我的卡包（可用/已用完/已过期）
    ├── card-detail/    会员卡详情 + 使用记录
    ├── verify-code/    核销码（二维码 + 60 秒倒计时）
    ├── orders/         我的订单
    ├── order-detail/   订单详情
    └── profile/        我的
```

## 接口地址与环境切换

后端按 URL 前缀区分端，本小程序只调 `/api/app/**`：

| 方法 | 路径 | 用途 |
| --- | --- | --- |
| POST | `/api/app/auth/login` | `wx.login` 的 code 换 token |
| POST | `/api/app/auth/phone` | 绑定手机号 |
| GET | `/api/app/home` | 门店信息 + 在售卡 |
| GET | `/api/app/product/{id}` | 卡详情 |
| POST | `/api/app/order` | 创建订单 |
| POST | `/api/app/order/{orderNo}/mock-pay` | M1 模拟支付 |
| GET | `/api/app/order/page` | 我的订单分页 |
| GET | `/api/app/order/{orderNo}` | 订单详情 |
| GET | `/api/app/card/page` | 我的卡包 |
| GET | `/api/app/card/{id}` | 卡详情 + 最近 20 条记录 |
| POST | `/api/app/card/{id}/verify-code` | 生成核销码，60 秒有效 |
| GET | `/api/app/member/profile` | 我的信息 |

联合调试时后端的完整接口文档在 `http://localhost:8080/doc.html`（Knife4j）。

## 已知限制

- 手机号不能随 `wx.login` 静默获取，必须用户点一次 `open-type="getPhoneNumber"`，且需要已认证的非个人主体小程序。本项目把手机号做成**硬门禁**：未授权时三个 tab 页全屏拦截，无法浏览卡产品与下单；确认订单页再兜一层校验。
- `miniprogram_npm/` 是「构建 npm」的产物，不入版本库，拉下代码后需要自己构建一次。

## 验证记录（2026-10-30）

用微信开发者工具 CLI 的自动化模式（`cli auto` + `miniprogram-automator`）在模拟器里跑过一轮，数据全部来自本地后端：

| 页面 | 实际结果 |
| --- | --- |
| 首页 | 门店信息 + 在售卡 1 张 |
| 卡详情 | 洗车5次卡，¥199 |
| 确认订单 | 卡信息加载，未绑手机号时手机号为空 |
| 卡包 | 1 张，余 5 次，状态可用 |
| 会员卡详情 | 余 5 次、0 条记录、核销按钮可用 |
| 核销码 | token 32 位，倒计时 54 秒，进度 90% |
| 订单 / 订单详情 | 1 条，¥199.00 已支付 |
| 我的 | 品牌名正确，昵称与手机号为空 |

控制台无 error / warn；二维码绘制成功（日志 `[qr] 绘制完成 228`）。自动化截图不渲染 canvas 原生层，截图里二维码区域是空白，真机上正常。

尚未验证：真机扫码、手机号授权绑定（游客 AppID 取不到 code）、真实微信支付。屏幕亮度自动调节调的是 `wx.setScreenBrightness`，开发者工具里不生效，真机上才看得出效果。

## 自测清单

1. 首页下拉刷新，能看到门店信息与在售卡；卡为 0 时显示空态。
2. 首次进入（或无手机号时）首页、卡包、我的三个 tab 都会被全屏授权页拦住，点「授权手机号并继续」后自动解除。
3. 授权后回到首页，点卡进详情 → 立即购买 → 确认订单页显示卡信息与金额，勾选协议 → 确认支付 → 支付结果页显示金额与卡名。
4. 卡包出现该卡，余次等于卡的总次数；进卡详情能看到「出示核销码」可用。
5. 核销码页显示二维码与倒计时，屏幕会自动调到最亮并给出提示，离开页面恢复原亮度；60 秒后自动换码；点「立即换码」立即刷新。
6. 店员核销后返回卡详情，余次减 1 且使用记录多一条。
7. 订单页能看到刚才的订单；待支付订单在详情页可继续支付。

## 上传与发布

1. 用真实 AppID 打开项目，确认「构建 npm」已执行。
2. 工具栏「上传」，填写版本号与项目备注，生成开发版。
3. 微信公众平台 → 版本管理 → 提交审核（类目通常为生活服务/到店服务）。
4. 审核通过后发布。上线前把 `config/index.js` 的 `ENV` 改为 `prod` 并填好正式域名。
