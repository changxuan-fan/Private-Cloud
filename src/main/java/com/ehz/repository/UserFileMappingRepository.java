package com.ehz.repository;

import com.ehz.domain.File;
import com.ehz.domain.User;
import com.ehz.domain.UserFileMapping;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserFileMappingRepository extends JpaRepository<UserFileMapping, Long> {
  Optional<UserFileMapping> findByUserAndFile(User user, File file);

  List<UserFileMapping> findAllByUser(User user);

  void deleteByUser(User user);
}
