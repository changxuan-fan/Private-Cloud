package com.ehz.controller;

import com.ehz.domain.File;
import com.ehz.domain.SubFile;
import com.ehz.domain.User;
import com.ehz.domain.UserFileMapping;
import com.ehz.service.FileService;
import com.ehz.service.SubFileService;
import com.ehz.service.UserFileMappingService;
import com.ehz.service.UserService;
import com.ehz.storage.StorageFileNotFoundException;
import com.ehz.storage.StorageProperties;
import com.ehz.storage.StorageService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.FileUtils;
import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.office.OfficeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class FileUploadController {

  final UserFileMappingService userFileMappingService;
  private final StorageService storageService;
  private final FileService fileService;
  private final UserService userService;
  private final SubFileService subFileService;
  private final String rootLocation;
  private final DocumentConverter documentConverter;

  @Autowired
  public FileUploadController(
      StorageService storageService,
      FileService fileService,
      UserService userService,
      SubFileService subFileService,
      UserFileMappingService userFileMappingService,
      StorageProperties storageProperties,
      DocumentConverter documentConverter) {

    this.storageService = storageService;
    this.fileService = fileService;
    this.userService = userService;
    this.subFileService = subFileService;
    this.userFileMappingService = userFileMappingService;
    this.rootLocation = storageProperties.getRootLocation();
    this.documentConverter = documentConverter;
  }

  @GetMapping({"/", "/ehz/files"})
  public String fileListHome() {
    String uuidString = subFileService.findBySubFilePath(rootLocation).getSubFileId().toString();

    return "redirect:/ehz/files/" + uuidString;
  }

  @GetMapping("/ehz/files/{uuidString}")
  public String fileListOrDisplay(@PathVariable String uuidString, Model model, Principal principal)
      throws IOException, OfficeException {
    SubFile subFile = subFileService.findById(UUID.fromString(uuidString));

    // Get user's permission for the uuidString
    String permission = getAccessPermission(principal, subFile);

    // permission cannot be None
    if (permission.equals("None")) {
      throw new AccessDeniedException(
          "Access Denied: User doesn't have the permission to enter the url");
    }

    String subFilePath = subFile.getSubFilePath();
    if (subFile.getIsDirectory()) { // If uuidString refers to a directory, then enter the directory
      List<Path> paths = storageService.loadAll(subFilePath).collect(Collectors.toList());

      // Create fileList that contains file attributes
      List<Map<String, String>> fileList = createFileList(paths, principal);
      model.addAttribute("fileList", fileList);
      model.addAttribute("uuidString", uuidString);
      model.addAttribute("permission", permission);
      return "files";

    } else { // If uuidString refers to a file, then display the pdf form

      //            if (permission.equals("Display")) {
      // get file's byte array
      byte[] fileBytes = getFileBytes(subFilePath);
      if (fileBytes != null) {
        String encodedFile =
            Base64.getEncoder().encodeToString(fileBytes); // transform bytes to base64 string
        model.addAttribute("encodedFile", encodedFile);
        return "preview";

      } else {
        throw new AccessDeniedException("File type is not available to open");
      }
    }
  }

  // Get byte array of a file
  private byte[] getFileBytes(String subFilePath) throws IOException {

    Path path = storageService.load(subFilePath);
    String fileName = path.getFileName().toString();
    String tempPdfName = ".temp_display_" + fileName + ".pdf";

    Path tempPdfPath = path.getParent().resolve(tempPdfName);

    if (subFilePath.endsWith(".pdf")) {
      return Files.readAllBytes(path);
    }

    if (Files.exists(tempPdfPath)) {
      return Files.readAllBytes(tempPdfPath);
    }

    return null;
  }

  // Get current user's permission level of the input url
  public String getAccessPermission(Principal principal, SubFile subFile) {
    User currentUser = userService.findByUsername(principal.getName());
    File currentFile = subFile.getFile();
    UserFileMapping userFileMapping =
        userFileMappingService.findByUserAndFile(currentUser, currentFile);

    return userFileMapping.getPermission().getPermissionName();
  }

  // Instead of passing the whole File objects, it creates and uses a Map
  // The Map includes file attributes.
  // It filters out files without permission
  // Key: filename;  Value: filePath, fileUUID, 'fileIsDirectory'
  private List<Map<String, String>> createFileList(List<Path> paths, Principal principal)
      throws IOException {
    List<Map<String, String>> fileList = new ArrayList<>();

    for (Path path : paths) {
      String filename = path.getFileName().toString();

      // Skip files starting with ".temp_"
      if (filename.startsWith(".temp_")) {
        continue;
      }

      String subFilePath = path.toString().replace('\\', '/');

      try {
        // Check if the file exists in the database
        SubFile subFile = subFileService.findBySubFilePath(subFilePath);

      } catch (EntityNotFoundException e) {
        // If the file doesn't exist, create it in the database
        Path modifiedPath = Paths.get(subFilePath);
        Path parentPath = modifiedPath.getParent();
        Path grandParentPath = parentPath != null ? parentPath.getParent() : null;

        if (parentPath != null && parentPath.toString().equals(rootLocation)) {
          fileService.createFile(parentPath.toString(), filename, null);
        } else if (grandParentPath != null && grandParentPath.toString().equals(rootLocation)) {
          fileService.createFile(parentPath.toString(), filename, null);
        } else {
          subFileService.createSubFile(
              parentPath.toString(), filename, "Other", " — ", Files.isDirectory(path), null);
        }

      } finally {
        // Fetch the file from the database and check the permission
        SubFile subFile = subFileService.findBySubFilePath(subFilePath);
        String permission = getAccessPermission(principal, subFile);
        User uploadUser = subFile.getUploadUser();
        String uploadUserRealName = (uploadUser != null) ? uploadUser.getRealName() : "";

        if (!"None".equals(permission)) {
          // Add file attributes to the fileList
          Map<String, String> fileAttributes = new HashMap<>();
          fileAttributes.put("filename", filename);
          fileAttributes.put("filePath", subFilePath);
          fileAttributes.put("uuid", subFile.getSubFileId().toString());
          fileAttributes.put("isDirectory", String.valueOf(subFile.getIsDirectory()));
          fileAttributes.put("uploadDate", subFile.getUploadDate());
          fileAttributes.put("uploadUser", uploadUserRealName);
          fileAttributes.put("fileSize", subFile.getFileSize());
          fileAttributes.put("fileType", subFile.getFileType());
          fileAttributes.put("permission", permission);
          fileAttributes.put("description", subFile.getDescription()); // Could be null
          fileList.add(fileAttributes);
        }
      }
    }

    return fileList;
  }

  @GetMapping("/ehz/files/{uuidString}/download")
  @ResponseBody
  public ResponseEntity<byte[]> fileDownload(@PathVariable String uuidString, Principal principal)
      throws IOException {
    SubFile subFile = subFileService.findById(UUID.fromString(uuidString));

    // Get user's permission for the uuidString
    String permission = getAccessPermission(principal, subFile);

    // permission cannot be None
    if (!permission.equals("Download") && !permission.equals("Modify")) {
      throw new AccessDeniedException(
          "Access Denied: User doesn't have the permission to download");
    }
    String subFilePath = subFile.getSubFilePath();
    String filename;
    byte[] fileByteArray;

    if (subFile.getIsDirectory()) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ZipOutputStream zipOut = new ZipOutputStream(baos);

      java.io.File file = storageService.loadAsFile(subFilePath);

      // Zip the directory
      storageService.zipFile(file, file.getName(), zipOut);
      filename = file.getName() + ".zip";

      // Close Stream first, Be careful of the order
      zipOut.close();
      baos.close();
      fileByteArray = baos.toByteArray();

    } else {
      Resource file = storageService.loadAsResource(subFilePath);
      filename = file.getFilename();
      fileByteArray = file.getContentAsByteArray();
    }

    assert filename != null;
    return ResponseEntity.ok()
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + URLEncoder.encode(filename, StandardCharsets.UTF_8) + "\"")
        .body(fileByteArray);
  }

  // the file created is a **Directory**
  @PostMapping("/ehz/files/{uuidString}/create")
  public String fileCreate(
      @PathVariable String uuidString,
      @RequestParam("filename") String filename,
      Principal principal,
      RedirectAttributes redirectAttributes)
      throws IOException {
    SubFile subFile = subFileService.findById(UUID.fromString(uuidString));

    String permission = getAccessPermission(principal, subFile);
    if (!permission.equals("Download") && !permission.equals("Modify")) {
      throw new AccessDeniedException("Access Denied: User doesn't have the permission to create");
    }

    String subFilePath = subFile.getSubFilePath();
    String filePath = Paths.get(subFilePath, filename).toString();

    // Create the file in the system
    storageService.create(filePath);

    User currentUser = userService.findByUsername(principal.getName());

    // Create the file in the database
    if (subFilePath.equals(rootLocation)
        || Paths.get(subFilePath).getParent().toString().equals(rootLocation)) {
      fileService.createFile(subFilePath, filename, currentUser);
    } else {
      subFileService.createSubFile(subFilePath, filename, "Other", " — ", true, currentUser);
    }

    return "redirect:/ehz/files/" + uuidString;
  }

  @GetMapping("/ehz/files/{uuidString}/delete")
  public String fileDelete(
      @PathVariable String uuidString,
      @RequestParam("uuidOriginal") String uuidOriginal,
      Principal principal,
      RedirectAttributes redirectAttributes)
      throws IOException {
    SubFile subFile = subFileService.findById(UUID.fromString(uuidString));

    // Get user's permission for the uuidString
    String permission = getAccessPermission(principal, subFile);
    // permission cannot be None
    if (!permission.equals("Modify")) {
      throw new AccessDeniedException("Access Denied: User doesn't have the permission to delete");
    }

    String subFilePath = subFile.getSubFilePath();
    // Delete files in the database
    fileService.deleteFile(subFile, subFilePath);
    // Delete files in the system
    storageService.delete(subFilePath);

    return "redirect:/ehz/files/" + uuidOriginal;
  }

  @PostMapping("/ehz/files/{uuidString}/upload")
  public String fileUpload(
      @PathVariable String uuidString,
      @RequestParam("files") MultipartFile[] files,
      Principal principal,
      RedirectAttributes redirectAttributes)
      throws AccessDeniedException {
    SubFile subFile = subFileService.findById(UUID.fromString(uuidString));

    // Get user's permission for the uuidString
    String permission = getAccessPermission(principal, subFile);
    // permission cannot be None
    if (!permission.equals("Modify")) {
      throw new AccessDeniedException("Access Denied: User doesn't have the permission to upload");
    }

    String subFilePath = subFile.getSubFilePath();
    String parentPath = Paths.get(subFilePath).getParent().toString();

    // The first two layers of root can only contain directories
    if (subFilePath.equals(rootLocation) || Objects.equals(parentPath, rootLocation)) {
      throw new IllegalArgumentException("Cannot upload files in the root directories");
    }

    storageService.store(files, subFilePath);

    User currentUser = userService.findByUsername(principal.getName());
    // Insert files to the database
    for (MultipartFile multipartFile : files) {
      subFileService.createSubFile(
          subFilePath,
          multipartFile.getOriginalFilename(),
          multipartFile.getContentType(),
          FileUtils.byteCountToDisplaySize(multipartFile.getSize()),
          false,
          currentUser);
    }

    redirectAttributes.addFlashAttribute(
        "message", "You successfully uploaded " + files.length + " files!");

    return "redirect:/ehz/files/" + uuidString;
  }

  @Transactional
  @PostMapping("/ehz/files/{uuidString}/description")
  public String fileDescriptionUpdate(
      @PathVariable String uuidString,
      @RequestParam("uuidOriginal") String uuidOriginal,
      @RequestParam("input-description") String descriptionInput,
      Principal principal,
      RedirectAttributes redirectAttributes)
      throws AccessDeniedException {
    SubFile subFile = subFileService.findById(UUID.fromString(uuidString));

    // Get user's permission for the uuidString
    String permission = getAccessPermission(principal, subFile);
    // permission cannot be None
    if (permission.equals("None")) {
      throw new AccessDeniedException("Access Denied: User doesn't have the permission to upload");
    }

    subFile.setDescription(descriptionInput);
    return "redirect:/ehz/files/" + uuidOriginal;
  }

  @GetMapping("/ehz/files/{uuidString}/search")
  public String fileSearch(
      @RequestParam("q") String query,
      @PathVariable String uuidString,
      Principal principal,
      Model model)
      throws IOException {
    SubFile subFile = subFileService.findById(UUID.fromString(uuidString));
    List<SubFile> subFileList = subFile.getFile().getSubFiles();

    List<Path> paths =
        subFileList.stream()
            .filter(file -> file.getDescription().contains(query))
            .map(f -> Paths.get(f.getSubFilePath()))
            .collect(Collectors.toList());

    List<Map<String, String>> fileList = createFileList(paths, principal);
    model.addAttribute("fileList", fileList);
    model.addAttribute("uuidString", uuidString);

    return "file_search";
  }

  @ExceptionHandler(StorageFileNotFoundException.class)
  public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
    return ResponseEntity.notFound().build();
  }
}
