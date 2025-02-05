package de.caritas.cob.consultingtypeservice.api.consultingtypes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.caritas.cob.consultingtypeservice.ConsultingTypeServiceApplication;
import de.caritas.cob.consultingtypeservice.schemas.model.ConsultingType;
import de.caritas.cob.consultingtypeservice.testHelper.MongoTestInitializer;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(
    classes = ConsultingTypeServiceApplication.class,
    initializers = MongoTestInitializer.class)
@TestPropertySource(properties = "spring.profiles.active=testing")
class ConsultingTypeRepositoryIT {

  @Autowired private ConsultingTypeLoader consultingTypeLoader;
  @Autowired private ConsultingTypeRepositoryService consultingTypeRepositoryService;

  @BeforeAll
  static void setUp() throws IOException {
    MongoTestInitializer.setUp();
  }

  @AfterAll
  static void tearDown() {
    MongoTestInitializer.tearDown();
  }

  @Test
  void getConsultingTypeById_Should_ReturnCorrectConsultingType() {

    Integer consultingTypeId = 0;
    String slug = "consultingtype0";
    ConsultingType result = consultingTypeRepositoryService.getConsultingTypeById(consultingTypeId);
    assertEquals(consultingTypeId, result.getId());
    assertEquals(slug, result.getSlug());
  }

  @Test
  void getConsultingTypeBySlug_Should_ReturnCorrectConsultingType() {

    Integer consultingTypeId = 0;
    String slug = "consultingtype0";
    ConsultingType result = consultingTypeRepositoryService.getConsultingTypeBySlug(slug);
    assertEquals(consultingTypeId, result.getId());
    assertEquals(slug, result.getSlug());
  }

  @Test
  void getListOfConsultingTypes_Should_ReturnCompleteListOfConsultingTypes() {

    List<ConsultingType> result = consultingTypeRepositoryService.getListOfConsultingTypes();
    assertEquals(5, result.size());
  }
}
