package com.ehz.securingweb;

import com.ehz.service.*;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class DaoTests {
  @Autowired private FileService fileService;

  @Autowired private UserService userService;
  @Autowired private RoleService roleService;
  @Autowired private PermissionService permissionService;
  @Autowired private SubFileService subFileService;

  @Test
  public void DaoTest() throws IOException {

    roleService.createRole("ADMIN");
    roleService.createRole("USER");

    permissionService.createPermission("None");
    permissionService.createPermission("Display");
    permissionService.createPermission("Download");
    permissionService.createPermission("Modify");

    fileService.createRoot();
    userService.createUser("kevin", "{noop}140", "Kevin", "ADMIN");

    userService.createUser("jack", "{noop}140", "Jack", "USER");

    //        fileService.createFile("root", "school");
    //        fileService.createFile("root", "home");
    //
    //
    //        userService.createUser("kevin", "140", "Admin");
    //        userService.createUser("tom", "333", "Admin");
    //        userService.createUser("mack", "222", "User");
    //
    //        fileService.createFile("root/home", "bedroom");
    //        fileService.createFile("root/home", "restroom");
    //
    //        UUID uuid = subFileService.findBySubFilePath("root/home/bedroom").getSubFileId();
    //        subFileService.createSubFile(uuid, "desk");
    //        subFileService.createSubFile(uuid, "table");
    //
    //        fileService.createFile("root", "mall");
    //        fileService.createFile("root/mall", "staff");
    //
    //        UUID uuid = subFileService.findBySubFilePath("root/home/bedroom").getSubFileId();
    //
    //
    //        fileService.deleteFile(uuid);
  }
}
