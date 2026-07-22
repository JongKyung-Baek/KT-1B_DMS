package kr.esob.fdms;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import kr.esob.fdms.config.CustomBeanNameGenerator;

@SpringBootApplication
@EnableScheduling
@ComponentScan(nameGenerator = CustomBeanNameGenerator.class)
public class FdmsApplication extends SpringBootServletInitializer{

	public static void main(String[] args)  {
		new SpringApplicationBuilder(FdmsApplication.class).run(args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(FdmsApplication.class);
	}

}
