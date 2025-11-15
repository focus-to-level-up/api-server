package com.studioedge.focus_to_levelup_server.global.config;

import org.springframework.batch.core.DefaultJobKeyGenerator;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.dao.Jackson2ExecutionContextStringSerializer;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.item.database.support.DefaultDataFieldMaxValueIncrementerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing // Spring Batch 활성화
public class SpringBatchConfig {

    /**
     * Spring Batch가 사용할 JobRepository를 정의합니다.
     * @Qualifier를 사용하여 Batch가 오직 'metaDBSource'와
     * 'metaTransactionManager'만 사용하도록 강제합니다.
     */
    @Bean
    public JobRepository jobRepository(
            @Qualifier("metaDBSource") DataSource dataSource,
            @Qualifier("metaTransactionManager") PlatformTransactionManager transactionManager)
            throws Exception
    {
        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(dataSource);
        factory.setTransactionManager(transactionManager);
        factory.setIsolationLevelForCreate("ISOLATION_READ_COMMITTED");
        factory.setTablePrefix("BATCH_");
        factory.setDatabaseType("MYSQL");
        DefaultDataFieldMaxValueIncrementerFactory incrementerFactory =
                new DefaultDataFieldMaxValueIncrementerFactory(dataSource);
        factory.setIncrementerFactory(incrementerFactory);
        factory.setJobKeyGenerator(new DefaultJobKeyGenerator());
        factory.setJdbcOperations(new JdbcTemplate(dataSource));
        factory.setConversionService(new DefaultConversionService());
        factory.setLobHandler(new DefaultLobHandler());

        factory.setSerializer(new Jackson2ExecutionContextStringSerializer());
        return factory.getObject();
    }
}
