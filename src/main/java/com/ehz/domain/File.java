package com.ehz.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table
@Data
@AllArgsConstructor
@NoArgsConstructor
public class File implements Serializable {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long fileId;

  @Column(nullable = false, unique = true, length = 1000)
  private String filePath;

  @OneToMany(mappedBy = "file")
  private List<UserFileMapping> userFileMappings;

  @OneToMany(mappedBy = "file")
  private List<SubFile> subFiles;
}
