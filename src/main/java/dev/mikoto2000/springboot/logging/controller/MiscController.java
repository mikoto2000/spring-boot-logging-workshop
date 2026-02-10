package dev.mikoto2000.springboot.logging.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.mikoto2000.springboot.logging.service.MiscService;
import lombok.RequiredArgsConstructor;

/**
 * MiscController
 */
@RequiredArgsConstructor
@RestController
public class MiscController {

  private final MiscService service;

  @GetMapping("if1")
  public String if1() {
    return service.service1();
  }

  @GetMapping("if2")
  public String if2() {
    return service.service2();
  }

  @GetMapping("if3")
  public String if3() {
    return service.service3();
  }
}
