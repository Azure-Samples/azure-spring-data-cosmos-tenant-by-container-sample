// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.springexamples;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "cosmos")
public class CosmosProperties {

//    private Map<Object, Object> dataSources = new LinkedHashMap<>();
//
//    public Map<Object, Object> getDataSources() {
//        return dataSources;
//    }
//
//    public void setDataSources(Map<String, Map<String, String>> datasources) {
//        System.out.println("current tenant in setDataSources: "+ TenantStorage.getCurrentTenant());
//        datasources.forEach((key, value) -> this.dataSources.put(key, convert(value)));
//    }
//
//    public Object convert(Map<String, String> source) {
//        setUri(source.get("uri"));
//        setKey(source.get("key"));
//        setDatabaseName(source.get("database"));
//        return null;
//    }
    private String uri;

    private String key;

    private String secondaryKey;

    private String databaseName;

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    private boolean queryMetricsEnabled;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSecondaryKey() {
        return secondaryKey;
    }

    public void setSecondaryKey(String secondaryKey) {
        this.secondaryKey = secondaryKey;
    }

    public boolean isQueryMetricsEnabled() {
        return queryMetricsEnabled;
    }

    public void setQueryMetricsEnabled(boolean enableQueryMetrics) {
        this.queryMetricsEnabled = enableQueryMetrics;
    }
}
