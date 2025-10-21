package com.struts.model;

import java.util.List;

public record AggregationResult(
        List<User> users,
        boolean partial,
        List<String> failedSources
) {}
