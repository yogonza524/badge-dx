package com.badge.dx.business;

import com.badge.dx.port.incoming.IncomingRequest;
import java.io.IOException;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class BadgeService {
  private final ImageDownloader imageDownloader;
  private final SvgToPngConverter svgToPngConverter;
  private final GithubService githubService;

  public BadgeService(
      ImageDownloader imageDownloader,
      SvgToPngConverter svgToPngConverter,
      GithubService githubService) {
    this.imageDownloader = imageDownloader;
    this.svgToPngConverter = svgToPngConverter;
    this.githubService = githubService;
  }

  public ResponseEntity generate(IncomingRequest request)
      throws IOException, ExecutionException, InterruptedException, GitAPIException {
    String path = "/home/gonzalo.mendoza/Downloads/" + UUID.randomUUID().toString() + ".svg";
    boolean result =
        imageDownloader.downloadImage(
            request.getSubject(), request.getStatus(), request.getColor(), request.getIcon(), path);
    if (result) {
      String pngPath = svgToPngConverter.convert(path);
      String urlToPng = githubService.uploadFile(request, pngPath);
      return ResponseEntity.ok(Collections.singletonMap("path", urlToPng));
    }
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(Collections.singletonMap("error", "internal"));
  }
}
