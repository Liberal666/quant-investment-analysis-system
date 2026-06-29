# 基于 Spring Boot + Vue3 的量化投资绩效分析系统

这是一个适合课程设计答辩展示的前后端分离项目，包含股票 K 线同步、技术指标计算、相关性分析、ECharts 可视化和可选 AI 解读。

## 技术栈

- 后端：Java 17、Spring Boot、MyBatis、MySQL、RESTful API、JDK HttpClient
- 前端：Vue3 Composition API、Axios、ECharts、Vite
- 数据源：新浪财经非官方接口，失败时自动使用内置 Mock 数据

## 项目结构

```text
backend/
  src/main/java/com/example/quant/
    controller/
    service/
    service/impl/
    mapper/
    entity/
    util/
frontend/
  src/
    views/
    components/
    api/
database/
  schema.sql
  reset_real_data.py
```

## 快速启动

### 1. 初始化 MySQL

```bash
python3 database/reset_real_data.py
```

该脚本会保留表字段结构，清空旧数据，并从新浪财经拉取近一年真实日 K 数据；非交易日或缺失日期会复制前一个可用交易日数据，保证每天都有记录。

默认连接配置在 `backend/src/main/resources/application.yml`：

- database：`quant_invest_data`
- username：`root`
- password：默认读取 `DB_PASSWORD`，未设置时使用 `123456`

可通过环境变量覆盖：`DB_URL`、`DB_USERNAME`、`DB_PASSWORD`。默认 JDBC 地址为：

```text
jdbc:mysql://localhost:3306/quant_invest_data?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
```

如果页面出现 `Request failed with status code 500`，优先检查后端日志中的 MySQL 连接错误。常见原因是本机 MySQL 用户名或密码和配置不一致，可这样启动：

```bash
cd backend
DB_USERNAME=root DB_PASSWORD=你的密码 mvn spring-boot:run
```

如只需要创建基础表结构，也可以执行：

```bash
mysql -u你的用户名 -p < database/schema.sql
```

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端地址：`http://localhost:8080`

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端地址：`http://localhost:5173`

## 核心 API

- `GET /api/stock/products`
- `GET /api/stock/kline?code=600519`
- `GET /api/stock/indicator?code=600519`
- `GET /api/stock/correlation?code=600519`
- `POST /api/stock/sync?code=600519`
- `GET /api/stock/analysis?code=600519`
- `GET /api/users`
- `POST /api/users/permission`

## AI 解读配置

DeepSeek 使用 OpenAI 兼容接口。未配置密钥时，系统会返回本地规则生成的解释，保证项目可以运行。

```bash
export DEEPSEEK_API_KEY=sk-xxx
export DEEPSEEK_BASE_URL=https://api.deepseek.com
export DEEPSEEK_MODEL=deepseek-v4-flash
```

也可以在后端目录使用本地环境文件启动：

```bash
cd backend
set -a
source .env.local
set +a
mvn spring-boot:run
```

## 答辩展示建议

1. 输入 `600519`，点击“同步数据”。
2. 展示 K 线图、MA 均线、MACD、RSI、相关性散点图。
3. 说明后端如何从新浪接口获取数据，接口失败时如何使用 Mock 数据保证系统可演示。
4. 打开后端测试，展示 MA、MACD、RSI、Pearson 相关系数均为 Java 实现。
