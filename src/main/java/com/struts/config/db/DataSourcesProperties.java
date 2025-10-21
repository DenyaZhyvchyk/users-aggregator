package com.struts.config.db;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "data-sources")
public class DataSourcesProperties {
    private List<Source> sources;

    public static record Source(
            String name,
            String strategy,
            String url,
            String table,
            String user,
            String password,
            Map<String, String> mapping
    ) {}
}
