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
  @Autowired private SubFileService subFileService;

  @Test
  public void DaoTest() throws IOException {}
}
