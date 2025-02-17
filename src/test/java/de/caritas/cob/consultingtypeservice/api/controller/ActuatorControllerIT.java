package de.caritas.cob.consultingtypeservice.api.controller;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.caritas.cob.consultingtypeservice.ConsultingTypeServiceApplication;
import de.caritas.cob.consultingtypeservice.api.controller.ActuatorControllerIT.TestConfig;
import de.caritas.cob.consultingtypeservice.testHelper.MongoTestInitializer;
import de.caritas.cob.consultingtypeservice.testHelper.TestMongoConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(
    classes = {ConsultingTypeServiceApplication.class, TestConfig.class},
    initializers = MongoTestInitializer.class)
@TestPropertySource(properties = "spring.profiles.active=testing")
@Import(TestMongoConfig.class)
class ActuatorControllerIT {

  @Autowired private WebApplicationContext context;

  private MockMvc mockMvc;

  @BeforeEach
  public void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
  }

  @Test
  void getHealtcheck_Should_returnHealtcheck() throws Exception {
    mockMvc
        .perform(get("/actuator/health").contentType(APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("status", is("UP")));
  }

  @Test
  void getActuatorEndpoints_Should_return404NotFoundIfEndpointsNotExposed() throws Exception {
    mockMvc
        .perform(get("/actuator/env").contentType(APPLICATION_JSON))
        .andExpect(status().isNotFound());

    mockMvc
        .perform(get("/actuator/beans").contentType(APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Configuration
  static class TestConfig {
    @MockBean private JwtDecoder jwtDecoder;
  }
}
