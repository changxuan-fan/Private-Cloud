package com.ehz.controller;

import com.ehz.domain.User;
import com.ehz.service.UserFileMappingService;
import com.ehz.service.UserService;
import jakarta.transaction.Transactional;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {
  private final UserService userService;
  private final UserFileMappingService userFileMappingService;
  private final PasswordEncoder passwordEncoder;

  public UserController(
      UserService userService,
      UserFileMappingService userFileMappingService,
      PasswordEncoder passwordEncoder) {
    this.userService = userService;
    this.userFileMappingService = userFileMappingService;
    this.passwordEncoder = passwordEncoder;
  }

  @GetMapping({"/ehz/admin", "/users", "/admin", "/ehz/users"})
  public String admin() {
    return "redirect:/ehz/admin/users";
  }

  @GetMapping("/ehz/admin/users")
  public String users(Model model) {
    List<User> users = userService.getAllUsers();
    model.addAttribute("users", users);
    return "users";
  }

  @PostMapping("/ehz/admin/users")
  @Transactional
  public String userCreate(
      @RequestParam("username") String username,
      @RequestParam("password") String password,
      @RequestParam("realName") String realName,
      @RequestParam("role") String role) {

    userService.createUser(username, password, realName, role, true);

    return "redirect:/ehz/admin/users";
  }

  @PostMapping("/ehz/admin/username-check")
  @ResponseBody
  public boolean usernameCheck(@RequestParam("username") String username) {
    String userTrimmed = username.trim();
    return userService.existsByUsername(userTrimmed);
  }

  @Transactional
  @PostMapping("/ehz/admin/users/{userId}/password")
  @ResponseBody
  public boolean userPasswordUpdate(
      @PathVariable String userId,
      @RequestParam("input-password") String password,
      RedirectAttributes redirectAttributes) {

    User user = userService.findById(Long.valueOf(userId));
    user.setPassword(passwordEncoder.encode(password.trim()));
    return true;
  }

  @Transactional
  @GetMapping("/ehz/admin/users/{userId}/delete")
  @ResponseBody
  public boolean userDelete(@PathVariable String userId, RedirectAttributes redirectAttributes) {

    User user = userService.findById(Long.valueOf(userId));
    // Delete all userFileMapping
    userFileMappingService.deleteAllByUser(user);

    userService.deleteByUserId(Long.valueOf(userId));
    return true;
  }

  @Transactional
  @GetMapping("/ehz/admin/users/{userId}/enable")
  @ResponseBody
  public boolean userEnabled(@PathVariable String userId, RedirectAttributes redirectAttributes) {

    User user = userService.findById(Long.valueOf(userId));
    user.setEnabled(true);
    return true;
  }

  @Transactional
  @GetMapping("/ehz/admin/users/{userId}/disable")
  @ResponseBody
  public boolean userDisabled(@PathVariable String userId, RedirectAttributes redirectAttributes) {

    User user = userService.findById(Long.valueOf(userId));
    user.setEnabled(false);
    return true;
  }
}
