package com.ehz.controller;

import com.ehz.domain.User;
import com.ehz.service.UserFileMappingService;
import com.ehz.service.UserService;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {
  private final UserService userService;
  private final UserFileMappingService userFileMappingService;

  public UserController(UserService userService, UserFileMappingService userFileMappingService) {
    this.userService = userService;
    this.userFileMappingService = userFileMappingService;
  }

  @GetMapping({"/ehz/admin", "/users", "/admin"})
  public String admin() {
    return "redirect:/ehz/admin/users";
  }

  @GetMapping("/ehz/admin/users")
  public String users(Model model) {
    List<User> users = userService.getAllUsers();
    model.addAttribute("users", users);
    return "users";
  }

  @Transactional
  @PostMapping("/ehz/admin/users/{userId}/password")
  @ResponseBody
  public boolean userPasswordUpdate(
      @PathVariable String userId,
      @RequestParam("input-password") String password,
      RedirectAttributes redirectAttributes) {

    User user = userService.findById(Long.valueOf(userId));
    user.setPassword(password);
    return true;
  }

  @Transactional
  @GetMapping("/ehz/admin/users/{userId}/delete")
  @ResponseBody
  public boolean fileDelete(@PathVariable String userId, RedirectAttributes redirectAttributes) {

    User user = userService.findById(Long.valueOf(userId));
    // Delete all userFileMapping
    userFileMappingService.deleteByUser(user);

    userService.deleteByUserId(Long.valueOf(userId));
    return true;
  }

  @Transactional
  @GetMapping("/ehz/admin/users/{userId}/enable")
  @ResponseBody
  public boolean fileEnabled(@PathVariable String userId, RedirectAttributes redirectAttributes) {

    User user = userService.findById(Long.valueOf(userId));
    user.setEnabled(true);
    return true;
  }

  @Transactional
  @GetMapping("/ehz/admin/users/{userId}/disable")
  @ResponseBody
  public boolean fileDisabled(@PathVariable String userId, RedirectAttributes redirectAttributes) {

    User user = userService.findById(Long.valueOf(userId));
    user.setEnabled(false);
    return true;
  }
}
