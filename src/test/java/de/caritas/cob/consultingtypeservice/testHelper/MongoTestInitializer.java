package de.caritas.cob.consultingtypeservice.testHelper;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import java.io.IOException;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

public class MongoTestInitializer
    implements ApplicationContextInitializer<ConfigurableApplicationContext> {

  private static MongodExecutable mongodExecutable;
  private static final int mongoPort = 27017;

  @Override
  public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
    TestPropertyValues.of("spring.data.mongodb.uri=mongodb://localhost:" + mongoPort + "/test")
        .applyTo(configurableApplicationContext.getEnvironment());
  }

  public static void setUp() throws IOException {
    MongodStarter starter = MongodStarter.getDefaultInstance();
    MongodConfig mongodConfig =
        MongodConfig.builder()
            .version(Version.Main.V4_0)
            .net(new Net(mongoPort, Network.localhostIsIPv6()))
            .build();
    mongodExecutable = starter.prepare(mongodConfig);
    mongodExecutable.start();
  }

  public static void tearDown() {
    if (mongodExecutable != null) {
      mongodExecutable.stop();
    }
  }
}
