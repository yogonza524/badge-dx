package com.badge.dx.business;

import com.badge.dx.domain.Badge;
import com.badge.dx.domain.BadgeForRepo;
import com.badge.dx.domain.BadgeJsonStore;
import com.badge.dx.port.incoming.IncomingRequest;
import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LsRemoteCommand;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

@Slf4j
public class BadgeStoreService {
  private final Git git;
  private final UsernamePasswordCredentialsProvider credentials;
  private final String repoFolder =
      File.separator + "tmp" + File.separator + UUID.randomUUID().toString();
  private final String storeFileName;
  private final CamoEraserService camoEraserService;

  public BadgeStoreService(
      UsernamePasswordCredentialsProvider credentials,
      String storageGitUrl,
      String storeFileName,
      CamoEraserService camoEraserService)
      throws GitAPIException {
    this.credentials = credentials;
    this.storeFileName = storeFileName;
    this.camoEraserService = camoEraserService;
    this.git =
        Git.cloneRepository()
            .setCredentialsProvider(credentials)
            .setDirectory(new File(repoFolder))
            .setURI(storageGitUrl)
            .setBranch("master")
            .call();
  }

  public boolean existsUserRepository(String project, String repo) {
    try {
      final LsRemoteCommand lsCmd = new LsRemoteCommand(null);
      lsCmd.setRemote("https://github.com/" + project + "/" + repo + ".git").call();

    } catch (GitAPIException e) {
      if (log.isErrorEnabled())
        log.error(
            "action=existsUserRepository, project="
                + project
                + ", repo="
                + repo
                + ", msg=Not exists or is a private repo");
      e.printStackTrace();
      return false;
    }
    return true;
  }

  private void commitChanges(String message) throws GitAPIException {
    git.add().addFilepattern(".").call();
    git.commit().setMessage(message).call();

    git.push().setPushAll().setCredentialsProvider(credentials).call();
  }

  public boolean createJsonBadgeFileIfDoesntExists(String filePath)
      throws GitAPIException, IOException {
    boolean exists = Files.exists(Paths.get(filePath));
    if (!exists) {
      if (log.isInfoEnabled())
        log.info("action=createJsonBadgeFileIfDoesntExists, msg=Creating JSON file");
      Files.createFile(Paths.get(filePath));
    }
    return Files.exists(Paths.get(filePath));
  }

  public void updateJsonFile(String repoFolder, String jsonFileName, BadgeJsonStore store)
      throws GitAPIException, IOException {
    String filePath = repoFolder + "/" + jsonFileName;
    if (!Files.exists(Paths.get(filePath))) {
      boolean created = createJsonBadgeFileIfDoesntExists(filePath);
      if (!created) throw new IOException("File " + filePath + " not created!");
    }

    try (Writer writer = new FileWriter(filePath)) {
      Gson gson = new GsonBuilder().create();
      gson.toJson(store, writer);
    }
    commitChanges("Updating JSON data store");
  }

  public BadgeJsonStore readStore(String jsonFilePath)
      throws IOException, JsonSyntaxException, GitAPIException {
    if (!Files.exists(Paths.get(jsonFilePath))) {
      createJsonBadgeFileIfDoesntExists(jsonFilePath);
      commitChanges("Creating Store badges file");
    }
    // throw new IOException("File doesn't exists. Path: " + jsonFilePath);

    return new Gson()
        .fromJson(
            FileUtils.readFileToString(new File(jsonFilePath), "UTF-8"), BadgeJsonStore.class);
  }

  public void addBadgeForRepo(String repoName, Badge badge) throws IOException, GitAPIException {
    pullRepo();
    String absoluteFilePath = this.repoFolder + File.separator + this.storeFileName;
    BadgeJsonStore store =
        Optional.ofNullable(readStore(absoluteFilePath))
            .orElse(BadgeJsonStore.builder().badgeForRepos(new HashSet<>()).build());

    // Check if Repo exists
    BadgeForRepo repo =
        store
            .getBadgeForRepos()
            .stream()
            .filter(r -> r.getRepo().equalsIgnoreCase(repoName))
            .findFirst()
            .orElseGet(() -> BadgeForRepo.builder().repo(repoName).badges(new HashSet<>()).build());

    // Remove badge if exists
    repo.getBadges().remove(badge);
    repo.getBadges().add(badge);

    store.getBadgeForRepos().remove(repo);
    store.getBadgeForRepos().add(repo);

    updateJsonFile(this.repoFolder, this.storeFileName, store);
  }

  private void pullRepo() throws GitAPIException {
    git.pull()
        .setFastForward(MergeCommand.FastForwardMode.FF)
        .setRemote("origin")
        .setRemoteBranchName("master")
        .setCredentialsProvider(credentials)
        .call();
  }

  public Badge generateBadge(IncomingRequest request) throws IOException, GitAPIException {
    Objects.requireNonNull(request, "Request is required");

    final String BADGEN_URL = "https://badgen.net/badge/$subject/$status/$color?icon=$icon";

    String urlEncoded = encodeUrl(request, BADGEN_URL);

    Badge newBadge =
        Badge.builder().badgeName(request.getBadgeName()).badgenUrl(urlEncoded).build();
    addBadgeForRepo(request.getProject() + "/" + request.getRepo(), newBadge);

    // Erase cache
    new Thread(
            () -> {
              try {
                camoEraserService.eraseCache(request.getProject() + "/" + request.getRepo());
              } catch (IOException e) {
                e.printStackTrace();
              }
            })
        .start();

    String outputUrl =
        System.getenv("SERVER_URL") != null ? System.getenv("SERVER_URL") : "http://localhost:8080";

    return Badge.builder()
        .badgeName(request.getBadgeName())
        .badgenUrl(
            "!["
                + request.getBadgeName()
                + " by Badge DX]("
                + outputUrl
                + "/"
                + request.getProject()
                + "/"
                + request.getRepo()
                + "/"
                + request.getBadgeName()
                + ")")
        .build();
  }

  private String encodeUrl(IncomingRequest request, String BADGEN_URL)
      throws UnsupportedEncodingException {
    Escaper scaper = UrlEscapers.urlFragmentEscaper();
    return BADGEN_URL
        .replaceAll("\\$subject", scaper.escape(request.getSubject()))
        .replaceAll("\\$status", scaper.escape(request.getStatus()))
        .replaceAll("\\$icon", scaper.escape(request.getIcon()))
        .replaceAll("\\$color", scaper.escape(request.getColor()));
  }

  public byte[] find(String project, String repo, String badgeName)
      throws IOException, GitAPIException {
    pullRepo();
    BadgeJsonStore store = readStore(this.repoFolder + File.separator + this.storeFileName);
    Badge badge =
        store
            .getBadgeForRepos()
            .stream()
            .filter(r -> r.getRepo().equalsIgnoreCase(project + "/" + repo))
            .findFirst()
            .orElseThrow(
                () -> new RuntimeException("Repository not found! " + project + "/" + repo))
            .getBadges()
            .stream()
            .filter(b -> b.getBadgeName().equalsIgnoreCase(badgeName))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Badge not found! " + badgeName));

    String tmpImagePath = "/tmp" + File.separator + UUID.randomUUID().toString();

    if (downloadImage(badge.getBadgenUrl(), tmpImagePath)) {
      InputStream in = Files.newInputStream(Paths.get(tmpImagePath));
      return IOUtils.toByteArray(in);
    }
    throw new RuntimeException(
        "Image could not be downloaded from Badgen. " + badge.getBadgenUrl());
  }

  public boolean downloadImage(String badgenUrl, String outputAbsoluteFile) throws IOException {
    URL url = new URL(badgenUrl);
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
