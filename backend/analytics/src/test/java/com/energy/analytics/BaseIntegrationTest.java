package com.energy.analytics;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class BaseIntegrationTest {

   @Container
   @ServiceConnection
   static PostgreSQLContainer<?> timescale = new PostgreSQLContainer<>(
           DockerImageName.parse("timescale/timescaledb:latest-pg16")
                   .asCompatibleSubstituteFor("postgres")
   ).withReuse(true);
}
