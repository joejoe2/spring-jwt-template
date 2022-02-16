package com.joejoe2.demo;

import com.joejoe2.demo.exception.AlreadyExist;
import com.joejoe2.demo.exception.ValidationError;
import com.joejoe2.demo.model.auth.Role;
import com.joejoe2.demo.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Locale;

@SpringBootApplication
@EnableAsync
public class DemoApplication implements CommandLineRunner {
	@Autowired
	private Environment env;
	@Autowired
	private UserService userService;
	private static final Logger logger = LoggerFactory.getLogger(DemoApplication.class);

	public static void main(String[] args) {
		Locale.setDefault(Locale.ENGLISH);
		SpringApplication.run(DemoApplication.class, args);
	}

	//run after application start
	@Override
	public void run(String... args) throws Exception {
		String adminName=env.getProperty("default.admin.username", "");
		String adminPassword=env.getProperty("default.admin.password", "");
		String adminEmail=env.getProperty("default.admin.email", "");
		if (adminName.length()>0&&adminPassword.length()>0&&adminEmail.length()>0){
			try {
				userService.createUser(adminName, adminPassword, adminEmail, Role.ADMIN);
				logger.info("create admin user from env !");
			}catch (AlreadyExist e){
				logger.info("default admin already exist, skip creation");
			}catch (ValidationError e){
				logger.error("cannot create default admin: "+e.getMessage());
			}
		}
	}
}
