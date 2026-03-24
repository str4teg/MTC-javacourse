package com.mipt.andreysofronov.controller;

import com.mipt.andreysofronov.scope.PrototypeScopedBean;
import com.mipt.andreysofronov.scope.RequestScopedBean;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/scope")
public class ScopeDemoController {

  @GetMapping("/request")
  public Map<String, String> requestScope(RequestScopedBean requestScopedBean) {
    Map<String, String> body = new LinkedHashMap<>();
    body.put("requestId", requestScopedBean.getRequestId());
    body.put("processingStartedAt", requestScopedBean.getProcessingStartedAt().toString());
    body.put(
        "note",
        "Повторите запрос — requestId изменится (новый HTTP-запрос = новый бин).");
    return body;
  }

  @GetMapping("/prototype")
  public Map<String, Object> prototypeScope(ObjectProvider<PrototypeScopedBean> prototypeProvider) {
    PrototypeScopedBean first = prototypeProvider.getObject();
    PrototypeScopedBean second = prototypeProvider.getObject();
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("firstGeneratorInstanceId", first.getGeneratorInstanceId());
    body.put("secondGeneratorInstanceId", second.getGeneratorInstanceId());
    body.put("sameBeanInstance", first == second);
    body.put("taskIdFromFirst", first.generateTaskId());
    body.put("taskIdFromSecond", second.generateTaskId());
    return body;
  }
}
