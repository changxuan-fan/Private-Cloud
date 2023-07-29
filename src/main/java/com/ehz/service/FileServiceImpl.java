package com.ehz.service;

import com.ehz.domain.*;
import com.ehz.repository.*;
import com.ehz.storage.StorageProperties;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FileServiceImpl implements FileService {
  private final UserRepository userRepository;
  private final UserFileMappingRepository userFileMappingRepository;
  private final FileRepository fileRepository;
  private final PermissionRepository permissionRepository;
  private final SubFileRepository subFileRepository;
  private final String rootLocation;

  @Autowired
  public FileServiceImpl(
      UserRepository userRepository,
      UserFileMappingRepository userFileMappingRepository,
      FileRepository fileRepository,
      PermissionRepository permissionRepository,
      SubFileRepository subFileRepository,
      StorageProperties storageProperties) {
    this.userRepository = userRepository;
    this.userFileMappingRepository = userFileMappingRepository;
    this.fileRepository = fileRepository;
    this.permissionRepository = permissionRepository;
    this.subFileRepository = subFileRepository;
    this.rootLocation = storageProperties.getRootLocation();
  }

  @Transactional
  public void createRoot() {
    File file = new File();
    file.setFilePath(rootLocation);
    File savedFile = fileRepository.save(file);

    SubFile subFile = new SubFile();
    subFile.setSubFilePath(rootLocation);
    subFile.setFile(savedFile);
    subFile.setIsDirectory(true);
    subFileRepository.save(subFile);
  }

  @Transactional
  public void createFile(String subFilePath, String filename) throws IOException {
    // Save file in FileRepository
    String newFilePath = subFilePath + "/" + filename;

    if (fileRepository.existsByFilePath(newFilePath)) {
      throw new IOException("File already exists.");
    }

    File file = new File();
    file.setFilePath(newFilePath);
    File savedFile = fileRepository.save(file);

    // Create its corresponding subFile, and save it in SubFileRepository
    SubFile newSubFile = new SubFile();
    newSubFile.setFile(savedFile);
    newSubFile.setSubFilePath(savedFile.getFilePath());
    newSubFile.setIsDirectory(true);
    subFileRepository.save(newSubFile);

    // Save all UserFileMapping objects in bulk
    Permission permissionDisplay =
        permissionRepository
            .findByPermissionName("Display")
            .orElseThrow(() -> new EntityNotFoundException("Permission Display not present"));
    Permission permissionModify =
        permissionRepository
            .findByPermissionName("Modify")
            .orElseThrow(() -> new EntityNotFoundException("Permission Modify not present"));

    List<User> users = userRepository.findAll();
    List<UserFileMapping> mappings = new ArrayList<>();

    // Assign permission level based on Roles
    for (User user : users) {
      if (user.getRole().getRoleName().equals("ADMIN")) {
        mappings.add(new UserFileMapping(user, savedFile, permissionModify));
      } else if (user.getRole().getRoleName().equals("USER")) {
        mappings.add(new UserFileMapping(user, savedFile, permissionDisplay));
      } else {
        // Handle other cases or throw an exception
        throw new IllegalArgumentException("Invalid role for user: " + user.getUsername());
      }
    }

    userFileMappingRepository.saveAll(mappings);
  }

  @Transactional
  public void deleteFile(SubFile subFile, String subFilePath) {

    // Delete the entry in subFile
    subFileRepository.delete(subFile);

    File file = subFile.getFile();
    String filePath = file.getFilePath();

    // Delete the entry in File
    if (subFilePath.equals(filePath)) {

      // Delete file, subFiles, and userFileMappings in the database
      deleteFileAndSubFiles(file);

      // If the file deleted is *directly* under the root
      Path subPath = Paths.get(filePath).getParent();
      if (subPath != null && subPath.toString().equals(rootLocation)) {
        List<File> files = fileRepository.findByFilePathStartingWith(filePath);
        for (File f : files) {
          deleteFileAndSubFiles(f);
        }
      }
    }
  }

  private void deleteFileAndSubFiles(File file) {
    userFileMappingRepository.deleteAll(file.getUserFileMappings());
    subFileRepository.deleteAll(file.getSubFiles());
    fileRepository.delete(file);
  }

  public File findByFilePath(String filePath) {
    return fileRepository
        .findByFilePath(filePath)
        .orElseThrow(() -> new EntityNotFoundException("File path not exists"));
  }
}
