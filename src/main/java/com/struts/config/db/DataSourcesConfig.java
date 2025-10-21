package com.struts.config.db;

import com.struts.config.DriverProperties;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class DataSourcesConfig {

    private final DataSourcesProperties properties;
    private final DriverProperties driverProperties;

    public DataSourcesConfig(DataSourcesProperties properties, DriverProperties driverProperties) {
        this.properties = properties;
        this.driverProperties = driverProperties;
    }

    @Bean
    public DataSourcesHolder dataSourcesHolder() {
        List<DataSourcesHolder.DataSourceWrapper> dataSources = new ArrayList<>();

        for (DataSourcesProperties.Source source : properties.getSources()) {
            try {
                HikariDataSource ds = createDataSource(source);
                dataSources.add(new DataSourcesHolder.DataSourceWrapper(
                        ds, source.name(), source.strategy(), source.table(), source.mapping()));
            } catch (Exception e) {
                throw new IllegalStateException("Failed to create DataSource for strategy: "
                        + source.strategy(), e);
            }
        }

        return new DataSourcesHolder(dataSources);
    }

    private HikariDataSource createDataSource(DataSourcesProperties.Source source) {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(source.url());
        ds.setUsername(source.user());
        ds.setPassword(source.password());
        ds.setDriverClassName(driverProperties.resolve(source.strategy()));
        return ds;
    }
}
