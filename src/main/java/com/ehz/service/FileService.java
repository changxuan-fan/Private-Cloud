package com.ehz.service;

import com.ehz.domain.File;
import com.ehz.domain.SubFile;
import com.ehz.domain.User;
import java.io.IOException;

public interface FileService {
  void createFile(String subFilePath, String filename, User uploadUser) throws IOException;

  void createRoot();

  void deleteFile(SubFile subFile, String subFilePath);

  File findByFilePath(String filePath);
}
