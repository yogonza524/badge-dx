package com.badge.dx.business;

import com.badge.dx.domain.BadgeForRepo;
import com.badge.dx.domain.BadgeJsonStore;
import com.badge.dx.port.incoming.IncomingRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.LsRemoteCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GithubService {
  private final String storageUrlRepo;

  private final UsernamePasswordCredentialsProvider gitCredentials;

  public GithubService(String storageUrlRepo, UsernamePasswordCredentialsProvider gitCredentials) {
    this.storageUrlRepo = storageUrlRepo;
    this.gitCredentials = gitCredentials;
  }

  private String extractRepoName(String repoUrl) {
    return repoUrl.replaceAll("(.+)\\/", "").replaceAll(".git", "");
  }

  public void cloneRepo(String branch) throws GitAPIException {
    if (Files.exists(Paths.get(extractRepoName(storageUrlRepo)))) return;
    if (log.isInfoEnabled()) {
      log.info("action=cloneRepo, msg=cloning, repo=" + storageUrlRepo);
    }
    Git.cloneRepository()
        .setCredentialsProvider(gitCredentials)
        .setURI(storageUrlRepo)
        .setBranchesToClone(Collections.singleton(branch))
        .call();
  }

  public boolean repoExists(String project, String repo) {
    try {
      final LsRemoteCommand lsCmd = new LsRemoteCommand(null);
      lsCmd.setRemote("https://github.com/" + project + "/" + repo + ".git").call();

    } catch (GitAPIException e) {
      if (log.isErrorEnabled())
        log.error(
            "action=cloneRepo, project="
                + project
                + ", repo="
                + repo
                + ", msg=Not exists or is a private repo");
      e.printStackTrace();
      return false;
    }
    return true;
  }

  public boolean createBranchIfNotExistsForRepo(String project, String repo) {
    try {
      String repoFolder =
          "/tmp/" + extractRepoName(storageUrlRepo) + "-" + UUID.randomUUID().toString();
      Git client = null;
      if (Files.exists(Paths.get(repoFolder))) {
        client = Git.open(new File(repoFolder));
      } else {
        client =
            Git.cloneRepository()
                .setURI(storageUrlRepo)
                .setCredentialsProvider(gitCredentials)
                .setDirectory(new File(repoFolder))
                .setBranchesToClone(Arrays.asList("refs/heads/master"))
                .call();
      }
      boolean existsBranch = existsBranchAtStorage(client, project, repo);
      if (!existsBranch) {
        Ref newBranch = client.branchCreate().setName(project + "/" + repo).call();

        client.push().add(newBranch).setCredentialsProvider(gitCredentials).call();

        return true;
      }

    } catch (IOException | GitAPIException e) {
      if (log.isErrorEnabled())
        log.error("action=createBranchForRepo, error=" + e + ", storageUrl=" + storageUrlRepo);
      return false;
    }
    return true;
  }

  private boolean existsBranchAtStorage(Git client, String project, String repo) {
    try {
      List<Ref> branches =
          client
              .branchList()
              .setListMode(ListBranchCommand.ListMode.REMOTE)
              .setContains("refs/heads/" + project + "/" + repo)
              .call();

      return branches
              .stream()
              .filter(branch -> branch.getName().contains(project + "/" + repo))
              .count()
          == 1;
    } catch (GitAPIException e) {
      if (log.isErrorEnabled()) log.error("error=" + e);
    }
    return false;
  }

  public boolean pushFile(Git client, String pathToFile, IncomingRequest request)
      throws GitAPIException, IOException {
    // Ref checkout = client.checkout().setName(project + "/" + repo).call();
    System.out.println("Current branch: " + client.getRepository().getBranch());
    System.out.println(
        "Repo: "
            + client
                .getRepository()
                .getDirectory()
                .getAbsolutePath()
                .substring(
                    0, client.getRepository().getDirectory().getAbsolutePath().length() - 5));
    if (Files.exists(Paths.get(pathToFile))) {
      System.out.println("Exists");
    }

    String dest =
        client
                .getRepository()
                .getDirectory()
                .getAbsolutePath()
                .substring(0, client.getRepository().getDirectory().getAbsolutePath().length() - 5)
            + "/"
            + request.getBadgeName();

    FileUtils.copyFile(new File(pathToFile), new File(dest));

    client.add().addFilepattern(".").call();
    RevCommit commit =
        client.commit().setMessage("Auto generating badge for " + request.getProject()).call();
    client.status().call().getAdded().stream().forEach(System.out::println);
    client.push().setPushAll().setCredentialsProvider(gitCredentials).call();
    return true;
  }

  public String uploadFile(IncomingRequest request, String pngPath)
      throws GitAPIException, IOException {
    if (!createBranchIfNotExistsForRepo(request.getProject(), request.getRepo()))
      throw new RuntimeException("Problems creating branch for project: " + request.getProject());

    String repoFolder = "/tmp/badge-dx-static-" + UUID.randomUUID().toString();
    Git git =
        Git.cloneRepository()
            .setCredentialsProvider(gitCredentials)
            .setDirectory(new File(repoFolder))
            .setURI("https://github.com/yogonza524/badge-dx-static.git")
            .setBranch(request.getProject() + "/" + request.getRepo())
            .call();
    pushFile(git, pngPath, request);
    return null;
  }

  public String createJsonBadgeFileIfDoesntExists(
      UsernamePasswordCredentialsProvider credentials, String jsonName)
      throws GitAPIException, IOException {
    String repoFolder = "/tmp/badge-dx-static-" + UUID.randomUUID().toString();
    Git git =
        Git.cloneRepository()
            .setCredentialsProvider(credentials)
            .setDirectory(new File(repoFolder))
            .setURI("https://github.com/yogonza524/badge-dx-static.git")
            .setBranch("master")
            .call();

    String filePath = repoFolder + "/" + jsonName + ".json";
    boolean exists = Files.exists(Paths.get(filePath));
    if (!exists) {
      if (log.isInfoEnabled())
        log.info("action=createJsonBadgeFileIfDoesntExists, msg=Creating JSON file");
      Files.createFile(Paths.get(filePath));

      commitChanges(git, credentials, "Creating JSON Database file");
    }
    return filePath;
  }

  private void commitChanges(
      Git git, UsernamePasswordCredentialsProvider credentials, String message)
      throws GitAPIException {
    git.add().addFilepattern(".").call();
    git.commit().setMessage(message).call();

    git.push().setPushAll().setCredentialsProvider(credentials).call();
  }

  public void updateJsonFile(
      Git git,
      UsernamePasswordCredentialsProvider credentials,
      String jsonPathFile,
      BadgeJsonStore store)
      throws GitAPIException, IOException {
    if (!Files.exists(Paths.get(jsonPathFile))) {
      jsonPathFile = createJsonBadgeFileIfDoesntExists(credentials, jsonPathFile);
    }

    try (Writer writer = new FileWriter(jsonPathFile)) {
      Gson gson = new GsonBuilder().create();
      gson.toJson(store, writer);
    }
    commitChanges(git, credentials, "Updating JSON data store");
  }

  public BadgeJsonStore readStore(String jsonFilePath) throws IOException {
    if (!Files.exists(Paths.get(jsonFilePath))) throw new IOException("File doesn't exists");

    return new Gson().fromJson(Files.readString(Paths.get(jsonFilePath)), BadgeJsonStore.class);
  }

  public void addBadgeForRepo(
      Git git,
      UsernamePasswordCredentialsProvider credentialsProvider,
      String jsonFilePath,
      BadgeForRepo badge)
      throws IOException, GitAPIException {
    BadgeJsonStore store = readStore(jsonFilePath);
    store =
        Optional.ofNullable(store)
            .orElse(BadgeJsonStore.builder().badgeForRepos(Collections.emptySet()).build());

    store.getBadgeForRepos().add(badge);

    updateJsonFile(git, credentialsProvider, jsonFilePath, store);
  }
}
