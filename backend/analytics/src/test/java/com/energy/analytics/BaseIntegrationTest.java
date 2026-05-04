package com.energy.analytics;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class BaseIntegrationTest {

   static PostgreSQLContainer<?> timescale = new PostgreSQLContainer<>(
           DockerImageName.parse("timescale/timescaledb:latest-pg16")
                   .asCompatibleSubstituteFor("postgres")
   );

   static {
      timescale.start();
   }

   @DynamicPropertySource
   static void configureProperties(DynamicPropertyRegistry registry) {
      registry.add("spring.datasource.url", timescale::getJdbcUrl);
      registry.add("spring.datasource.username", timescale::getUsername);
      registry.add("spring.datasource.password", timescale::getPassword);
   }

}

