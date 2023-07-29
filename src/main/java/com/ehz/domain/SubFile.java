package com.ehz.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubFile implements Serializable {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID subFileId;

  @Column(nullable = false, unique = true, length = 1000)
  private String subFilePath;

  @Column(nullable = false)
  private Boolean isDirectory;

  @ManyToOne
  @JoinColumn(name = "file_id")
  private File file;
}