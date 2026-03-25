package com.mipt.andreysofronov.controller;

import com.mipt.andreysofronov.dto.ScopePrototypeInfoDto;
import com.mipt.andreysofronov.dto.ScopeRequestInfoDto;
import com.mipt.andreysofronov.scope.PrototypeScopedBean;
import com.mipt.andreysofronov.scope.RequestScopedBean;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/scope")
@Tag(name = "Scope demo", description = "Демонстрация scope бинов Spring")
public class ScopeDemoController {

  private final ObjectProvider<PrototypeScopedBean> prototypeScopedBeanProvider;

  public ScopeDemoController(ObjectProvider<PrototypeScopedBean> prototypeScopedBeanProvider) {
    this.prototypeScopedBeanProvider = prototypeScopedBeanProvider;
  }

  @GetMapping("/request")
  @Operation(summary = "Демонстрация request scope")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Информация о request-scoped бине",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ScopeRequestInfoDto.class)))
  })
  public ResponseEntity<ScopeRequestInfoDto> requestScope(RequestScopedBean requestScopedBean) {
    ScopeRequestInfoDto body =
        new ScopeRequestInfoDto(
            requestScopedBean.getRequestId(),
            requestScopedBean.getProcessingStartedAt(),
            "Повторите запрос — requestId изменится (новый HTTP-запрос = новый бин).");
    return ResponseEntity.ok(body);
  }

  @GetMapping("/prototype")
  @Operation(summary = "Демонстрация prototype scope")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Два экземпляра prototype-бина за один запрос",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ScopePrototypeInfoDto.class)))
  })
  public ResponseEntity<ScopePrototypeInfoDto> prototypeScope() {
    PrototypeScopedBean first = prototypeScopedBeanProvider.getObject();
    PrototypeScopedBean second = prototypeScopedBeanProvider.getObject();
    ScopePrototypeInfoDto body =
        new ScopePrototypeInfoDto(
            first.getGeneratorInstanceId(),
            second.getGeneratorInstanceId(),
            first == second,
            first.generateTaskId(),
            second.generateTaskId());
    return ResponseEntity.ok(body);
  }
}
