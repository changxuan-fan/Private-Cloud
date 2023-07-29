package com.ehz.securingweb;

import com.ehz.storage.StorageProperties;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class fileTest {
  @Autowired StorageProperties properties;

  @Test
  public void fileTest() throws IOException {

    //        // create object of Path
    //        Path path = Paths.get("Resume.pdf/sfd");
    //
    //        // call getParent() to get parent path
    //        Path parentPath = path.getParent();
    //
    //        // print ParentPath
    //        System.out.println("Parent Path: "
    //                + parentPath);

    //        Path path = Paths.get("upload-dir");
    //
    //        Stream<Path> paths = Files.walk(path, 1)
    //                .filter(p -> !p.equals(path));
    //
    //
    //        paths.forEach(System.out::println);

    //        Stream<Path> pathStream = Files.walk(Paths.get("root"), 1)
    //                .filter(p -> !p.equals(Paths.get("root")))
    //                .map(p -> Paths.get(p.toString().replace('\\', '/')));

  }
}
