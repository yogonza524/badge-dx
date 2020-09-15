package com.badge.dx.business;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.springframework.stereotype.Service;

@Service
public class SvgToPngConverter {

  public String convert(String pathToSvg)
      throws IOException, ExecutionException, InterruptedException {
    String outputFile = "/tmp/" + UUID.randomUUID().toString() + ".png";

    ProcessBuilder builder = new ProcessBuilder();
    builder.directory(new File("/tmp"));
    builder.command("rsvg-convert", "-f", "png", "-o", outputFile, pathToSvg);
    int value = builder.start().onExit().get().exitValue();
    if (value == 0) return outputFile;
    throw new IOException("PNG file not generated!");
  }
}
