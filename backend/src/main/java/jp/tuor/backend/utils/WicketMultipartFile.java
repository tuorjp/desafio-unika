package jp.tuor.backend.utils;

import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

//classe auxiliar que usa um FileUpload do wicket como fonte para um MultipartFile (o service de importação espera um s)
public class WicketMultipartFile implements MultipartFile {
  private final FileUpload fileUpload;

  public WicketMultipartFile(FileUpload fileUpload) {
    this.fileUpload = fileUpload;
  }

  @Override
  public String getName() {
    return fileUpload.getClientFileName();
  }

  @Override
  public String getOriginalFilename() {
    return fileUpload.getClientFileName();
  }

  @Override
  public String getContentType() {
    return fileUpload.getContentType();
  }

  @Override
  public boolean isEmpty() {
    return fileUpload.getSize() == 0;
  }

  @Override
  public long getSize() {
    return fileUpload.getSize();
  }

  @Override
  public byte[] getBytes() throws IOException {
    return fileUpload.getBytes();
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return fileUpload.getInputStream();
  }

  @Override
  public void transferTo(File dest) throws IOException, IllegalStateException {
    try {
      fileUpload.writeTo(dest);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
