package com.StockSync.product.microservice.Filter;

import com.StockSync.product.microservice.Filter.ManagerRoleBlockFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<ManagerRoleBlockFilter> managerRoleFilterRegistration() {
        FilterRegistrationBean<ManagerRoleBlockFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new ManagerRoleBlockFilter());
        // match both the collection root and any sub-paths
        registrationBean.addUrlPatterns("/api/v1/categories", "/api/v1/categories/*"); // restrict category endpoints (fixed to match controller)
        registrationBean.setOrder(1); // priority
        return registrationBean;
    }
}
