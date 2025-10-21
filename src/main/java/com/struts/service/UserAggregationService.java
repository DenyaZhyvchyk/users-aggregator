package com.struts.service;

import com.struts.config.db.DataSourcesHolder;
import com.struts.model.AggregationResult;
import com.struts.model.User;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Log4j2
public class UserAggregationService {

    @Autowired
    private DataSourcesHolder dataSourcesHolder;

    private static final String ID_FIELD = "id";
    private static final String USERNAME_FIELD = "username";
    private static final String NAME_FIELD = "name";
    private static final String SURNAME_FIELD = "surname";

    public AggregationResult getUsers() {
        List<DataSourcesHolder.DataSourceWrapper> ds = dataSourcesHolder.getDataSources();
        List<CompletableFuture<List<User>>> futures = new ArrayList<>();
        Map<String, Throwable> failed = new ConcurrentHashMap<>();


        for (DataSourcesHolder.DataSourceWrapper wrp : ds) {
            CompletableFuture<List<User>> future = CompletableFuture.supplyAsync(() -> {
                try {
                    JdbcTemplate jdbcTemplate = new JdbcTemplate(wrp.dataSource());
                    String sql = String.format(
                            "SELECT %s AS id, %s AS username, %s AS name, %s AS surname FROM %s",
                            wrp.mapping().get(ID_FIELD),
                            wrp.mapping().get(USERNAME_FIELD),
                            wrp.mapping().get(NAME_FIELD),
                            wrp.mapping().get(SURNAME_FIELD),
                            wrp.table()
                    );

                    return jdbcTemplate.query(sql, (rs, rowNum) -> new User(
                            rs.getString(ID_FIELD),
                            rs.getString(USERNAME_FIELD),
                            rs.getString(NAME_FIELD),
                            rs.getString(SURNAME_FIELD)
                    ));
                } catch (Exception e) {
                    log.warn("Failed to fetch from {}", wrp.table(), e);
                    failed.put(wrp.name(), e);
                    return Collections.emptyList();
                }
            });

            futures.add(future);
        }

        List<User> users = futures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .toList();

        return new AggregationResult(users, !failed.isEmpty(), new ArrayList<>(failed.keySet()));
    }
}
