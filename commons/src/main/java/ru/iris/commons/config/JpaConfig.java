package ru.iris.commons.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.sql.SQLException;

@Configuration
@ConfigurationProperties(prefix = "spring.datasource")
@EnableTransactionManagement
public class JpaConfig extends HikariConfig {

    @Bean
    public DataSource dataSource() throws SQLException {
        return new HikariDataSource(this);
    }

}