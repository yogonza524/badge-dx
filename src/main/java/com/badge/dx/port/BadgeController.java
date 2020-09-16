package com.badge.dx.port;

import com.badge.dx.business.BadgeStoreService;
import com.badge.dx.business.CamoEraserService;
import com.badge.dx.port.incoming.IncomingRequest;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class BadgeController {

  private final BadgeStoreService badgeStoreService;
  private final CamoEraserService camoEraserService;

  public BadgeController(BadgeStoreService badgeStoreService, CamoEraserService camoEraserService) {
    this.badgeStoreService = badgeStoreService;
    this.camoEraserService = camoEraserService;
  }

  @PostMapping("/create")
  public ResponseEntity create(@RequestBody IncomingRequest request)
      throws IOException, ExecutionException, InterruptedException, GitAPIException {
    if (log.isInfoEnabled()) log.info("request=" + request);
    return ResponseEntity.ok(badgeStoreService.generateBadge(request));
  }

  @GetMapping(value = "/{project}/{repo}/{badgeName}", produces = "image/svg+xml;charset=utf-8")
  public ResponseEntity find(
      @PathVariable String project, @PathVariable String repo, @PathVariable String badgeName)
      throws IOException, GitAPIException {
    if (log.isInfoEnabled()) log.info("repo=" + project + "/" + repo + ", badgeName=" + badgeName);
    return ResponseEntity.ok(badgeStoreService.find(project, repo, badgeName));
  }
}
