package com.sims.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.sims.repository")
@EnableTransactionManagement
public class DatabaseConfig {
    // Spring Boot will auto-configure DataSource based on application properties
    // No need to manually create DataSource bean for basic configuration
}