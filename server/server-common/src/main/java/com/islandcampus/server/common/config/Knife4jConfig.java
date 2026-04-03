package com.islandcampus.server.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("灵岛校园 IslandCampus API")
                        .version("2.1.0")
                        .description("中小学课堂电脑集群管理与教学辅助系统 - RESTful API文档")
                        .contact(new Contact().name("IslandCampus Team"))
                        .license(new License().name("MIT").url("https://github.com/islandcampus/island-campus")));
    }
}
