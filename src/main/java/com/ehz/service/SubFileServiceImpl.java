package com.ehz.service;

import com.ehz.domain.File;
import com.ehz.domain.SubFile;
import com.ehz.domain.User;
import com.ehz.repository.FileRepository;
import com.ehz.repository.SubFileRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SubFileServiceImpl implements SubFileService {

  // Static map to store file types and their extensions
  private static final Map<String, String> fileTypeMap = new HashMap<>();

  // Static initializer to populate the map
  static {
    fileTypeMap.put("application/vnd.ms-powerpoint", "PowerPoint");
    fileTypeMap.put(
        "application/vnd.openxmlformats-officedocument.presentationml.presentation", "PowerPoint");
    fileTypeMap.put("application/msword", "Word");
    fileTypeMap.put(
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "Word");
    fileTypeMap.put("application/vnd.ms-excel", "Excel");
    fileTypeMap.put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "Excel");
    fileTypeMap.put("text/csv", "Excel");
    fileTypeMap.put("application/pdf", "PDF");
    fileTypeMap.put("video/mp4", "Video");
    fileTypeMap.put("video/x-msvideo", "Video");
    fileTypeMap.put("video/ogg", "Video");
    fileTypeMap.put("video/mpeg", "Video");
    fileTypeMap.put("video/webm", "Video");
    fileTypeMap.put("image/png", "Image");
    fileTypeMap.put("image/tiff", "Image");
    fileTypeMap.put("image/gif", "Image");
    fileTypeMap.put("image/jpeg", "Image");
    fileTypeMap.put("audio/mpeg", "Audio");
  }

  private final SubFileRepository subFileRepository;
  private final FileRepository fileRepository;

  @Autowired
  public SubFileServiceImpl(SubFileRepository subFileRepository, FileRepository fileRepository) {
    this.subFileRepository = subFileRepository;
    this.fileRepository = fileRepository;
  }

  @Transactional
  public void createSubFile(
      String subFilePath,
      String filename,
      String fileType,
      String fileSize,
      boolean isDirectory,
      User uploadUser) {

    // Find the root File with the longest file path length
    List<File> fileList = fileRepository.findAll();
    Optional<File> longestFilePath =
        fileList.stream()
            .filter(file -> subFilePath.startsWith(file.getFilePath()))
            .max(Comparator.comparingInt(file -> file.getFilePath().length()));
    File longestFile = longestFilePath.orElse(null);


    // Create the subFile
    if (longestFile != null) {
      String newFilePath = subFilePath + "/" + filename;

      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
      LocalDateTime now = LocalDateTime.now();
      String uploadDate = now.format(formatter);

      SubFile newSubFile = new SubFile();
      newSubFile.setFile(longestFile);
      newSubFile.setSubFilePath(newFilePath);
      newSubFile.setIsDirectory(isDirectory);
      newSubFile.setFileType(getTypeExtension(fileType));
      newSubFile.setUploadDate(uploadDate);
      newSubFile.setUploadUser(uploadUser);
      newSubFile.setFileSize(fileSize);
      newSubFile.setFilename(filename);
      subFileRepository.save(newSubFile);
    } else {
      throw new IllegalStateException("No matching root file found for the subFile.");
    }
  }

  public String getTypeExtension(String fileType) {
    return fileTypeMap.getOrDefault(fileType, "Other");
  }

  public SubFile findById(UUID uuid) {
    return subFileRepository
        .findById(uuid)
        .orElseThrow(() -> new EntityNotFoundException("UUID not present"));
  }

  public SubFile findBySubFilePath(String subFilePath) {

    return subFileRepository
        .findBySubFilePath(subFilePath)
        .orElseThrow(
            () -> new EntityNotFoundException("subFilePath " + subFilePath + " not present"));
  }

  public SubFile findByFile(File file) {
    return subFileRepository
        .findByFile(file)
        .orElseThrow(() -> new EntityNotFoundException("file not present"));
  }

  public boolean existsBySubFilePath(String subFilePath) {
    return subFileRepository.existsBySubFilePath(subFilePath);
  }

  public Set<SubFile> fileSearch(String subFilePath, String description, String author, String filename) {
    return subFileRepository.fileSearch(subFilePath, description, author, filename);
  }

  @Transactional
  public void deleteBySubFilePath(String subFilePath) {
     subFileRepository.deleteBySubFilePath(subFilePath);
    // Delete files inside if it is directory
    subFileRepository.deleteBySubFilePathStartingWith(subFilePath);

  }

}
