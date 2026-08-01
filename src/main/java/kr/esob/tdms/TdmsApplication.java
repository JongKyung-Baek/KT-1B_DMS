package kr.esob.tdms;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import kr.esob.tdms.config.CustomBeanNameGenerator;

@SpringBootApplication
@EnableScheduling
@ComponentScan(nameGenerator = CustomBeanNameGenerator.class)
public class TdmsApplication extends SpringBootServletInitializer{

	public static void main(String[] args)  {
		new SpringApplicationBuilder(TdmsApplication.class).run(args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(TdmsApplication.class);
	}

}
