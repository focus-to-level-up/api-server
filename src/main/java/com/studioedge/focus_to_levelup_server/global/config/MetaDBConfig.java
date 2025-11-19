package com.studioedge.focus_to_levelup_server.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.jdbc.init.DataSourceScriptDatabaseInitializer;
import org.springframework.boot.sql.init.DatabaseInitializationMode;
import org.springframework.boot.sql.init.DatabaseInitializationSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.List;

@Configuration
public class MetaDBConfig {

    @Bean(name = "metaDBSource")
    @ConfigurationProperties(prefix = "spring.datasource-meta")
    public DataSource metaDBSource() {
        return DataSourceBuilder.create()
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .build();
    }
    @Bean(name = "metaTransactionManager")
    public PlatformTransactionManager metaTransactionManager() {
        return new DataSourceTransactionManager(metaDBSource());
    }

    @Bean
    public DataSourceScriptDatabaseInitializer metaDbBatchSchemaInitializer() {
        DatabaseInitializationSettings settings = new DatabaseInitializationSettings();
        settings.setSchemaLocations(List.of("classpath:org/springframework/batch/core/schema-mysql.sql"));
        settings.setMode(DatabaseInitializationMode.ALWAYS);
        settings.setContinueOnError(false);
        return new DataSourceScriptDatabaseInitializer(metaDBSource(), settings);
    }
}
