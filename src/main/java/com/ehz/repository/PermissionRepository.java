package com.ehz.repository;

import com.ehz.domain.Permission;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
  Optional<Permission> findByPermissionName(String permissionName);
}
