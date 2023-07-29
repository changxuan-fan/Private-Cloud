package com.ehz.service;

import com.ehz.domain.File;
import com.ehz.domain.SubFile;
import java.io.IOException;

public interface FileService {
  void createFile(String subFilePath, String filename) throws IOException;

  void createRoot();

  void deleteFile(SubFile subFile, String subFilePath);

  File findByFilePath(String filePath);
}
