package com.ehz.service;

import com.ehz.domain.*;
import com.ehz.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
  private final UserRepository userRepository;
  private final UserFileMappingRepository userFileMappingRepository;

  private final RoleRepository roleRepository;

  private final FileRepository fileRepository;
  private final PermissionRepository permissionRepository;

  @Autowired
  public UserServiceImpl(
      UserRepository userRepository,
      UserFileMappingRepository userFileMappingRepository,
      RoleRepository roleRepository,
      FileRepository fileRepository,
      PermissionRepository permissionRepository) {
    this.userRepository = userRepository;
    this.userFileMappingRepository = userFileMappingRepository;
    this.roleRepository = roleRepository;
    this.fileRepository = fileRepository;
    this.permissionRepository = permissionRepository;
  }

  @Transactional
  public void createUser(String username, String password, String roleName) {
    // Retrieve the role from the role repository
    Role role =
        roleRepository
            .findByRoleName(roleName)
            .orElseThrow(() -> new EntityNotFoundException("Role not present"));

    // Create and save the user object
    User user = new User();
    user.setUsername(username);
    user.setPassword(password);
    user.setRole(role);
    User savedUser = userRepository.save(user);

    // Get permission
    Permission permissionNone =
        permissionRepository
            .findByPermissionName("None")
            .orElseThrow(() -> new EntityNotFoundException("Permission None not present"));

    Permission permissionModify =
        permissionRepository
            .findByPermissionName("Modify")
            .orElseThrow(() -> new EntityNotFoundException("Permission Modify not present"));

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
              .map(file -> new UserFileMapping(savedUser, file, permissionNone))
              .collect(Collectors.toList());
    } else {
      throw new IllegalArgumentException("Invalid roleName: " + roleName);
    }

    userFileMappingRepository.saveAll(userFileMappings);
  }

  @Transactional
  public void deleteUser(String username) {
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(
                () -> new EntityNotFoundException("User not found with username: " + username));

    // Disconnect the user from all files
    List<UserFileMapping> userFileMappings = user.getUserFileMappings();
    userFileMappingRepository.deleteAll(userFileMappings);

    userRepository.delete(user);
  }

  public User findByUsername(String username) {
    return userRepository
        .findByUsername(username)
        .orElseThrow(() -> new EntityNotFoundException("User not exists"));
  }
}
