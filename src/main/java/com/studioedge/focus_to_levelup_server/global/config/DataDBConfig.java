package com.studioedge.focus_to_levelup_server.global.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.studioedge.focus_to_levelup_server.domain.*.dao",
        entityManagerFactoryRef = "dataEntityManagerFactory",
        transactionManagerRef = "dataTransactionManager"
)
public class DataDBConfig {

    @Primary // [추가] 메인 DB이므로 Primary로 설정
    @Bean(name = "dataDBSource") // [수정] Bean 이름 명시
    @ConfigurationProperties(prefix = "spring.datasource-data")
    public DataSource dataDBSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = "dataEntityManagerFactory") // [수정] 이름 명시
    public LocalContainerEntityManagerFactoryBean dataEntityManagerFactory(
            EntityManagerFactoryBuilder builder, // [수정] Builder를 주입받아 사용
            DataSource dataDBSource) { // [수정] dataDBSource를 명시적으로 주입

        LocalContainerEntityManagerFactoryBean em = builder
                .dataSource(dataDBSource)
                .packages("com.studioedge.focus_to_levelup_server.domain.*.entity")
                .persistenceUnit("data")
                .build();

        // [수정] application.yml의 spring.jpa.* 속성이 자동으로 적용되므로
        // HashMap으로 수동 설정하는 로직 (properties.put...) 제거

        return em;
    }

    @Primary
    @Bean(name = "dataTransactionManager") // [수정] 이름 명시
    public PlatformTransactionManager dataTransactionManager(
            EntityManagerFactory dataEntityManagerFactory) { // [수정] dataEntityManagerFactory 명시적 주입

        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(dataEntityManagerFactory);
        return transactionManager;
    }
}
