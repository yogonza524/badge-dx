package com.badge.dx.port;

import com.badge.dx.business.BadgeStoreService;
import com.badge.dx.port.incoming.IncomingRequest;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
public class BadgeController {

  private final BadgeStoreService badgeStoreService;

  public BadgeController(BadgeStoreService badgeStoreService) {
    this.badgeStoreService = badgeStoreService;
  }

  @PostMapping("/create")
  public ResponseEntity create(@RequestBody IncomingRequest request)
      throws IOException, ExecutionException, InterruptedException, GitAPIException {
    return ResponseEntity.ok(badgeStoreService.generateBadge(request));
  }

  @GetMapping(value = "/{project}/{repo}/{badgeName}", produces = "image/svg+xml;charset=utf-8")
  public ResponseEntity find(
      @PathVariable String project, @PathVariable String repo, @PathVariable String badgeName,
        HttpServletResponse httpServletResponse
      )
      throws IOException, GitAPIException {
    httpServletResponse.setHeader("Cache-Control", "no-cache");
    return ResponseEntity.ok(badgeStoreService.find(project, repo, badgeName));
  }
}
