package com.badge.dx.port;

import com.badge.dx.business.CamoEraserService;
import java.io.IOException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CamoController {
  private final CamoEraserService camoEraserService;

  public CamoController(CamoEraserService camoEraserService) {
    this.camoEraserService = camoEraserService;
  }

  @GetMapping("/camo/{project}/{repo}")
  public ResponseEntity eraseCache(
          @PathVariable String project,
          @PathVariable String repo) throws IOException {
    this.camoEraserService.eraseCache(project + "/" + repo);
    return ResponseEntity.ok("Request for erase Camo Cache of " + project + "/" + repo + " executed");
  }
}
