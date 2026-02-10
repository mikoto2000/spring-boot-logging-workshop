package dev.mikoto2000.springboot.logging.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;

/**
 * UserService
 */
@Service
public class UserService {

  private final Set<String> users = new HashSet<>();

  public void addUser(String name) {
    users.add(name);
  }

  public void removeUser(String name) {
    users.remove(name);
  }

  public Set<String> getUsers() {
    return new HashSet<String>(users);
  }
}
