package com.ehz.controller;

import com.ehz.domain.File;
import com.ehz.domain.SubFile;
import com.ehz.domain.User;
import com.ehz.service.FileService;
import com.ehz.service.SubFileService;
import com.ehz.service.UserFileMappingService;
import com.ehz.service.UserService;
import com.ehz.storage.StorageFileNotFoundException;
import com.ehz.storage.StorageProperties;
import com.ehz.storage.StorageService;
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
import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
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

  @GetMapping("/ehz/files")
  public String listUploadedFiles() {
    String uuidString = subFileService.findBySubFilePath(rootLocation).getSubFileId().toString();

    return "redirect:/ehz/files/" + uuidString;
  }

  @GetMapping("/ehz/files/{uuidString}")
  public String listOrDisplayUploadedFiles(
      @PathVariable String uuidString, Model model, Principal principal)
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

      // Create fileMap that contains file attributes
      Map<String, Map<String, String>> fileMap = createFileMap(paths, principal);
      model.addAttribute("fileMap", fileMap);
      model.addAttribute("uuidString", uuidString);

      return "files";

    } else { // If uuidString refers to a file, then display the pdf form

      //            if (permission.equals("Display")) {
      // get file's byte array
      byte[] fileBytes = getFileBytes(subFilePath);
      String encodedFile =
          Base64.getEncoder().encodeToString(fileBytes); // transform bytes to base64 string
      model.addAttribute("encodedFile", encodedFile);

      return "preview";
      //            }

      //            else {
      //                Resource file = storageService.loadAsResource(subFilePath);
      //                String filename = file.getFilename();
      //                assert filename != null;
      //                // Add URLEncoder.encode() and  StandardCharsets.UTF_8 to solve unreadable
      // characters
      //                return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
      //                        "inline; filename=\"" + URLEncoder.encode(filename,
      // StandardCharsets.UTF_8) + "\"").body(file);
      //            }

    }
  }

  // Get byte array of a file
  private byte[] getFileBytes(String subFilePath) throws IOException, OfficeException {
    Path path = storageService.load(subFilePath);

    if (subFilePath.endsWith(".pdf")) {
      return Files.readAllBytes(path);
    } else {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

      // Convert
      documentConverter
          .convert(path.toFile())
          .to(outputStream)
          .as(DefaultDocumentFormatRegistry.PDF)
          .execute();

      // Converting the outputStream into a byte array
      return outputStream.toByteArray();
    }
  }

  // Get current user's permission level of the input url
  public String getAccessPermission(Principal principal, SubFile subFile) {
    User currentUser = userService.findByUsername(principal.getName());
    File currentFile = subFile.getFile();

    return userFileMappingService
        .findByUserAndFile(currentUser, currentFile)
        .getPermission()
        .getPermissionName();
  }

  // Instead of passing the whole File objects, it creates and uses a Map
  // The Map includes file attributes.
  // It filters out files without permission
  // Key: filename;  Value: filePath, fileUUID, 'fileIsDirectory'
  private Map<String, Map<String, String>> createFileMap(List<Path> paths, Principal principal) {
    Map<String, Map<String, String>> fileMap = new HashMap<>();

    for (Path path : paths) {
      String subFilePath = path.toString().replace('\\', '/'); // If the system is Windows
      SubFile subFile = subFileService.findBySubFilePath(subFilePath);

      String permission = getAccessPermission(principal, subFile);
      if (!permission.equals("None")) {
        Map<String, String> fileAttributes = new HashMap<>();

        fileAttributes.put("filePath", subFilePath);
        fileAttributes.put("uuid", subFile.getSubFileId().toString());

        fileMap.put(path.getFileName().toString(), fileAttributes);
      }
    }
    return fileMap;
  }

  @GetMapping("/ehz/files/{uuidString}/download")
  @ResponseBody
  public ResponseEntity<Resource> fileDownload(@PathVariable String uuidString, Principal principal)
      throws AccessDeniedException {
    SubFile subFile = subFileService.findById(UUID.fromString(uuidString));

    // Get user's permission for the uuidString
    String permission = getAccessPermission(principal, subFile);

    // permission cannot be None
    if (!permission.equals("Download") && !permission.equals("Modify")) {
      throw new AccessDeniedException(
          "Access Denied: User doesn't have the permission to download");
    }

    String subFilePath = subFile.getSubFilePath();
    Resource file = storageService.loadAsResource(subFilePath);
    String filename = file.getFilename();
    assert filename != null;

    // Add URLEncoder.encode() and  StandardCharsets.UTF_8 to solve unreadable characters
    return ResponseEntity.ok()
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + URLEncoder.encode(filename, StandardCharsets.UTF_8) + "\"")
        .body(file);
  }

  @PostMapping("/ehz/files/{uuidString}/create")
  public String fileCreate(
      @PathVariable String uuidString,
      @RequestParam("filename") String filename,
      Principal principal,
      RedirectAttributes redirectAttributes)
      throws IOException {
    SubFile subFile = subFileService.findById(UUID.fromString(uuidString));

    // Get user's permission for the uuidString
    String permission = getAccessPermission(principal, subFile);
    // permission cannot be None
    if (!permission.equals("Download") && !permission.equals("Modify")) {
      throw new AccessDeniedException("Access Denied: User doesn't have the permission to create");
    }

    String subFilePath = subFile.getSubFilePath();
    String filePath = subFilePath + "/" + filename;

    // Create the file in the OS
    storageService.create(filePath);

    // Create the file in the database according to whether it is root files
    if (subFilePath.equals(rootLocation)
        || Objects.equals(Paths.get(subFilePath).getParent().toString(), rootLocation)) {
      fileService.createFile(subFilePath, filename);
    } else {
      subFileService.createSubFile(subFilePath, filename, true);
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
    if (!permission.equals("Download") && !permission.equals("Modify")) {
      throw new AccessDeniedException("Access Denied: User doesn't have the permission to upload");
    }

    String subFilePath = subFile.getSubFilePath();
    if (subFilePath.equals(rootLocation)
        || subFilePath.substring(0, subFilePath.lastIndexOf('/')).equals(rootLocation)) {
      throw new IllegalArgumentException("Cannot upload files in the root directories");
    }

    storageService.store(files, subFilePath);

    // Insert files to the database
    for (MultipartFile file : files) {
      System.out.println(
          "File Name: " + file.getOriginalFilename() + "  file type: " + file.getContentType());
      subFileService.createSubFile(subFilePath, file.getOriginalFilename(), false);
    }

    redirectAttributes.addFlashAttribute(
        "message", "You successfully uploaded " + files.length + " files!");

    return "redirect:/ehz/files/" + uuidString;
  }

  @ExceptionHandler(StorageFileNotFoundException.class)
  public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
    return ResponseEntity.notFound().build();
  }
}
