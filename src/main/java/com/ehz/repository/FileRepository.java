package com.ehz.repository;

import com.ehz.domain.File;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<File, Long> {
  Optional<File> findByFilePath(String filePath);

  Boolean existsByFilePath(String filePath);

  List<File> findByFilePathStartingWith(String filePath);
}
