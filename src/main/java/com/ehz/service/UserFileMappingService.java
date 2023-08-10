package com.ehz.service;

import com.ehz.domain.File;
import com.ehz.domain.User;
import com.ehz.domain.UserFileMapping;
import java.util.List;

public interface UserFileMappingService {
  UserFileMapping findByUserAndFile(User user, File file);

  List<UserFileMapping> findAllByUser(User user);

  List<UserFileMapping> findAll();

  void deleteAllByUser(User user);

  void deleteByUserAndFile(User user, File file);

  UserFileMapping save(UserFileMapping userFileMapping);
}
