package com.ehz.service;

import com.ehz.domain.Permission;
import com.ehz.repository.PermissionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class PermissionServiceImpl implements PermissionService {
  private final PermissionRepository permissionRepository;

  public PermissionServiceImpl(PermissionRepository permissionRepository) {
    this.permissionRepository = permissionRepository;
  }

  @Transactional
  @Override
  public void createPermission(String permissionName) {
    Permission permission = new Permission();
    permission.setPermissionName(permissionName);
    permissionRepository.save(permission);
  }
}
