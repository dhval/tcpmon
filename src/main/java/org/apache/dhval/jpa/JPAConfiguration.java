package org.apache.dhval.jpa;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
@Profile("db")
public class JPAConfiguration {

    @Bean
    @Primary
    @ConfigurationProperties("datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }


    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {
        // http://stackoverflow.com/questions/28821521/configure-datasource-programmatically-in-spring-boot
        return properties.initializeDataSourceBuilder().build();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public JdbcTemplate contractJdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
