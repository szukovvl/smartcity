package re.smartcity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import re.smartcity.data.TestBean;
import re.smartcity.handlers.SmartCityResourceHandler;
import re.smartcity.task.TestService;

import javax.annotation.PreDestroy;

@SpringBootApplication
public class SmartCityApplication implements CommandLineRunner {

	private final Logger logger = LoggerFactory.getLogger(SmartCityResourceHandler.class);

	@Autowired
	private TestBean bean;

	//@Autowired
	//private TestService service;

	public static void main(String[] args) {
		SpringApplication.run(SmartCityApplication.class, args);
	}

	@Override
	public void run(String... args) {
		logger.info("--> run");
		//service.start(bean);
		logger.info("--> run ВЫХОД");
	}
}
