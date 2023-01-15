package com.joejoe2.demo.init;

import com.joejoe2.demo.exception.AlreadyExist;
import com.joejoe2.demo.model.auth.Role;
import com.joejoe2.demo.service.user.auth.RegistrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class DefaultAdminInitializer implements CommandLineRunner {
    @Autowired
    private Environment env;
    @Autowired
    private RegistrationService registrationService;

    private static final Logger logger = LoggerFactory.getLogger(DefaultAdminInitializer.class);

    //run after application start
    @Override
    public void run(String... args) throws Exception {
        createDefaultAdmin(env);
    }

    private void createDefaultAdmin(Environment env) {
        String adminName = env.getProperty("default.admin.username", "");
        String adminPassword = env.getProperty("default.admin.password", "");
        String adminEmail = env.getProperty("default.admin.email", "");
        if (adminName.length() > 0 && adminPassword.length() > 0 && adminEmail.length() > 0) {
            try {
                registrationService.createUser(adminName, adminPassword, adminEmail, Role.ADMIN);
                logger.info("create admin user from env !");
            } catch (AlreadyExist e) {
                logger.info("default admin already exist, skip creation !");
            } catch (Exception e) {
                logger.error("cannot create default admin: " + e.getMessage());
            }
        }
    }
}
