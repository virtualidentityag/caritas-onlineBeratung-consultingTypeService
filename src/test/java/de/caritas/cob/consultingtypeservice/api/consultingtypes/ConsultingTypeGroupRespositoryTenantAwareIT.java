package de.caritas.cob.consultingtypeservice.api.consultingtypes;

import static org.assertj.core.api.Assertions.assertThat;

import de.caritas.cob.consultingtypeservice.api.tenant.TenantContext;
import de.caritas.cob.consultingtypeservice.schemas.model.ConsultingType;
import de.caritas.cob.consultingtypeservice.testHelper.MongoTestInitializer;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.profiles.active=testing")
@TestPropertySource(properties = "multitenancy.enabled=true")
@ContextConfiguration(
    initializers = MongoTestInitializer.class)
@TestPropertySource(
    properties =
        "consulting.types.json.path=src/test/resources/consulting-type-settings-tenant-specific")
class ConsultingTypeGroupRespositoryTenantAwareIT {

  @Autowired
  private ConsultingTypeGroupRepository consultingTypeGroupRepository;

  @BeforeAll
  static void setUp() throws IOException {
    MongoTestInitializer.setUp();
  }

  @AfterAll
  static void tearDown() {
    MongoTestInitializer.tearDown();
  }

  @AfterEach
  public void teardown() {
    TenantContext.clear();
  }

  @Test
  void getConsultingTypesGroupMap_Should_ReturnMapWithConsultingTypeGroups() {
    // given
    TenantContext.setCurrentTenant(2L);

    // when
    var result = consultingTypeGroupRepository.getConsultingTypesGroupMap();

    // then
    assertGroupsCorrectlyRetrievedAndFilteredForTenant(result);
  }

  private void assertGroupsCorrectlyRetrievedAndFilteredForTenant(
      Map<String, List<ConsultingType>> result) {
    assertThat(result).isNotNull();
    final String GROUP_1 = "group1";
    assertThat(result.get(GROUP_1)).isNull();
    final String GROUP_2 = "group2";
    final String GROUP_3 = "group3";
    final int CONSULTING_TYPE_ID_1 = 11;
    final int CONSULTING_TYPE_ID_2 = 12;
    assertThat(result.get(GROUP_2)).hasSize(1);
    assertThat(result.get(GROUP_3)).hasSize(1);
    assertThat(result).containsKey(GROUP_2);
    assertThat(result).containsKey(GROUP_3);
    assertThat(result.get(GROUP_2).get(0).getId()).isEqualTo(CONSULTING_TYPE_ID_1);
    assertThat(result.get(GROUP_3))
        .extracting(ConsultingType::getId)
        .contains(CONSULTING_TYPE_ID_1);
  }
}
