package com.ehz.service;

import com.ehz.domain.User;
import java.util.List;

public interface UserService {
  void createUser(
      String username, String password, String realName, String roleName, boolean isEnabled);

  void deleteByUserId(Long userId);

  User findByUsername(String username);

  List<User> getAllUsers();

  User findById(Long id);
}
