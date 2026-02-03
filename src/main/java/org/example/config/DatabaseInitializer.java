package org.example.config;

import org.example.entity.Role;
import org.example.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        initializeRoles();
    }

    private void initializeRoles() {
        logger.info("Initializing roles...");

        if (!roleRepository.existsByName(Role.RoleName.ROLE_USER)) {
            Role userRole = new Role(Role.RoleName.ROLE_USER, "Standard user with basic permissions");
            roleRepository.save(userRole);
            logger.info("Created ROLE_USER");
        }

        if (!roleRepository.existsByName(Role.RoleName.ROLE_ADMIN)) {
            Role adminRole = new Role(Role.RoleName.ROLE_ADMIN, "Administrator with full system access");
            roleRepository.save(adminRole);
            logger.info("Created ROLE_ADMIN");
        }

        if (!roleRepository.existsByName(Role.RoleName.ROLE_MANAGER)) {
            Role managerRole = new Role(Role.RoleName.ROLE_MANAGER, "Manager with elevated permissions");
            roleRepository.save(managerRole);
            logger.info("Created ROLE_MANAGER");
        }

        logger.info("Roles initialization completed");
    }
}
