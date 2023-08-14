package com.ehz.controller;

import com.ehz.domain.*;
import com.ehz.service.FileService;
import com.ehz.service.SubFileService;
import com.ehz.service.UserFileMappingService;
import com.ehz.service.UserService;
import com.ehz.storage.StorageFileNotFoundException;
import com.ehz.storage.StorageProperties;
import com.ehz.storage.StorageService;
import jakarta.transaction.Transactional;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
public class FileController {

  final UserFileMappingService userFileMappingService;
  private final StorageService storageService;
  private final FileService fileService;
  private final UserService userService;
  private final SubFileService subFileService;
  private final String rootLocation;

  @Autowired
  public FileController(
      StorageService storageService,
      FileService fileService,
      UserService userService,
      SubFileService subFileService,
      UserFileMappingService userFileMappingService,
      StorageProperties storageProperties) {

    this.storageService = storageService;
    this.fileService = fileService;
    this.userService = userService;
    this.subFileService = subFileService;
    this.userFileMappingService = userFileMappingService;
    this.rootLocation = storageProperties.getRootLocation();
  }

  private static String getParentString(String filePath) {
    int lastIndex = filePath.lastIndexOf('/');
    if (lastIndex == -1) {
      // If the separator is not found, it means the file is in the root directory
      return null;
    }
    return filePath.substring(0, lastIndex);
  }

  @GetMapping({"/", "/ehz/files", "/ehz"})
  public String fileListHome() {
    // Set root directory if it does not exist
    //    fileService.createRoot();

    String uuidString = subFileService.findBySubFilePath(rootLocation).getSubFileId().toString();

    return "redirect:/ehz/files/" + uuidString;
  }

  @GetMapping("/ehz/files/{uuidString}")
  public String fileListOrDisplay(@PathVariable String uuidString, Model model, Principal principal)
      throws IOException, OfficeException {
    SubFile subFile = subFileService.findById(UUID.fromString(uuidString));

    // Get user's permission for the uuidString
    Permission permission = getAccessPermission(principal, subFile);

    // permission cannot be None
    if (permission == Permission.NONE) {
      throw new AccessDeniedException(
          "Access Denied: User doesn't have the permission to enter the url");
    }

    String subFilePath = subFile.getSubFilePath();
    // The first two layers of root can only contain directories
    boolean isInRoot;
    if (subFilePath.equals(rootLocation)) {
      isInRoot = true;
    } else {
      String parentPath = Paths.get(subFilePath).getParent().toString();
      isInRoot = Objects.equals(parentPath, rootLocation);
    }

    if (subFile.getIsDirectory()) { // If uuidString refers to a directory, then enter the directory
      List<Path> paths = storageService.loadAll(subFilePath).collect(Collectors.toList());

      List<Map<String, String>> fileParents = getFileParents(subFilePath);

      // Create fileList that contains file attributes
      List<Map<String, String>> fileList = createFileList(paths, principal);
      model.addAttribute("fileList", fileList);
      model.addAttribute("uuidString", uuidString);
      model.addAttribute("permission", permission.toString());
      model.addAttribute("isInRoot", isInRoot);
      model.addAttribute("fileParents", fileParents);
      return "files";

    } else { // If uuidString refers to a file, then display the pdf form

      byte[] fileBytes = getFileBytes(subFilePath);
      if (fileBytes != null) {
        String encodedFile =
            Base64.getEncoder().encodeToString(fileBytes); // transform bytes to base64 string
        model.addAttribute("encodedFile", encodedFile);
        return "preview";

      } else {
        return "/errors/preview-error";
      }
    }
  }

  private List<Map<String, String>> getFileParents(String subFilePath) {

    List<Map<String, String>> fileParents = new LinkedList<>();

    // Get all ancestors' filename as key, uuid as value
    while (subFilePath != null) {
      String filename = Paths.get(subFilePath).getFileName().toString();
      String ancestorUUID = subFileService.findBySubFilePath(subFilePath).getSubFileId().toString();

      Map<String, String> fileParent = new HashMap<>();
      fileParent.put("filename", filename);
      fileParent.put("ancestorUUID", ancestorUUID);
      fileParents.add(0, fileParent);

      subFilePath = getParentString(subFilePath);
    }

    return fileParents;
  }

  @GetMapping("/ehz/files/{uuidString}/parents")
  @ResponseBody
  public List<Map<String, String>> getFileParents(
      @PathVariable String uuidString, Principal principal) throws AccessDeniedException {
    SubFile subFile = subFileService.findById(UUID.fromString(uuidString));

    // Get user's permission for the uuidString
    Permission permission = getAccessPermission(principal, subFile);

    // permission cannot be None
    if (permission == Permission.NONE) {
      throw new AccessDeniedException(
          "Access Denied: User doesn't have the permission to enter the url");
    }

    String subFilePath = subFile.getSubFilePath();

    List<Map<String, String>> fileParents = getFileParents(subFilePath);

    return fileParents;
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
  public Permission getAccessPermission(Principal principal, SubFile subFile) {
    User currentUser = userService.findByUsername(principal.getName());
    File currentFile = subFile.getFile();
    UserFileMapping userFileMapping =
        userFileMappingService.findByUserAndFile(currentUser, currentFile);

    return userFileMapping.getPermission();
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

      // If file starting with ".temp_display" has no corresponding file, delete it
      if (filename.startsWith(".temp_display_") && filename.endsWith(".pdf")) {
        String filteredFilename =
            filename.substring(".temp_display_".length(), filename.length() - ".pdf".length());

        boolean exists =
            paths.stream()
                .map(p -> p.getFileName().toString())
                .anyMatch(p -> p.equals(filteredFilename));

        // If the corresponding file of "temp_display_" does not exist
        if (!exists) {
          storageService.delete(path.toString().replace('\\', '/'));
        }
        continue;
      }

      String subFilePath = path.toString().replace('\\', '/');

      // Check if the file exists in the database
      if (!subFileService.existsBySubFilePath(subFilePath)) {
        // If the file doesn't exist, create it in the database
        String parentPath = getParentString(subFilePath);
        String grandParentPath = parentPath != null ? getParentString(parentPath) : null;

        if (parentPath != null && parentPath.equals(rootLocation)
            || grandParentPath != null && grandParentPath.equals(rootLocation)) {
          if (fileService.existsByFilePath(subFilePath)) {
            fileService.deleteFileAndSubFiles(fileService.findByFilePath(subFilePath));
          }
          fileService.createFile(parentPath, filename, null);
        } else {
          subFileService.createSubFile(
              parentPath, filename, "Other", " — ", Files.isDirectory(path), null);
        }
      }

      // Fetch the file from the database and check the permission
      SubFile subFile = subFileService.findBySubFilePath(subFilePath);
      Permission permission = getAccessPermission(principal, subFile);
      String uploadUser = subFile.getUploadUser();

      if (permission != Permission.NONE) {
        // Add file attributes to the fileList
        Map<String, String> fileAttributes = new HashMap<>();
        fileAttributes.put("filename", filename);
        fileAttributes.put("filePath", subFilePath);
        fileAttributes.put("uuid", subFile.getSubFileId().toString());
        fileAttributes.put("isDirectory", String.valueOf(subFile.getIsDirectory()));
        fileAttributes.put("uploadDate", subFile.getUploadDate());
        fileAttributes.put("uploadUser", uploadUser);
        fileAttributes.put("fileSize", subFile.getFileSize());
        fileAttributes.put("fileType", subFile.getFileType());
        fileAttributes.put("permission", permission.toString());
        fileAttributes.put("description", subFile.getDescription());
        fileAttributes.put("author", subFile.getAuthor());

        fileList.add(fileAttributes);
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
    Permission permission = getAccessPermission(principal, subFile);

    // permission cannot be None
    if (permission == Permission.NONE) {
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

    Permission permission = getAccessPermission(principal, subFile);
    if (permission != Permission.MODIFY) {
      throw new AccessDeniedException("Access Denied: User doesn't have the permission to create");
    }

    String filenameTrimmed = filename.trim();
    String subFilePath = subFile.getSubFilePath();
    String filePath = subFilePath + "/" + filenameTrimmed;

    // Create the file in the system
    storageService.create(filePath);

    User currentUser = userService.findByUsername(principal.getName());

    // Create the file in the database
    // *Since we already checked that the file does not exist in the disk*
    // *Therefore, if it exists in the database, delete it*
    if (subFileService.existsBySubFilePath(filePath) && fileService.existsByFilePath(filePath)) {
      fileService.deleteFile(subFileService.findBySubFilePath(filePath), filePath);
    } else if (fileService.existsByFilePath(filePath)) {
      File file = fileService.findByFilePath(filePath);
      fileService.deleteFileAndSubFiles(file);
    } else if (subFileService.existsBySubFilePath(filePath)) {
      subFileService.deleteBySubFilePath(filePath);
    }

    if (subFilePath.equals(rootLocation)
        || Paths.get(subFilePath).getParent().toString().equals(rootLocation)) {
      fileService.createFile(subFilePath, filenameTrimmed, currentUser);
    } else {
      subFileService.createSubFile(
          subFilePath, filenameTrimmed, "Other", " — ", true, currentUser.getRealName());
    }

    return "redirect:/ehz/files/" + uuidString;
  }

  @PostMapping("/ehz/files/{uuidString}/check-duplicates")
  @ResponseBody
  public boolean checkDuplicates(
      @RequestParam("files") String[] fileList, @PathVariable String uuidString)
      throws AccessDeniedException, UnsupportedEncodingException {
    SubFile subFile = subFileService.findById(UUID.fromString(uuidString));

    String subFilePath = subFile.getSubFilePath();
    Path directoryPath = Paths.get(subFilePath);

    return storageService.hasDuplicateConflict(fileList, directoryPath);
  }

  @GetMapping("/ehz/files/{uuidString}/delete")
  @ResponseBody
  public boolean fileDelete(
      @PathVariable String uuidString, Principal principal, RedirectAttributes redirectAttributes)
      throws IOException {
    SubFile subFile = subFileService.findById(UUID.fromString(uuidString));

    // Get user's permission for the uuidString
    Permission permission = getAccessPermission(principal, subFile);
    // permission cannot be None
    if (permission != Permission.MODIFY) {
      throw new AccessDeniedException("Access Denied: User doesn't have the permission to delete");
    }

    String subFilePath = subFile.getSubFilePath();
    // Delete files in the database
    fileService.deleteFile(subFile, subFilePath);
    // Delete files in the system
    storageService.delete(subFilePath);

    return true;
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
    Permission permission = getAccessPermission(principal, subFile);
    // permission Must be the MODIFY
    if (permission != Permission.MODIFY) {
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

    // If database exists the inserting files' names, delete them
    for (MultipartFile multipartFile : files) {
      String newFilePath = subFilePath + "/" + multipartFile.getOriginalFilename();
      if (subFileService.existsBySubFilePath(newFilePath)) {
        subFileService.deleteBySubFilePath(newFilePath);
      }
    }

    // Store files to the disk
    storageService.store(files, subFilePath);

    // Insert files to the database
    for (MultipartFile multipartFile : files) {
      subFileService.createSubFile(
          subFilePath,
          multipartFile.getOriginalFilename(),
          multipartFile.getContentType(),
          FileUtils.byteCountToDisplaySize(multipartFile.getSize()),
          false,
          currentUser.getRealName());
    }

    redirectAttributes.addFlashAttribute(
        "message", "You successfully uploaded " + files.length + " files!");

    return "redirect:/ehz/files/" + uuidString;
  }

  @Transactional
  @PostMapping("/ehz/files/{uuidString}/description")
  @ResponseBody
  public boolean fileDescriptionUpdate(
      @PathVariable String uuidString,
      @RequestParam("input-description") String descriptionInput,
      Principal principal,
      RedirectAttributes redirectAttributes)
      throws AccessDeniedException {
    SubFile subFile = subFileService.findById(UUID.fromString(uuidString));

    // Get user's permission for the uuidString
    Permission permission = getAccessPermission(principal, subFile);
    // permission cannot be None
    if (permission == Permission.NONE) {
      throw new AccessDeniedException(
          "Access Denied: User doesn't have the permission to update the description");
    }

    subFile.setDescription(descriptionInput);
    return true;
  }

  @Transactional
  @PostMapping("/ehz/files/{uuidString}/author")
  @ResponseBody
  public boolean fileAuthorUpdate(
      @PathVariable String uuidString,
      @RequestParam("input-author") String authorInput,
      Principal principal,
      RedirectAttributes redirectAttributes)
      throws AccessDeniedException {
    SubFile subFile = subFileService.findById(UUID.fromString(uuidString));

    // Get user's permission for the uuidString
    Permission permission = getAccessPermission(principal, subFile);
    // permission cannot be None
    if (permission == Permission.NONE) {
      throw new AccessDeniedException(
          "Access Denied: User doesn't have the permission to update the author");
    }

    subFile.setAuthor(authorInput);
    return true;
  }

  @GetMapping("/ehz/files/{uuidString}/search")
  public String fileSearch(
      @RequestParam("q") String query,
      @PathVariable String uuidString,
      Principal principal,
      Model model)
      throws IOException {
    SubFile subFile = subFileService.findById(UUID.fromString(uuidString));
    // Get user's permission for the uuidString
    Permission permission = getAccessPermission(principal, subFile);

    // permission cannot be None
    if (permission == Permission.NONE) {
      throw new AccessDeniedException(
          "Access Denied: User doesn't have the permission to enter the url");
    }

    // Split the string by white space
    String[] subQueryList = query.split("\\s+");
    // Check for duplicate elements using Set
    Set<String> uniqueQueryList = new HashSet<>(Arrays.asList(subQueryList));

    Set<SubFile> subFileSet = new HashSet<>();

    // Search files and Get Intersections
    for (String subQuery : uniqueQueryList) {
      Set<SubFile> subFileSetTemp =
          subFileService.fileSearch(subFile.getSubFilePath(), subQuery, subQuery, subQuery);
      // If intersection is empty, add all elements from the first subFileSetTemp
      if (subFileSet.isEmpty()) {
        subFileSet.addAll(subFileSetTemp);
      } else {
        // Retain only elements that are present in both 'intersection' and 'subFileSetTemp'
        subFileSet.retainAll(subFileSetTemp);
      }
    }
    List<Map<String, String>> fileParents = getFileParents(subFile.getSubFilePath());

    // Check if the set contains the root element, and if so, remove it
    SubFile root = subFileService.findBySubFilePath(rootLocation);
    subFileSet.remove(root);

    List<Path> paths =
        subFileSet.stream().map(f -> Paths.get(f.getSubFilePath())).collect(Collectors.toList());

    List<Map<String, String>> fileList = createFileList(paths, principal);
    model.addAttribute("fileList", fileList);
    model.addAttribute("uuidString", uuidString);
    model.addAttribute("permission", "Download");
    model.addAttribute("query", query);
    model.addAttribute("fileParents", fileParents);

    return "files";
  }


  @GetMapping("/getRootSize")
  @ResponseBody
  public String getRootSize() {
    long size = FileUtils.sizeOfDirectory(new java.io.File(rootLocation));
    String readableSize = FileUtils.byteCountToDisplaySize(size);
    System.out.println(readableSize);
    return readableSize;
  }


  @ExceptionHandler(StorageFileNotFoundException.class)
  public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
    return ResponseEntity.notFound().build();
  }
}
