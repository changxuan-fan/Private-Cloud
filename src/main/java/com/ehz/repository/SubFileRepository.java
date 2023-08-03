package com.ehz.repository;

import com.ehz.domain.File;
import com.ehz.domain.SubFile;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SubFileRepository extends JpaRepository<SubFile, UUID> {
  Optional<SubFile> findByFile(File file);

  Optional<SubFile> findBySubFilePath(String subFilePath);

  boolean existsBySubFilePath(String subFilePath);

  void deleteBySubFilePath(String subFilePath);

  void deleteBySubFilePathStartingWith(String subFilePath);

  @Query(
      "SELECT s FROM SubFile s "
          + "WHERE (s.subFilePath LIKE CONCAT(:subFilePath, '/%')) "
          + "AND (LOWER(s.description) LIKE CONCAT('%', LOWER(:description), '%') "
          + "OR LOWER(s.author) LIKE CONCAT('%', LOWER(:author), '%') "
          + "OR LOWER(s.filename) LIKE CONCAT('%', LOWER(:filename), '%'))")
  Set<SubFile> fileSearch(String subFilePath, String description, String author, String filename);
}
