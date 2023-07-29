package com.ehz.service;

import com.ehz.domain.Role;
import com.ehz.repository.RoleRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl implements RoleService {
  private final RoleRepository roleRepository;

  public RoleServiceImpl(RoleRepository roleRepository) {
    this.roleRepository = roleRepository;
  }

  @Transactional
  public void createRole(String roleName) {
    Role role = new Role();
    role.setRoleName(roleName);
    roleRepository.save(role);
  }
}
