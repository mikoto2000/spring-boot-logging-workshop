package dev.mikoto2000.springboot.logging.controller;

import java.util.Set;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.mikoto2000.springboot.logging.service.UserService;
import lombok.RequiredArgsConstructor;

/**
 * UserController
 */
@RequiredArgsConstructor
@RestController
public class UserController {

  private final UserService service;

  @GetMapping("addUser")
  public void addUser(
      @RequestParam String name
      ) {
    service.addUser(name);
  }

  @GetMapping("removeUser")
  public void removeUser(
      @RequestParam String name
      ) {
    service.removeUser(name);
  }

  @GetMapping("getUsers")
  public Set<String> getUsers() {
    return service.getUsers();
  }

  @GetMapping("fireException")
  public void fireException() {
    service.fireException();
  }
}
