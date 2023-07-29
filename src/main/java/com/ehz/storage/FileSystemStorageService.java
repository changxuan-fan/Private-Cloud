package com.ehz.storage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileSystemStorageService implements StorageService {

  private final String rootLocation;

  @Autowired
  public FileSystemStorageService(StorageProperties properties) {
    this.rootLocation = properties.getRootLocation();
  }

  @Override
  public void store(MultipartFile[] files, String filePath) {

    try {
      Path directoryPath = Paths.get(filePath);
      System.out.println(directoryPath);
      // Collect all the file names to check for conflicts
      Set<String> existingFilenames = new HashSet<>();
      for (MultipartFile file : files) {
        String filename = file.getOriginalFilename();
        assert filename != null;
        Path filePathInDirectory = directoryPath.resolve(filename);

        System.out.println(filePathInDirectory);
        if (existingFilenames.contains(filename) || Files.exists(filePathInDirectory)) {
          throw new IllegalArgumentException("File with the same name already exists: " + filename);
        }

        existingFilenames.add(filename);
      }

      // If no conflicts, copy the files
      for (MultipartFile file : files) {
        String filename = file.getOriginalFilename();
        assert filename != null;
        Path filePathInDirectory = directoryPath.resolve(filename);

        try (InputStream inputStream = file.getInputStream()) {
          Files.copy(inputStream, filePathInDirectory);
        }
      }
    } catch (IOException e) {
      throw new StorageException("Failed to store files.", e);
    }
  }

  @Override
  public Stream<Path> loadAll(
      String filePath) { // filePath is the relative path without starting with slash

    try {
      return Files.walk(Paths.get(filePath), 1).filter(p -> !p.equals(Paths.get(filePath)));

    } catch (IOException e) {
      throw new StorageException("Failed to read stored files", e);
    }
  }

  @Override
  public Path load(String filePath) {
    return Paths.get(filePath);
  }

  @Override
  public Resource loadAsResource(String filePath) {

    try {
      Path file = load(filePath);
      Resource resource = new UrlResource(file.toUri());

      if (!resource.exists() || !resource.isReadable()) {
        throw new StorageFileNotFoundException("Could not read file");
      }

      return resource;
    } catch (MalformedURLException e) {
      throw new StorageFileNotFoundException("Could not read file: ", e);
    }
  }

  @Override
  public void delete(String filePath) {
    FileSystemUtils.deleteRecursively(Paths.get(filePath).toFile());
  }

  @Override
  public void create(String filePath) throws FileAlreadyExistsException {
    File file = new File(filePath);
    if (!file.exists()) {
      file.mkdir();
    } else {
      throw new FileAlreadyExistsException("The directory has already existed");
    }
  }

  @Override
  public void init() {
    try {
      Path path = Paths.get(rootLocation);
      if (!Files.exists(path)) {
        Files.createDirectories(path);
      }
    } catch (IOException e) {
      throw new StorageException("Could not initialize storage", e);
    }
  }
}
