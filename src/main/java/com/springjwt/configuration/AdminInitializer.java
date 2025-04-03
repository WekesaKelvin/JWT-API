package com.springjwt.configuration;

import com.springjwt.dto.SignupDTO;
import com.springjwt.dto.UserDTO;
import com.springjwt.repositories.UserRepository;
import com.springjwt.services.auth.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminInitializer.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        // Check if an admin user already exists
        if (userRepository.findByRole("ROLE_ADMIN").isEmpty()) {
            logger.info("No admin user found, creating default admin user");

            SignupDTO adminSignup = new SignupDTO();
            adminSignup.setName("Admin");
            adminSignup.setEmail("admin@gmail.com");
            adminSignup.setPassword("admin123"); // Will be encoded by AuthService
            adminSignup.setRole("ROLE_ADMIN");

            UserDTO createdAdmin = authService.createUser(adminSignup);
            logger.info("Default admin user created with email: " + createdAdmin.getEmail());
        } else {
            logger.info("Admin user already exists, skipping creation");
        }
    }
}