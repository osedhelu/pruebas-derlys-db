package com.derlys.config;

import java.io.IOException;
import java.util.Properties;

public record DatabaseConfig(String url) {

    public static DatabaseConfig load() throws IOException {
        var props = new Properties();
        props.load(DatabaseConfig.class.getResourceAsStream("/database.properties"));
        return new DatabaseConfig(props.getProperty("db.url").trim());
    }
}
