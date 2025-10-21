package com.struts.config.db;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

public class DataSourcesHolder {
    private List<DataSourceWrapper> dataSources;

    public DataSourcesHolder(List<DataSourceWrapper> dataSources) {
        this.dataSources = dataSources;
    }

    public List<DataSourceWrapper> getDataSources() {
        return dataSources;
    }

    public void setDataSources(List<DataSourceWrapper> dataSources) {
        this.dataSources = dataSources;
    }

    public static record DataSourceWrapper(DataSource dataSource, String name, String strategy, String table, Map<String, String> mapping) {
    }
}
