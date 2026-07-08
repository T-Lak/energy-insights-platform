package com.energy.analytics;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class BaseContainerTest {

   @Container
   @ServiceConnection
   static PostgreSQLContainer<?> timescale = new PostgreSQLContainer<>(
           DockerImageName.parse("timescale/timescaledb:latest-pg16")
                   .asCompatibleSubstituteFor("postgres")
   );

}

