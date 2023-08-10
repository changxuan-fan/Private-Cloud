package com.ehz.service;

import com.ehz.domain.*;
import com.ehz.repository.*;
import com.ehz.storage.StorageProperties;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FileServiceImpl implements FileService {
  private final UserRepository userRepository;
  private final UserFileMappingRepository userFileMappingRepository;
  private final FileRepository fileRepository;
  private final SubFileRepository subFileRepository;
  private final String rootLocation;

  @Autowired
  public FileServiceImpl(
      UserRepository userRepository,
      UserFileMappingRepository userFileMappingRepository,
      FileRepository fileRepository,
      SubFileRepository subFileRepository,
      StorageProperties storageProperties) {
    this.userRepository = userRepository;
    this.userFileMappingRepository = userFileMappingRepository;
    this.fileRepository = fileRepository;
    this.subFileRepository = subFileRepository;
    this.rootLocation = storageProperties.getRootLocation();
  }

  private static String getUploadDate() {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
    LocalDateTime now = LocalDateTime.now();
    return now.format(formatter);
  }

  @Transactional
  public void createRoot() {
    if (!fileRepository.existsByFilePath(rootLocation)) {
      File file = new File();
      file.setFilePath(rootLocation);
      file.setFilename(rootLocation);
      File savedFile = fileRepository.save(file);

      String uploadDate = getUploadDate();

      SubFile subFile = new SubFile();
      subFile.setSubFilePath(rootLocation);
      subFile.setFilename(rootLocation);
      subFile.setFile(savedFile);
      subFile.setIsDirectory(true);
      subFile.setUploadDate(uploadDate);
      subFile.setFileType("Other");

      subFileRepository.save(subFile);
    }
  }

  @Transactional
  public void createFile(String subFilePath, String filename, User uploadUser) throws IOException {
    // Save file in FileRepository
    String newFilePath = subFilePath + "/" + filename;

    if (fileRepository.existsByFilePath(newFilePath)) {
      throw new IOException("File already exists.");
    }

    File file = new File();
    file.setFilePath(newFilePath);
    file.setFilename(filename);
    File savedFile = fileRepository.save(file);

    String uploadDate = getUploadDate();

    // Create its corresponding subFile, and save it in SubFileRepository
    SubFile newSubFile = new SubFile();
    newSubFile.setFile(savedFile);
    newSubFile.setSubFilePath(savedFile.getFilePath());
    newSubFile.setIsDirectory(true);
    newSubFile.setFileType("Other");
    newSubFile.setUploadUser(uploadUser.getRealName());
    newSubFile.setUploadDate(uploadDate);
    newSubFile.setFilename(filename);
    subFileRepository.save(newSubFile);

    // Save all UserFileMapping objects in bulk
    Permission permissionDisplay = Permission.DISPLAY;
    Permission permissionModify = Permission.MODIFY;

    List<User> users = userRepository.findAll();
    List<UserFileMapping> mappings = new ArrayList<>();

    // Assign permission level based on Roles
    for (User user : users) {
      if (user.getRole() == Role.ADMIN) {
        mappings.add(new UserFileMapping(user, savedFile, permissionModify));
      } else if (user.getRole() == Role.USER) {
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

  @Transactional
  public void deleteFileAndSubFiles(File file) {
    userFileMappingRepository.deleteAll(file.getUserFileMappings());
    subFileRepository.deleteAll(file.getSubFiles());
    fileRepository.delete(file);
  }

  public boolean existsByFilePath(String subFilePath) {
    return fileRepository.existsByFilePath(subFilePath);
  }

  public File findByFilePath(String filePath) {
    return fileRepository
        .findByFilePath(filePath)
        .orElseThrow(() -> new EntityNotFoundException("File path not exists"));
  }

  @Override
  public File findById(Long fileId) {
    return fileRepository
        .findById(fileId)
        .orElseThrow(() -> new EntityNotFoundException("File Id not exists"));
  }

  @Override
  public List<File> findAll() {
    return fileRepository.findAll();
  }
}
