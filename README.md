# 空气质量公众监督系统

市民上报空气质量问题，管理员按城市把工单指派给网格员，网格员现场录入检测值并生成核验结果。

系统分三种角色：

- 市民监督员：注册、登录、提交反馈
- 管理员：管理人员、指派工单、查看统计
- 网格员：处理分配给自己的工单并提交核验

技术栈：Java 8、Spring Boot 2.6、MyBatis-Plus、MySQL、Redis、Vue 3、Vite、Element Plus。

目录：

- `shixun`：后端
- `demo1/demo`：前端

## 环境

- JDK 8
- Maven 3.6+
- MySQL 8
- Redis（默认 `127.0.0.1:6379`，无密码）
- Node.js 22.18 及以上

先启动 MySQL 和 Redis，再启动后端，最后启动前端。

## 数据库配置

1. 在 MySQL 中创建数据库，库名必须是 `demo`：

```sql
CREATE DATABASE demo DEFAULT CHARACTER SET utf8mb4;
```

2. 把课程提供的表结构和初始数据导入 `demo`。后端使用的表包括 `admins`、`supervisor`、`grid_member`、`grid_province`、`grid_city`、`aqi`、`aqi_feedback`、`statistics`。

3. 连接信息写在 `shixun/src/main/resources/application.yml`：地址 `localhost:3306`，库名 `demo`，用户名 `root`。密码和 JWT 密钥不写在这个文件里。

4. 在同一目录新建 `application-local.yml`（该文件已被忽略，不会提交到 Git）：

```yaml
spring:
  datasource:
    password: 你的MySQL密码
jwt:
  secret: 请换成至少32位的字符串
```

`jwt.secret` 长度不足 32 个字符时，后端无法签发登录令牌。

## 后端启动

进入 `shixun`，执行：

```powershell
mvn spring-boot:run -Dspring-boot.run.mainClass=com.hd.nepm.NepmServerApplication
```

也可以在 IDE 中直接运行 `com.hd.nepm.NepmServerApplication`。

启动成功后接口前缀为 `http://localhost:8080/shixun`。Redis 或 MySQL 没启动时，应用会在启动阶段失败。

## 前端启动

进入 `demo1/demo`，执行：

```powershell
npm install
npm run dev
```

浏览器打开 `http://localhost:5173`。开发服务器会把以 `/shixun` 开头的请求转发到 `http://localhost:8080`，因此后端必须先启动。

使用已有账号登录。登录成功后，之后的请求会自动带上 JWT。
