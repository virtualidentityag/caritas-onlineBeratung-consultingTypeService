package de.caritas.cob.consultingtypeservice.testHelper;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

@Component
public class MongoCleanupService implements DisposableBean {
  @Override
  public void destroy() {
    System.out.println("Cleaning up resources...");
    synchronized (MongoTestInitializer.mongodExecutable) {
      if (MongoTestInitializer.mongodExecutable != null) {
        MongoTestInitializer.mongodExecutable.stop();
      }
    }
    // MongoDB shutdown or other resource cleanup
  }
}
