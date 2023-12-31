package com.ehz.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import lombok.*;

@Entity
@Table
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@IdClass(UF.class)
public class UserFileMapping implements Serializable {
  @Id
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @Id
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "file_id")
  private File file;

  @Enumerated(EnumType.STRING)
  @Column(name = "permission", nullable = false)
  private Permission permission;
}
