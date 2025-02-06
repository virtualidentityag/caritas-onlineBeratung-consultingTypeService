package de.caritas.cob.consultingtypeservice.testHelper;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

@Slf4j
public class MongoTestInitializer
    implements ApplicationContextInitializer<ConfigurableApplicationContext> {

  public static MongodExecutable mongodExecutable;
  private static final int mongoPort = 27017;

  @Override
  public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
    TestPropertyValues.of("spring.data.mongodb.uri=mongodb://localhost:" + mongoPort + "/test")
        .applyTo(configurableApplicationContext.getEnvironment());
    try {
      setUp();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void setUp() throws IOException {
    if (mongodExecutable != null) {
      return;
    }
    MongodStarter starter = MongodStarter.getDefaultInstance();
    MongodConfig mongodConfig =
        MongodConfig.builder()
            .version(Version.Main.V4_0)
            .net(new Net(mongoPort, Network.localhostIsIPv6()))
            .build();
    synchronized (this) {
      mongodExecutable = starter.prepare(mongodConfig);
      mongodExecutable.start();
    }
  }

  public void tearDown() {
    log.info("Stopping embedded MongoDB");
    synchronized (this) {
      if (mongodExecutable != null) {
        mongodExecutable.stop();
      }
    }
  }
}
