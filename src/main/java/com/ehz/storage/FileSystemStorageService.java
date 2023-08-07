package com.ehz.storage;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.office.OfficeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileSystemStorageService implements StorageService {

  private static final Map<String, String> Allowed_DOCUMENT_TYPES_To_PDF = new HashMap<>();

  static {
    Allowed_DOCUMENT_TYPES_To_PDF.put("doc", "application/msword");
    Allowed_DOCUMENT_TYPES_To_PDF.put(
        "docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    Allowed_DOCUMENT_TYPES_To_PDF.put("ppt", "application/vnd.ms-powerpoint");
    Allowed_DOCUMENT_TYPES_To_PDF.put(
        "pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
    Allowed_DOCUMENT_TYPES_To_PDF.put("xls", "application/vnd.ms-excel");
    Allowed_DOCUMENT_TYPES_To_PDF.put(
        "xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    Allowed_DOCUMENT_TYPES_To_PDF.put("txt", "text/plain");
    Allowed_DOCUMENT_TYPES_To_PDF.put("html", "text/html");
    Allowed_DOCUMENT_TYPES_To_PDF.put("csv", "text/csv");
  }

  private final String rootLocation;
  private final DocumentConverter documentConverter;

  @Autowired
  public FileSystemStorageService(
      StorageProperties properties, DocumentConverter documentConverter) {
    this.rootLocation = properties.getRootLocation();
    this.documentConverter = documentConverter;
  }

  public static Map<String, String> getAllowed_DOCUMENT_TYPES_To_PDF() {
    return Collections.unmodifiableMap(Allowed_DOCUMENT_TYPES_To_PDF);
  }

  public String appendTempAndPdf(String filename) {
    return ".temp_display_" + filename + ".pdf";
  }

  @Override
  public void store(MultipartFile[] files, String filePath) {
    try {
      Path directoryPath = Paths.get(filePath);

      // If no conflicts, copy the files, otherwise replace the existing files
      for (MultipartFile file : files) {
        String filename = file.getOriginalFilename();
        String contentType = file.getContentType();

        if (filename != null && contentType != null) {
          Path filePathInDirectory = directoryPath.resolve(filename);
          String tempFilename = appendTempAndPdf(filename);

          // Delete temp if the real file does not exist
          Path tempFilePathInDirectory = directoryPath.resolve(tempFilename);
          if (Files.exists(tempFilePathInDirectory)) {
            Files.delete(tempFilePathInDirectory);
          }

          try (InputStream inputStream = file.getInputStream()) {
            // replace existing files
            Files.copy(inputStream, filePathInDirectory, StandardCopyOption.REPLACE_EXISTING);
          }

          // Create temp file
          if (getAllowed_DOCUMENT_TYPES_To_PDF().containsValue(contentType)) {
            try (InputStream inputStream = file.getInputStream()) {
              File tempFile = tempFilePathInDirectory.toFile();
              documentConverter.convert(inputStream).to(tempFile).execute();
              tempFile.createNewFile();
            }
          }

          // Create temp file for image
          if (contentType.equals("image/png")
              || contentType.equals("image/jpeg")
              || contentType.equals("image/tiff")
              || contentType.equals("image/gif")) {
            try (InputStream imageInputStream = file.getInputStream()) {

              File tempFile = tempFilePathInDirectory.toFile();
              PDDocument doc = new PDDocument();
              PDPage page = new PDPage();
              doc.addPage(page);

              // Get image
              PDImageXObject pdImage =
                  LosslessFactory.createFromImage(doc, ImageIO.read(imageInputStream));

              // Get the image's original dimensions
              int imageWidth = pdImage.getWidth();
              int imageHeight = pdImage.getHeight();

              // Get the PDF page's dimensions
              PDRectangle mediaBox = page.getMediaBox();
              float pdfPageWidth = mediaBox.getWidth();
              float pdfPageHeight = mediaBox.getHeight();

              // Calculate the scale to fit the image within the PDF page
              float widthScale = pdfPageWidth / imageWidth;
              float heightScale = pdfPageHeight / imageHeight;
              float scale = Math.min(widthScale, heightScale);

              float newImageWidth;
              float newImageHeight;

              // Scale the image if larger than pdf page
              if (scale < 1) {
                // Calculate the new dimensions for the image
                newImageWidth = imageWidth * scale;
                newImageHeight = imageHeight * scale;
              } else {
                // Calculate the new dimensions for the image
                newImageWidth = imageWidth;
                newImageHeight = imageHeight;
              }

              //              // Calculate the positioning to center the image on the page
              //              float xPosition = (pdfPageWidth - newImageWidth) / 2;
              //              float yPosition = (pdfPageHeight - newImageHeight) / 2;

              try (PDPageContentStream contents = new PDPageContentStream(doc, page)) {

                contents.drawImage(
                    pdImage,
                    0,
                    pdfPageHeight - newImageHeight,
                    newImageWidth,
                    newImageHeight); // ?y's position
              }

              doc.save(tempFile);
              doc.close();
            }
          }
        }
      }
    } catch (IOException e) {
      throw new StorageException("Failed to store files.", e);
    } catch (OfficeException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean hasDuplicateConflict(String[] fileList, Path directoryPath) {
    // Collect all the file names to check for conflicts
    for (String filename : fileList) {
      Path filePathInDirectory = directoryPath.resolve(filename);

      if (Files.exists(filePathInDirectory)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Stream<Path> loadAll(
      String filePath) { // filePath is the relative path without starting with slash

    try {
      return Files.walk(Paths.get(filePath), 1).filter(p -> !p.equals(Paths.get(filePath)));

    } catch (IOException e) {
      throw new StorageException("Failed to read stored files", e);
    }
  }

  @Override
  public Path load(String filePath) {
    return Paths.get(filePath);
  }

  @Override
  public Resource loadAsResource(String filePath) {

    try {
      Path path = load(filePath);
      Resource resource = new UrlResource(path.toUri());

      if (!resource.exists() || !resource.isReadable()) {
        throw new StorageFileNotFoundException("Could not read file");
      }

      return resource;
    } catch (MalformedURLException e) {
      throw new StorageFileNotFoundException("Could not read file: ", e);
    }
  }

  @Override
  public File loadAsFile(String filePath) {

    Path path = load(filePath);
    File file = path.toFile();

    if (!file.exists()) {
      throw new StorageFileNotFoundException("Could not read file");
    }

    return file;
  }

  @Override
  public void delete(String filePath) {
    Path path = Paths.get(filePath);
    Path directoryPath = path.getParent();
    String tempFilename = appendTempAndPdf(String.valueOf(path.getFileName()));
    Path tempFilePathInDirectory = directoryPath.resolve(tempFilename);

    FileSystemUtils.deleteRecursively(path.toFile());

    // If temp file exists, delete it
    if (Files.isRegularFile(tempFilePathInDirectory)) {
      FileSystemUtils.deleteRecursively(tempFilePathInDirectory.toFile());
    }
  }

  @Override
  public void create(String filePath) throws FileSystemException {
    File file = new File(filePath);
    if (!file.exists()) {
      boolean isCreated = file.mkdir();
      if (!isCreated) {
        throw new FileSystemException("Illegal File Name");
      }
    } else {
      throw new FileAlreadyExistsException("The directory has already existed");
    }
  }

  public void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
    if (fileToZip.isHidden()) {
      return;
    }
    if (fileToZip.isDirectory()) {
      if (fileName.endsWith("/")) {
        zipOut.putNextEntry(new ZipEntry(fileName));
        zipOut.closeEntry();
      } else {
        zipOut.putNextEntry(new ZipEntry(fileName + "/"));
        zipOut.closeEntry();
      }
      File[] children = fileToZip.listFiles();
      for (File childFile : children) {
        zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
      }
      return;
    }
    FileInputStream fis = new FileInputStream(fileToZip);
    ZipEntry zipEntry = new ZipEntry(fileName);
    zipOut.putNextEntry(zipEntry);
    byte[] bytes = new byte[1024];
    int length;
    while ((length = fis.read(bytes)) >= 0) {
      zipOut.write(bytes, 0, length);
    }
    fis.close();
  }

  @Override
  public void init() {
    try {
      Path path = Paths.get(rootLocation);
      if (!Files.exists(path)) {
        Files.createDirectories(path);
      }
    } catch (IOException e) {
      throw new StorageException("Could not initialize storage", e);
    }
  }
}
