package com.ehz.repository;

import com.ehz.domain.File;
import com.ehz.domain.SubFile;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubFileRepository extends JpaRepository<SubFile, UUID> {
  Optional<SubFile> findByFile(File file);

  Optional<SubFile> findBySubFilePath(String subFilePath);
}
