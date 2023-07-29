package com.ehz.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Role implements Serializable {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long roleId;

  @Column(nullable = false, unique = true)
  private String roleName;

  @OneToMany(mappedBy = "role")
  private List<User> users;
}
