package com.badge.dx.business;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.springframework.stereotype.Service;

@Service
public class ImageDownloader {
  public static final String URL_BASE =
      "https://badgen.net/badge/$subject/$status/$color?icon=$icon";

  public boolean downloadImage(
      String subject, String status, String color, String icon, String outputAbsoluteFile)
      throws IOException {
    String urlEncoded =
        URLEncoder.encode(
            URL_BASE
                .replaceAll("\\$subject", subject)
                .replaceAll("\\$status", status)
                .replaceAll("\\$icon", icon)
                .replaceAll("\\$color", color),
            StandardCharsets.UTF_8);
    URL url =
        new URL(
            URL_BASE
                .replaceAll("\\$subject", URLEncoder.encode(subject, StandardCharsets.UTF_8))
                .replaceAll("\\$status", URLEncoder.encode(status, StandardCharsets.UTF_8))
                .replaceAll("\\$icon", URLEncoder.encode(icon, StandardCharsets.UTF_8))
                .replaceAll("\\$color", URLEncoder.encode(color, StandardCharsets.UTF_8)));
    InputStream in = new BufferedInputStream(url.openStream());
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte[] buf = new byte[1024];
    int n = 0;
    while (-1 != (n = in.read(buf))) {
      out.write(buf, 0, n);
    }
    out.close();
    in.close();
    byte[] response = out.toByteArray();

    FileOutputStream fos = new FileOutputStream(outputAbsoluteFile);
    fos.write(response);
    fos.close();

    return Files.exists(Paths.get(outputAbsoluteFile));
  }
}
