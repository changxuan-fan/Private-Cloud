package com.ehz.storage;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "storage")
@Data
public class StorageProperties {

  /** Root Folder location for storing files */
  private String rootLocation;
}
