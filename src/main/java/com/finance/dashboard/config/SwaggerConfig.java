package com.finance.dashboard.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springdoc.core.models.GroupedOpenApi;


@Configuration
public class SwaggerConfig {

    @Bean
    public OpenApiCustomizer pageableOpenApiCustomizer() {
        return openApi -> {
            Schema<?> pageSchema = new Schema<>();
            pageSchema.setType("object");
            pageSchema.addProperty("content", new Schema<>().type("array"));
            pageSchema.addProperty("totalElements", new Schema<>().type("integer"));
            pageSchema.addProperty("totalPages", new Schema<>().type("integer"));
            pageSchema.addProperty("size", new Schema<>().type("integer"));
            pageSchema.addProperty("number", new Schema<>().type("integer"));
            pageSchema.addProperty("first", new Schema<>().type("boolean"));
            pageSchema.addProperty("last", new Schema<>().type("boolean"));
            pageSchema.addProperty("empty", new Schema<>().type("boolean"));
            openApi.getComponents().addSchemas("PageRecordResponse", pageSchema);
        };
    }
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("finance-dashboard")
                .packagesToScan("com.finance.dashboard.controller")
                .build();
    }
    @Bean
    public OpenAPI openAPI() {
        // This tells Swagger that our APIs use Bearer JWT tokens
        // Once configured, Swagger UI shows an "Authorize" button
        // Reviewer pastes their token once and all API calls include it automatically
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Finance Dashboard API")
                        .description(
                                "Backend API for a finance dashboard system with role-based access control. " +
                                        "Three roles are supported: ADMIN (full access), " +
                                        "ANALYST (read + dashboard), VIEWER (read only). " +
                                        "Register a user, login to get a JWT token, then click Authorize above."
                        )
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Jaswanth Kanamrlapudi")
                                .email("jaswanth5464@gmail.com")
                                .url("https://www.linkedin.com/in/jaswanth-kanamrlapudi-a41197252")))
                // Adds the security scheme globally
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        // This text appears in the Swagger Authorize popup
                                        .description("Paste your JWT token here (without 'Bearer ' prefix)")
                        ));
    }
}