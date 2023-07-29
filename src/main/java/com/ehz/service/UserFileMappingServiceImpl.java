package com.ehz.service;

import com.ehz.domain.File;
import com.ehz.domain.User;
import com.ehz.domain.UserFileMapping;
import com.ehz.repository.UserFileMappingRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserFileMappingServiceImpl implements UserFileMappingService {
  private final UserFileMappingRepository userFileMappingRepository;

  public UserFileMappingServiceImpl(UserFileMappingRepository userFileMappingRepository) {
    this.userFileMappingRepository = userFileMappingRepository;
  }

  @Override
  public UserFileMapping findByUserAndFile(User user, File file) {
    return userFileMappingRepository
        .findByUserAndFile(user, file)
        .orElseThrow(() -> new EntityNotFoundException("UserFileMapping not exists"));
  }
}
