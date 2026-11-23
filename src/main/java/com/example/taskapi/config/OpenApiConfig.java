package com.example.taskapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// 只加依赖就能出文档；这个配置类只是把文档标题、版本、说明换成自己的。
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI taskApiOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Task API（Week-02）")
                .version("v2.0")
                .description("统一响应 + 错误码 + 全局异常 + 参数校验的示例服务"));
    }
}
