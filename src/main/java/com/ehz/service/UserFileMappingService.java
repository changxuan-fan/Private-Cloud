package com.ehz.service;

import com.ehz.domain.File;
import com.ehz.domain.User;
import com.ehz.domain.UserFileMapping;

public interface UserFileMappingService {
  UserFileMapping findByUserAndFile(User user, File file);
}
