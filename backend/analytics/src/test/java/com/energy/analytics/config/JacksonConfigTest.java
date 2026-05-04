package com.energy.analytics.config;

import com.energy.analytics.dto.RawMetricDataDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class JacksonConfigTest {

   @Autowired
   private ObjectMapper objectMapper;

   @Test
   void shouldDeserializeNaNValue() throws Exception {
      // simulates what comes from kafka
      String json = """
        {
            "source": "solar",
            "value": NaN
        }
        """;

      RawMetricDataDTO dto = objectMapper.readValue(json, RawMetricDataDTO.class);
      assertThat(dto.value()).isNaN();
   }

}
