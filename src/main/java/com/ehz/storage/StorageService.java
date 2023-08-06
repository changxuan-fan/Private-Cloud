package com.ehz.storage;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.zip.ZipOutputStream;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

  void init();

  void store(MultipartFile[] file, String filePath);

  boolean hasDuplicateConflict(String[] fileList, Path directoryPath);

  Stream<Path> loadAll(String filePath);

  Path load(String filePath);

  Resource loadAsResource(String filePath);

  File loadAsFile(String filePath);

  void delete(String filePath);

  void create(String filePath) throws FileSystemException;

  void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException;
}
