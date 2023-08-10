package com.ehz.controller;

import com.ehz.domain.*;
import com.ehz.service.FileService;
import com.ehz.service.UserFileMappingService;
import com.ehz.service.UserService;
import com.ehz.storage.StorageProperties;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PermissionController {
  private final UserService userService;
  private final UserFileMappingService userFileMappingService;
  private final FileService fileService;

  private final String rootLocation;

  public PermissionController(
      UserService userService,
      UserFileMappingService userFileMappingService,
      FileService fileService,
      StorageProperties storageProperties) {
    this.userService = userService;
    this.userFileMappingService = userFileMappingService;
    this.fileService = fileService;
    this.rootLocation = storageProperties.getRootLocation();
  }

  private static List<Map<String, String>> getUsers(List<User> users) {
    List<Map<String, String>> newUsers = new ArrayList<>();
    for (User user : users) {
      Map<String, String> newUser = new HashMap<>();
      newUser.put("userId", String.valueOf(user.getUserId()));
      newUser.put("username", user.getUsername());
      newUser.put("realName", user.getRealName());

      newUsers.add(newUser);
    }
    return newUsers;
  }

  private static List<Map<String, String>> getUserFileMappings(
      List<UserFileMapping> userFileMappings) {
    List<Map<String, String>> newUserFileMappings = new ArrayList<>();
    for (UserFileMapping userFileMapping : userFileMappings) {
      Map<String, String> newUserFileMapping = new HashMap<>();
      newUserFileMapping.put("userId", String.valueOf(userFileMapping.getUser().getUserId()));
      newUserFileMapping.put("fileId", String.valueOf(userFileMapping.getFile().getFileId()));
      newUserFileMapping.put("permission", String.valueOf(userFileMapping.getPermission()));
      newUserFileMappings.add(newUserFileMapping);
    }

    return newUserFileMappings;
  }

  private static List<Map<String, Object>> getFileTree(List<File> files) {
    List<File> firstFileList = new ArrayList<>();
    List<File> secondFileList = new ArrayList<>();

    for (File file : files) {
      String filePath = file.getFilePath();
      int slashCount = countSlashes(filePath);

      if (slashCount == 1) {
        firstFileList.add(file);
      } else {
        secondFileList.add(file);
      }
    }

    List<Map<String, Object>> fileTree = new ArrayList<>();

    for (File firstFile : firstFileList) {
      String firstFilePath = firstFile.getFilePath();
      List<Map<String, String>> children = new ArrayList<>();

      for (File secondFile : secondFileList) {
        String secondFilePath = secondFile.getFilePath();
        if (secondFilePath.startsWith(firstFilePath)) {
          Map<String, String> child = new HashMap<>();
          child.put("fileId", String.valueOf(secondFile.getFileId()));
          child.put("filename", secondFile.getFilename());
          children.add(child);
        }
      }
      Map<String, Object> fileLeaf = new HashMap<>();
      fileLeaf.put("fileId", String.valueOf(firstFile.getFileId()));
      fileLeaf.put("filename", firstFile.getFilename());
      fileLeaf.put("children", children);

      fileTree.add(fileLeaf);
    }
    return fileTree;
  }

  private static int countSlashes(String filePath) {
    int count = 0;
    for (int i = 0; i < filePath.length(); i++) {
      if (filePath.charAt(i) == '/') {
        count++;
      }
    }
    return count;
  }

  @GetMapping({"/permissions", "/ehz/permissions"})
  public String permissionsRedirect() {
    return "redirect:/ehz/admin/permissions";
  }

  @PostMapping("/ehz/admin/update-permissions")
  @ResponseBody
  @Transactional
  public boolean updatePermissions(
      @RequestParam("userId") String userId,
      @RequestParam("fileId") String fileId,
      @RequestParam("permission") String permission) {
    User user = userService.findById(Long.valueOf(userId));
    File file = fileService.findById(Long.valueOf(fileId));
    userFileMappingService.deleteByUserAndFile(user, file);
    UserFileMapping userFileMapping =
        new UserFileMapping(user, file, Permission.valueOf(permission));
    userFileMappingService.save(userFileMapping);
    return true;
  }

  @GetMapping("/ehz/admin/get-permissions")
  @ResponseBody
  public List<Map<String, String>> getPermissions() {

    return getUserFileMappings(userFileMappingService.findAll());
  }

  @GetMapping("/ehz/admin/permissions")
  public String permissions(Model model) {
    List<Map<String, String>> users = getUsers(userService.findAllByRole(Role.USER));
    List<Map<String, String>> userFileMappings =
        getUserFileMappings(userFileMappingService.findAll());

    List<File> files = fileService.findAll();
    files.remove(fileService.findByFilePath(rootLocation));
    List<Map<String, Object>> fileTree = getFileTree(files);

    model.addAttribute("users", users);
    model.addAttribute("userFileMappings", userFileMappings);
    model.addAttribute("fileTree", fileTree);

    return "permissions";
  }
}
