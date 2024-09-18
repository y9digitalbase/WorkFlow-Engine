package net.risesoft.config;

import javax.sql.DataSource;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.zaxxer.hikari.HikariDataSource;

import lombok.RequiredArgsConstructor;

import net.risesoft.filter.ProcessAdminCheckUserLoginFilter;
import net.risesoft.listener.FlowableMultiTenantListener;
import net.risesoft.y9.Y9Context;
import net.risesoft.y9.configuration.Y9Properties;
import net.risesoft.y9.configuration.feature.sso.Y9SsoClientProperties;
import net.risesoft.y9.tenant.datasource.Y9TenantDataSource;
import net.risesoft.y9.tenant.datasource.Y9TenantDataSourceLookup;

/**
 * @author qinman
 * @author zhangchongjie
 * @date 2023/01/03
 */
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({Y9Properties.class, Y9ProcessAdminProperties.class})
@ImportResource({"classpath:/springconfigs/flowable.cfg.xml"})
@ComponentScan(basePackages = {"net.risesoft"})
public class ProcessAdminConfiguraton implements WebMvcConfigurer {

    private final Environment environment;

    private final Y9Properties y9config;

    Y9SsoClientProperties configProps;

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/", "/main/index");
    }

    @Bean
    public FlowableMultiTenantListener flowableMultiTenantListener() {
        return new FlowableMultiTenantListener();
    }

    @PostConstruct
    public void init() {
        configProps = y9config.getFeature().getSso();
    }

    @Bean(name = {"jdbcTemplate4Public"})
    @ConditionalOnMissingBean(name = "jdbcTemplate4Public")
    public JdbcTemplate jdbcTemplate4Public(@Qualifier("y9PublicDS") HikariDataSource y9PublicDs) {
        return new JdbcTemplate(y9PublicDs);
    }

    @Bean("jdbcTemplate4Tenant")
    @ConditionalOnMissingBean(name = "jdbcTemplate4Tenant")
    public JdbcTemplate jdbcTemplate4Tenant(@Qualifier("y9TenantDataSource") DataSource ds) {
        return new JdbcTemplate(ds);
    }

    @Bean
    public FilterRegistrationBean<ProcessAdminCheckUserLoginFilter> processAdminCheckUserLoginFilter() {
        final FilterRegistrationBean<ProcessAdminCheckUserLoginFilter> filterBean = new FilterRegistrationBean<>();
        filterBean.setFilter(new ProcessAdminCheckUserLoginFilter());
        filterBean.setAsyncSupported(false);
        filterBean.setOrder(50);
        filterBean.addUrlPatterns("/*");
        return filterBean;
    }

    @Bean
    public Y9Context y9Context() {
        return new Y9Context();
    }

    @Primary
    @ConfigurationProperties("spring.datasource.hikari.flowable")
    @Bean(name = {"y9FlowableDS"})
    public HikariDataSource y9FlowableDs() {
        HikariDataSource dataSource = new HikariDataSource();
        return dataSource;
    }

    @ConfigurationProperties("spring.datasource.hikari.y9-public")
    @Bean(name = {"y9PublicDS"})
    @ConditionalOnMissingBean(name = "y9PublicDS")
    public HikariDataSource y9PublicDs() {
        HikariDataSource dataSource = new HikariDataSource();
        return dataSource;
    }

    @Bean("y9TenantDataSource")
    public DataSource y9TenantDataSource(@Qualifier("y9FlowableDS") HikariDataSource y9FlowableDs,
        @Qualifier("y9TenantDataSourceLookup") Y9TenantDataSourceLookup y9TenantDataSourceLookup) {
        return new Y9TenantDataSource(y9FlowableDs, y9TenantDataSourceLookup);
    }

    @Bean("y9TenantDataSourceLookup")
    public Y9TenantDataSourceLookup y9TenantDataSourceLookup(@Qualifier("y9PublicDS") HikariDataSource ds) {
        return new Y9TenantDataSourceLookup(ds, environment.getProperty("y9.systemName"));
    }
}
