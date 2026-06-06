package com.mipt.andreysofronov.gateway.service;

import com.mipt.andreysofronov.gateway.client.ExternalTasksClient;
import com.mipt.andreysofronov.gateway.dto.CreateTaskRequest;
import com.mipt.andreysofronov.gateway.dto.TaskDto;
import com.mipt.andreysofronov.gateway.exception.ExternalTaskNotFoundException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Resilient wrapper over {@link ExternalTasksClient} (lecture 10, Resilience4j).
 *
 * <p>Every outbound call is guarded by a {@code @RateLimiter} and a {@code @CircuitBreaker} (both
 * named {@code externalApi}). Reads additionally use {@code @Retry}. Each method declares a
 * fallback with the matching signature ({@code originalArgs + Throwable}).
 *
 * <p>Fallback policy:
 *
 * <ul>
 *   <li>{@link ExternalTaskNotFoundException} (a normal 404) and {@link RequestNotPermitted} (rate
 *       limit hit) are re-thrown so the {@code @RestControllerAdvice} can map them to 404 / 429;
 *   <li>everything else (5xx, timeouts, {@code CallNotPermittedException} when the breaker is OPEN)
 *       degrades gracefully to a stub value.
 * </ul>
 */
@Service
public class TasksGatewayService {

  private static final Logger log = LoggerFactory.getLogger(TasksGatewayService.class);
  private static final String INSTANCE = "externalApi";

  private final ExternalTasksClient client;

  public TasksGatewayService(ExternalTasksClient client) {
    this.client = client;
  }

  @RateLimiter(name = INSTANCE)
  @CircuitBreaker(name = INSTANCE, fallbackMethod = "createFallback")
  public TaskDto createTask(CreateTaskRequest request) {
    return client.create(request);
  }

  @RateLimiter(name = INSTANCE)
  @CircuitBreaker(name = INSTANCE, fallbackMethod = "getFallback")
  public TaskDto getTask(Long id) {
    return client.get(id);
  }

  @Retry(name = INSTANCE)
  @RateLimiter(name = INSTANCE)
  @CircuitBreaker(name = INSTANCE, fallbackMethod = "listFallback")
  public List<TaskDto> listTasks(Boolean completed, Integer limit) {
    return client.list(completed, limit);
  }

  @RateLimiter(name = INSTANCE)
  @CircuitBreaker(name = INSTANCE, fallbackMethod = "deleteFallback")
  public void deleteTask(Long id) {
    client.delete(id);
  }

  @RateLimiter(name = INSTANCE)
  @CircuitBreaker(name = INSTANCE, fallbackMethod = "probeFallback")
  public String probeUnstable(String mode) {
    return client.probeUnstable(mode);
  }

  // -------- fallbacks (same args + Throwable) --------------------------------------------------

  TaskDto createFallback(CreateTaskRequest request, Throwable t) {
    rethrowIfClientConcern(t);
    log.warn("createTask degraded: {}", t.toString());
    // Could not confirm creation upstream; return an unconfirmed stub (id=null).
    return new TaskDto(null, request.title(), request.completed());
  }

  TaskDto getFallback(Long id, Throwable t) {
    rethrowIfClientConcern(t);
    log.warn("getTask({}) degraded: {}", id, t.toString());
    return new TaskDto(id, "(temporarily unavailable)", false);
  }

  List<TaskDto> listFallback(Boolean completed, Integer limit, Throwable t) {
    rethrowIfClientConcern(t);
    log.warn("listTasks degraded: {}", t.toString());
    return List.of();
  }

  void deleteFallback(Long id, Throwable t) {
    rethrowIfClientConcern(t);
    log.warn("deleteTask({}) degraded (treated as accepted): {}", id, t.toString());
  }

  String probeFallback(String mode, Throwable t) {
    rethrowIfClientConcern(t);
    log.warn("probeUnstable({}) degraded: {}", mode, t.toString());
    return "degraded: external service unavailable (" + t.getClass().getSimpleName() + ")";
  }

  /**
   * 404s and rate-limit rejections are not service degradation — let them propagate to the
   * exception handler so the client sees a precise 404 / 429 instead of a masked stub.
   */
  private static void rethrowIfClientConcern(Throwable t) {
    if (t instanceof ExternalTaskNotFoundException ex) {
      throw ex;
    }
    if (t instanceof RequestNotPermitted ex) {
      throw ex;
    }
  }
}
