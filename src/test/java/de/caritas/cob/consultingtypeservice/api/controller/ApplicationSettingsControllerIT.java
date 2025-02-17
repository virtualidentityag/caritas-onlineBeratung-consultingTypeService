package de.caritas.cob.consultingtypeservice.api.controller;

import static de.caritas.cob.consultingtypeservice.api.auth.UserRole.TENANT_ADMIN;
import static de.caritas.cob.consultingtypeservice.api.auth.UserRole.TOPIC_ADMIN;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.caritas.cob.consultingtypeservice.ConsultingTypeServiceApplication;
import de.caritas.cob.consultingtypeservice.api.model.ApplicationSettingsEntity;
import de.caritas.cob.consultingtypeservice.api.model.ApplicationSettingsPatchDTO;
import de.caritas.cob.consultingtypeservice.api.repository.ApplicationSettingsRepository;
import de.caritas.cob.consultingtypeservice.api.tenant.TenantContext;
import de.caritas.cob.consultingtypeservice.api.util.JsonConverter;
import de.caritas.cob.consultingtypeservice.testHelper.MongoTestInitializer;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(classes = ConsultingTypeServiceApplication.class)
@ContextConfiguration(
    classes = ConsultingTypeServiceApplication.class,
    initializers = MongoTestInitializer.class)
@TestPropertySource(properties = "spring.profiles.active=testing")
@TestPropertySource(properties = "feature.multitenancy.with.single.domain.enabled=true")
@AutoConfigureMockMvc(addFilters = false)
class ApplicationSettingsControllerIT {

  private MockMvc mockMvc;

  @Autowired private WebApplicationContext context;

  @Autowired private ApplicationSettingsRepository applicationSettingsRepository;

  @BeforeEach
  public void setup() {
    TenantContext.clear();
    mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
  }

  @Test
  void getApplicationSettings_Should_ReturnApplicationSettings_When_UserIsNotAuthenticated()
      throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get("/settings").accept(APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.multitenancyWithSingleDomainEnabled.value").value(true))
        .andExpect(jsonPath("$.multitenancyWithSingleDomainEnabled.readOnly").value(true))
        .andExpect(jsonPath("$.multitenancyEnabled.value").value(false))
        .andExpect(jsonPath("$.multitenancyEnabled.readOnly").value(true))
        .andExpect(jsonPath("$.useTenantService.value").value(true))
        .andExpect(jsonPath("$.useTenantService.readOnly").value(false))
        .andExpect(jsonPath("$.useConsultingTypesForAgencies.value").value(false))
        .andExpect(jsonPath("$.useConsultingTypesForAgencies.readOnly").value(false))
        .andExpect(jsonPath("$.enableWalkthrough.value").value(false))
        .andExpect(jsonPath("$.enableWalkthrough.readOnly").value(false))
        .andExpect(jsonPath("$.disableVideoAppointments.value").value(true))
        .andExpect(jsonPath("$.disableVideoAppointments.readOnly").value(false))
        .andExpect(jsonPath("$.mainTenantSubdomainForSingleDomainMultitenancy.value").value("app"))
        .andExpect(
            jsonPath("$.mainTenantSubdomainForSingleDomainMultitenancy.readOnly").value(false))
        .andExpect(jsonPath("$.budibaseAuthClientId.value").value("budibaseAuthClientId"))
        .andExpect(jsonPath("$.budibaseAuthClientId.readOnly").value(false))
        .andExpect(jsonPath("$.calcomUrl.value").value("calcomUrl"))
        .andExpect(jsonPath("$.calcomUrl.readOnly").value(false))
        .andExpect(jsonPath("$.budibaseUrl.value").value("budibaseUrl"))
        .andExpect(jsonPath("$.budibaseUrl.readOnly").value(false))
        .andExpect(jsonPath("$.calendarAppUrl.value").value("calendarAppUrl"))
        .andExpect(jsonPath("$.calendarAppUrl.readOnly").value(false))
        .andExpect(jsonPath("$.useOverviewPage.value").value(false))
        .andExpect(jsonPath("$.useOverviewPage.readOnly").value(false))
        .andExpect(jsonPath("$.legalContentChangesBySingleTenantAdminsAllowed.value").value(true))
        .andExpect(
            jsonPath("$.legalContentChangesBySingleTenantAdminsAllowed.readOnly").value(false))
        .andExpect(jsonPath("$.documentationEnabled.value").value(false))
        .andExpect(jsonPath("$.documentationEnabled.readOnly").value(true));
  }

  @Test
  void
      patchApplicationSettings_Should_ReturnUpdatedApplicationSettings_When_PatchOperationSuccessful()
          throws Exception {
    // given
    giveApplicationSettingEntityWithDynamicReleaseToggles();
    AuthenticationMockBuilder builder = new AuthenticationMockBuilder();
    Authentication authentication = builder.withUserRole(TENANT_ADMIN.getValue()).build();
    ApplicationSettingsPatchDTO patchDTO = new ApplicationSettingsPatchDTO();
    patchDTO.setLegalContentChangesBySingleTenantAdminsAllowed(false);
    patchDTO.setMainTenantSubdomainForSingleDomainMultitenancy("app2");
    String jsonRequest = JsonConverter.convertToJson(patchDTO);
    mockMvc
        .perform(
            patch("/settingsadmin")
                .with(authentication(authentication))
                .header("csrfHeader", "csrfToken")
                .cookie(new Cookie("csrfCookie", "csrfToken"))
                .contentType(jakarta.ws.rs.core.MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .contentType(jakarta.ws.rs.core.MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.multitenancyWithSingleDomainEnabled.value").value(true))
        .andExpect(jsonPath("$.multitenancyWithSingleDomainEnabled.readOnly").value(true))
        .andExpect(jsonPath("$.multitenancyEnabled.value").value(false))
        .andExpect(jsonPath("$.multitenancyEnabled.readOnly").value(true))
        .andExpect(jsonPath("$.useTenantService.value").value(true))
        .andExpect(jsonPath("$.useTenantService.readOnly").value(false))
        .andExpect(jsonPath("$.useConsultingTypesForAgencies.value").value(false))
        .andExpect(jsonPath("$.useConsultingTypesForAgencies.readOnly").value(false))
        .andExpect(jsonPath("$.enableWalkthrough.value").value(false))
        .andExpect(jsonPath("$.enableWalkthrough.readOnly").value(false))
        .andExpect(jsonPath("$.disableVideoAppointments.value").value(true))
        .andExpect(jsonPath("$.disableVideoAppointments.readOnly").value(false))
        .andExpect(jsonPath("$.mainTenantSubdomainForSingleDomainMultitenancy.value").value("app2"))
        .andExpect(
            jsonPath("$.mainTenantSubdomainForSingleDomainMultitenancy.readOnly").value(false))
        .andExpect(jsonPath("$.budibaseAuthClientId.value").value("budibaseAuthClientId"))
        .andExpect(jsonPath("$.budibaseAuthClientId.readOnly").value(false))
        .andExpect(jsonPath("$.calcomUrl.value").value("calcomUrl"))
        .andExpect(jsonPath("$.calcomUrl.readOnly").value(false))
        .andExpect(jsonPath("$.budibaseUrl.value").value("budibaseUrl"))
        .andExpect(jsonPath("$.budibaseUrl.readOnly").value(false))
        .andExpect(jsonPath("$.calendarAppUrl.value").value("calendarAppUrl"))
        .andExpect(jsonPath("$.calendarAppUrl.readOnly").value(false))
        .andExpect(jsonPath("$.useOverviewPage.value").value(false))
        .andExpect(jsonPath("$.useOverviewPage.readOnly").value(false))
        .andExpect(jsonPath("$.legalContentChangesBySingleTenantAdminsAllowed.value").value(false))
        .andExpect(
            jsonPath("$.legalContentChangesBySingleTenantAdminsAllowed.readOnly").value(false))
        .andExpect(jsonPath("$.releaseToggles.featureToggleTenantCreationEnabled").value(true));

    // clean up
    resetSettingsToPreviousState(authentication);
  }

  private void resetSettingsToPreviousState(Authentication authentication) throws Exception {
    var patchDTO = new ApplicationSettingsPatchDTO();
    patchDTO.setLegalContentChangesBySingleTenantAdminsAllowed(true);
    patchDTO.setMainTenantSubdomainForSingleDomainMultitenancy("app");
    var jsonRequest = JsonConverter.convertToJson(patchDTO);
    mockMvc
        .perform(
            patch("/settingsadmin")
                .with(authentication(authentication))
                .header("csrfHeader", "csrfToken")
                .cookie(new Cookie("csrfCookie", "csrfToken"))
                .contentType(jakarta.ws.rs.core.MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .contentType(jakarta.ws.rs.core.MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  void
      patchApplicationSettings_Should_ReturnForbidden_When_UserNameDoesNotHavePermissionToPatchSettings()
          throws Exception {
    AuthenticationMockBuilder builder = new AuthenticationMockBuilder();

    ApplicationSettingsPatchDTO patchDTO = new ApplicationSettingsPatchDTO();
    patchDTO.setLegalContentChangesBySingleTenantAdminsAllowed(false);
    String jsonRequest = JsonConverter.convertToJson(patchDTO);
    mockMvc
        .perform(
            patch("/settingsadmin")
                .with(authentication(builder.withUserRole(TOPIC_ADMIN.getValue()).build()))
                .header("csrfHeader", "csrfToken")
                .cookie(new Cookie("csrfCookie", "csrfToken"))
                .contentType(jakarta.ws.rs.core.MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .contentType(jakarta.ws.rs.core.MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }

  private void giveApplicationSettingEntityWithDynamicReleaseToggles() {
    ApplicationSettingsEntity entity = applicationSettingsRepository.findAll().get(0);
    entity.setReleaseToggles("featureToggleTenantCreationEnabled", true);
    applicationSettingsRepository.deleteAll();
    applicationSettingsRepository.save(entity);
  }
}
