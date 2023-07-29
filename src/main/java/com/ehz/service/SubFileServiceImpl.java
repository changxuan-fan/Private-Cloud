package com.ehz.service;

import com.ehz.domain.File;
import com.ehz.domain.SubFile;
import com.ehz.repository.FileRepository;
import com.ehz.repository.SubFileRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SubFileServiceImpl implements SubFileService {

  private final SubFileRepository subFileRepository;
  private final FileRepository fileRepository;

  @Autowired
  public SubFileServiceImpl(SubFileRepository subFileRepository, FileRepository fileRepository) {
    this.subFileRepository = subFileRepository;
    this.fileRepository = fileRepository;
  }

  @Transactional
  public void createSubFile(String subFilePath, String filename, boolean isDirectory) {

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
      SubFile newSubFile = new SubFile();
      newSubFile.setFile(longestFile);
      newSubFile.setSubFilePath(newFilePath);
      newSubFile.setIsDirectory(isDirectory);
      subFileRepository.save(newSubFile);
    } else {
      throw new IllegalStateException("No matching root file found for the subFile.");
    }
  }

  public void createSubFileSafe(String filePath) {
    // TODO
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
  //    public SubFile findBySubFilePathSafe(String subFilePath) {
  //        Optional<SubFile> subFileOptional = subFileRepository.findBySubFilePath(subFilePath);
  //        if (subFileOptional.isEmpty()) {
  //
  //        }
  //    }
}
