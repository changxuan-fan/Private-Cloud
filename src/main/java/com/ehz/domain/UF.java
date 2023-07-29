package com.ehz.domain;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UF implements Serializable {
  private User user;
  private File file;
  private Permission permission;
}
