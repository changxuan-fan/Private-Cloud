package com.ehz.storage;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

  void init();

  void store(MultipartFile[] file, String filePath);

  Stream<Path> loadAll(String filePath);

  Path load(String filePath);

  Resource loadAsResource(String filePath);

  void delete(String filePath);

  void create(String filePath) throws FileAlreadyExistsException;
}
