package com.ehz.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.List;
import lombok.*;

@Entity
@Table
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class User implements Serializable {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long userId;

  @Column(nullable = false, unique = true)
  private String username;

  @Column(nullable = false)
  private String realName;

  @Column(nullable = false)
  private String password;

  @OneToMany(mappedBy = "user")
  private List<UserFileMapping> userFileMappings;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false)
  private Role role;

  private boolean isEnabled = true;
}
