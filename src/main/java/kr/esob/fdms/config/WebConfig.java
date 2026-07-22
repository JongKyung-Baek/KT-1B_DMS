package kr.esob.fdms.config;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.lucy.security.xss.servletfilter.XssEscapeServletFilter;

import kr.esob.fdms.commonlogic.interceptor.CommonCheckInterceptor;
import kr.esob.fdms.util.HTMLCharacterEscapes;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;

@Configuration
public class WebConfig implements WebMvcConfigurer{
//	@Bean
//	MappingJackson2JsonView jsonView() {
//		return new MappingJackson2JsonView();
//	}

//    @Bean
//    public PlatformTransactionManager transactionManager(DataSource dataSource) {
//        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
////        transactionManager.setRollbackOnCommitFailure(true);
//        transactionManager.setGlobalRollbackOnParticipationFailure(false);
//        return transactionManager;
//    }

	@Bean
    public ServletContextInitializer clearJsession() {
        return new ServletContextInitializer() {
            @Override
            public void onStartup(ServletContext servletContext) throws ServletException {
               servletContext.setSessionTrackingModes(Collections.singleton(SessionTrackingMode.COOKIE));
               SessionCookieConfig sessionCookieConfig=servletContext.getSessionCookieConfig();
               sessionCookieConfig.setHttpOnly(true);
            }
        };
    }

	@Bean
	public Jackson2ObjectMapperBuilder objectMapperBuilder() {
	    Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
	    builder.serializationInclusion(JsonInclude.Include.NON_NULL);
	    return builder;
	}

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		// registry.addViewController("/").setViewName("forward:/login/loginPage"); // 루트 진입 분기는 RootController에서 처리
//		WebMvcConfigurer.super.addViewControllers(registry);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Bean
	public FilterRegistrationBean getFilterRegistrationBean(){
		FilterRegistrationBean registrationBean = new FilterRegistrationBean();
		registrationBean.setFilter(new XssEscapeServletFilter());
		registrationBean.setOrder(1);
		registrationBean.addUrlPatterns("*");    //filter瑜?嫄곗튌 url patterns
		return registrationBean;
	}


	@Bean
	public ReloadableResourceBundleMessageSource messageSource() {
		ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
		messageSource.setBasename("messages/message");
		messageSource.setDefaultEncoding("UTF-8");
		messageSource.setCacheSeconds(180);

		Locale.setDefault(Locale.KOREA);

		return messageSource;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new CommonCheckInterceptor());
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry){
		registry.addResourceHandler("/resources/excel/**")
				.addResourceLocations("/excel/")
				.setCachePeriod(3600);

		registry.addResourceHandler("/resources/**")
				.addResourceLocations("classpath:/static/")
				.setCachePeriod(3600);

		registry.addResourceHandler("/vuexy/**")
				.addResourceLocations("classpath:/static/vuexy/")
				.setCachePeriod(3600);
	}

	@Bean
	public WebMvcConfigurerAdapter controlTowerWebConfigurerAdapter() {
	    return new WebMvcConfigurerAdapter() {

	        @Override
	        public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
	            super.configureMessageConverters(converters);

	            // 5. WebMvcConfigurerAdapter??MessageConverter 異붽?
	            converters.add(htmlEscapingConveter());
	        }

	        private HttpMessageConverter<?> htmlEscapingConveter() {
	            ObjectMapper objectMapper = new ObjectMapper();
	            // 3. ObjectMapper???뱀닔 臾몄옄 泥섎━ 湲곕뒫 ?곸슜
	            objectMapper.getFactory().setCharacterEscapes(new HTMLCharacterEscapes());

	            // 4. MessageConverter??ObjectMapper ?ㅼ젙
	            MappingJackson2HttpMessageConverter htmlEscapingConverter =
	                    new MappingJackson2HttpMessageConverter();
	            htmlEscapingConverter.setObjectMapper(objectMapper);

	            return htmlEscapingConverter;
	        }
	    };
	}
}

