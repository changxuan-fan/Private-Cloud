package com.ehz.service;

import com.ehz.domain.User;

public interface UserService {
  void createUser(String username, String password, String roleName);

  void deleteUser(String username);

  User findByUsername(String username);
}
