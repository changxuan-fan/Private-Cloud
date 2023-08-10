package com.ehz.domain;

import java.io.Serializable;
import lombok.*;

@Data
@Getter
@Setter
public class UF implements Serializable {
  private User user;
  private File file;
  private Permission permission;
}
