package kr.esob.tdms.config;

import java.util.Collections;

import javax.inject.Inject;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.MatchAlwaysTransactionAttributeSource;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionInterceptor;



@Configuration
@Aspect
public class TransactionConfig {
	
	@Inject
	private PlatformTransactionManager transactionManager;
	
	
	@Bean
	public TransactionInterceptor txAdvice() {
		MatchAlwaysTransactionAttributeSource source = new MatchAlwaysTransactionAttributeSource();
		RuleBasedTransactionAttribute transactionAttribute = new RuleBasedTransactionAttribute();
		
		transactionAttribute.setName("*");
		transactionAttribute.setRollbackRules(Collections.singletonList(new RollbackRuleAttribute(Exception.class)));
		source.setTransactionAttribute(transactionAttribute);
		TransactionInterceptor txAdvice = new TransactionInterceptor(transactionManager, source);
		
		return txAdvice;
	
	}
	
	@Bean
	public Advisor txAdviceAdvisor() {
		AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
		// A servlet filter is an HTTP lifecycle boundary, not a business
		// transaction boundary. If the audit filter joins a controller/service
		// transaction, a handled validation exception can mark the outer filter
		// transaction rollback-only and replace the intended 4xx response with an
		// UnexpectedRollbackException after the response has been committed.
		pointcut.setExpression("execution(* kr.esob.tdms..*.*(..))"
				+ " && !within(kr.esob.tdms.commonlogic.audit.RequestAuditFilter)");
		return new DefaultPointcutAdvisor(pointcut, txAdvice());
	}
	
	

}
