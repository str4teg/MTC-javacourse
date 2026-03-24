package com.mipt.andreysofronov.scope;

import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
public class RequestScopedBean {

  private final String requestId = UUID.randomUUID().toString();
  private final Instant processingStartedAt = Instant.now();

  public String getRequestId() {
    return requestId;
  }

  public Instant getProcessingStartedAt() {
    return processingStartedAt;
  }
}
