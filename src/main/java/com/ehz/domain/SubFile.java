package com.ehz.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.UUID;
import lombok.*;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubFile implements Serializable {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID subFileId;

  @Column(nullable = false, unique = true, length = 1000)
  private String subFilePath;

  @Column(nullable = false)
  private String filename;

  @Column(nullable = false)
  private Boolean isDirectory;

  @Column(nullable = false)
  private String fileType;

  @Column(length = 1000)
  private String description = "";

  @Column(nullable = false)
  private String uploadDate;

  @Column(nullable = false)
  private String fileSize = " — ";

  @Column(nullable = false)
  private String author = " — ";

  @Column(nullable = false)
  private String uploadUser = " — ";

  @ManyToOne
  @JoinColumn(name = "file_id")
  private File file;
}
