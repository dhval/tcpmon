package com.dhval.jpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
@Profile("db")
public class JPAConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "datasource")
    public DataSource dataSourceDev() {
        // http://stackoverflow.com/questions/28821521/configure-datasource-programmatically-in-spring-boot
        return DataSourceBuilder.create().build();
    }

    @Bean
    public JdbcTemplate contractJdbcTemplate(@Autowired DataSource source){
        return new JdbcTemplate(source);
    }
}
