package com.ehz.domain;

import jakarta.persistence.*;

public enum Permission {
  NONE,
  DISPLAY,
  DOWNLOAD,
  MODIFY
}
