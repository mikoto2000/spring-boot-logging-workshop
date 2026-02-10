package dev.mikoto2000.springboot.logging.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * MiscController
 */
@RestController
public class MiscController {
  @GetMapping("if1")
  public String if1() {
    return "if1";
  }

  @GetMapping("if2")
  public String if2() {
    return "if2";
  }

  @GetMapping("if3")
  public String if3() {
    return "if3";
  }
}
