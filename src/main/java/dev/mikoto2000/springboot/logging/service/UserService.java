package dev.mikoto2000.springboot.logging.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * UserService
 */
@Service
@Slf4j
public class UserService {

  private final Set<String> users = new HashSet<>();

  public void addUser(String name) {
    if (users.add(name)) {
      log.info("Add user: name={}", name);
    } else {
      log.warn("Add user failed: name={}", name);
    }
  }

  public void removeUser(String name) {
    if (users.remove(name)) {
      log.info("Remove user: name={}", name);
    } else {
      log.warn("Remove user failed: name={}", name);
    }
  }

  public Set<String> getUsers() {
    return new HashSet<String>(users);
  }

  public void fireException() {
    throw new RuntimeException("Hello, Exception!!!");
  }
}
