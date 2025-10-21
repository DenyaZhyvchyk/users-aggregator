package com.struts.service;

import com.struts.config.db.DataSourcesHolder;
import com.struts.model.AggregationResult;
import com.struts.model.User;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(
        classes = {UserAggregationServiceIT.TestDataSourceConfig.class},
        properties = "spring.main.allow-bean-definition-overriding=true"
)
class UserAggregationServiceIT {

    @Container
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.2")
            .withDatabaseName("appdb")
            .withUsername("user")
            .withPassword("password");

    @Container
    public static MySQLContainer<?> mySql = new MySQLContainer<>("mysql:8.0.38")
            .withDatabaseName("appdb")
            .withUsername("user")
            .withPassword("password");

    @Autowired
    private UserAggregationService userAggregationService;

    @Autowired
    private DataSourcesHolder dataSourcesHolder;

    @BeforeAll
    static void initSlf4j() {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "INFO");
    }
    @BeforeEach
    void setUp() {
        dataSourcesHolder.getDataSources().forEach(wrapper -> {
            try {
                wrapper.dataSource().getConnection();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        JdbcTemplate jdbcTemplate1 = new JdbcTemplate(dataSourcesHolder.getDataSources().get(0).dataSource());
        jdbcTemplate1.execute("""
            CREATE TABLE IF NOT EXISTS users (
                user_id VARCHAR(10),
                login VARCHAR(50),
                first_name VARCHAR(50),
                last_name VARCHAR(50)
            )
        """);

        JdbcTemplate jdbcTemplate2 = new JdbcTemplate(dataSourcesHolder.getDataSources().get(1).dataSource());
        jdbcTemplate2.execute("""
            CREATE TABLE IF NOT EXISTS user_table (
                ldap_login VARCHAR(50),
                name VARCHAR(50),
                surname VARCHAR(50)
            )
        """);

        jdbcTemplate1.update("INSERT INTO users(user_id, login, first_name, last_name) VALUES (?, ?, ?, ?)",
                "1", "jdoe", "John", "Doe");
        jdbcTemplate1.update("INSERT INTO users(user_id, login, first_name, last_name) VALUES (?, ?, ?, ?)",
                "2", "asmith", "Alice", "Smith");

        jdbcTemplate2.update("INSERT INTO user_table(ldap_login, name, surname) VALUES (?, ?, ?)",
                "jdoe", "John", "Doe");
        jdbcTemplate2.update("INSERT INTO user_table(ldap_login, name, surname) VALUES (?, ?, ?)",
                "asmith", "Alice", "Smith");
    }

    @Test
    void getUsers() {
        var result = userAggregationService.getUsers();

        AggregationResult expected = new AggregationResult(
                List.of(
                        new User("1", "jdoe", "John", "Doe"),
                        new User("2", "asmith", "Alice", "Smith"),
                        new User("jdoe", "jdoe", "John", "Doe"),
                        new User("asmith", "asmith", "Alice", "Smith")
                ),
                false,
                List.of()
        );

        assertEquals(expected, result);
    }

    @TestConfiguration
    public static class TestDataSourceConfig {
        @Bean
        public DataSourcesHolder dataSourcesHolder() {
            List<DataSourcesHolder.DataSourceWrapper> dataSources = new ArrayList<>();

            HikariDataSource dataSource1 = new HikariDataSource();
            dataSource1.setJdbcUrl(postgres.getJdbcUrl());
            dataSource1.setUsername(postgres.getUsername());
            dataSource1.setPassword(postgres.getPassword());
            dataSources.add(new DataSourcesHolder.DataSourceWrapper(
                    dataSource1, "postgres-1", "postgres", "users", Map.of(
                            "id", "user_id",
                            "username", "login",
                            "name", "first_name",
                            "surname", "last_name"
                    )));

            HikariDataSource dataSource2 = new HikariDataSource();
            dataSource2.setJdbcUrl(mySql.getJdbcUrl());
            dataSource2.setUsername(mySql.getUsername());
            dataSource2.setPassword(mySql.getPassword());
            dataSources.add(new DataSourcesHolder.DataSourceWrapper(
                    dataSource2, "mysql-1", "mysql", "user_table", Map.of(
                    "id", "ldap_login",
                    "username", "ldap_login",
                    "name", "name",
                    "surname", "surname"
            )));

            return new DataSourcesHolder(dataSources);
        }

        @Bean
        public UserAggregationService userAggregationService(DataSourcesHolder dataSourcesHolder) {
            return new UserAggregationService();
        }
    }
}