package com.ehz.service;

import com.ehz.domain.*;
import com.ehz.repository.*;
import com.ehz.storage.StorageProperties;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
  private final UserRepository userRepository;
  private final UserFileMappingRepository userFileMappingRepository;
  private final FileRepository fileRepository;
  private final String rootLocation;
  private final PasswordEncoder passwordEncoder;

  @Autowired
  public UserServiceImpl(
      UserRepository userRepository,
      UserFileMappingRepository userFileMappingRepository,
      FileRepository fileRepository,
      StorageProperties storageProperties,
      PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.userFileMappingRepository = userFileMappingRepository;
    this.fileRepository = fileRepository;
    this.rootLocation = storageProperties.getRootLocation();
    this.passwordEncoder = passwordEncoder;
  }

  @Transactional
  public void createUser(
      String username, String password, String realName, String roleName, boolean isEnabled) {
    // Retrieve the role from the role repository

    Role role = Role.valueOf(roleName.trim());

    // Create and save the user object
    User user = new User();
    user.setUsername(username.trim());
    user.setRealName(realName.trim());
    user.setPassword(passwordEncoder.encode(password.trim()));
    user.setRole(role);
    user.setEnabled(isEnabled);
    User savedUser = userRepository.save(user);

    // Get permission
    Permission permissionNone = Permission.NONE;
    Permission permissionDisplay = Permission.DISPLAY;
    Permission permissionModify = Permission.MODIFY;

    // Get Root
    File root =
        fileRepository
            .findByFilePath(this.rootLocation)
            .orElseThrow(() -> new EntityNotFoundException("Root not exists"));

    // Create and save the userFileMapping objects
    List<File> files = fileRepository.findAll();
    List<UserFileMapping> userFileMappings;

    // Assign permission level based on Roles
    if (roleName.equals("ADMIN")) {
      userFileMappings =
          files.stream()
              .map(file -> new UserFileMapping(savedUser, file, permissionModify))
              .collect(Collectors.toList());
    } else if (roleName.equals("USER")) {
      userFileMappings =
          files.stream()
              .map(
                  file -> {
                    if (file.getFilePath()
                        .equals(this.rootLocation)) { // Set Root's permission as Display
                      return new UserFileMapping(savedUser, file, permissionDisplay);
                    } else {
                      return new UserFileMapping(savedUser, file, permissionNone);
                    }
                  })
              .collect(Collectors.toList());

    } else {
      throw new IllegalArgumentException("Invalid roleName: " + roleName);
    }

    userFileMappingRepository.saveAll(userFileMappings);
  }

  @Transactional
  public void deleteByUserId(Long userId) {
    userRepository.deleteByUserId(userId);
  }

  public User findByUsername(String username) {
    return userRepository
        .findByUsername(username)
        .orElseThrow(() -> new EntityNotFoundException("User not exists"));
  }

  @Override
  public List<User> findAllByRole(Role role) {
    return userRepository.findAllByRole(role);
  }

  public List<User> getAllUsers() {
    return userRepository.findAll();
  }

  @Override
  public User findById(Long userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new EntityNotFoundException("User not present"));
  }

  @Override
  public User save(User user) {
    return userRepository.save(user);
  }

  @Override
  public boolean existsByUsername(String username) {
    return userRepository.existsByUsername(username);
  }
}
