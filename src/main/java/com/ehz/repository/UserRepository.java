package com.ehz.repository;

import com.ehz.domain.Role;
import com.ehz.domain.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByUsername(String username);

  List<User> findAllByRole(Role role);

  void deleteByUserId(Long userId);

  boolean existsByUsername(String username);
}
