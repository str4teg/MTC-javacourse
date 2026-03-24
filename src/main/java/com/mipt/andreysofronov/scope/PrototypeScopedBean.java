package com.mipt.andreysofronov.scope;

import java.util.UUID;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PrototypeScopedBean {

  private final String generatorInstanceId = UUID.randomUUID().toString();

  public String getGeneratorInstanceId() {
    return generatorInstanceId;
  }

  public String generateTaskId() {
    return UUID.randomUUID().toString();
  }
}
