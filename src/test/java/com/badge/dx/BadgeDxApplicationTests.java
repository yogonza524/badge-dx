package com.badge.dx;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badge.dx.business.BadgeService;
import com.badge.dx.business.GithubService;
import com.badge.dx.business.ImageDownloader;
import com.badge.dx.business.SvgToPngConverter;
import com.badge.dx.domain.BadgeJsonStore;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BadgeDxApplicationTests {

  private GithubService githubService;

  private BadgeService badgeService;
  private Git git;
  private UsernamePasswordCredentialsProvider credentials;
  String repoFolder;

  @BeforeEach
  void init() throws GitAPIException {
    githubService =
        new GithubService(
            "https://github.com/yogonza524/badge-dx-static.git",
            new UsernamePasswordCredentialsProvider(System.getenv("GIT_ACCESS_TOKEN"), ""));
    badgeService = new BadgeService(new ImageDownloader(), new SvgToPngConverter(), githubService);

    credentials =
        new UsernamePasswordCredentialsProvider("4c505c5251f9890c77cb3f8a5461889299d4c5cf", "");
    repoFolder = "/tmp/badge-dx-static-" + UUID.randomUUID().toString();
    git =
        Git.cloneRepository()
            .setCredentialsProvider(credentials)
            .setDirectory(new File(repoFolder))
            .setURI("https://github.com/yogonza524/badge-dx-static.git")
            .setBranch("master")
            .call();
  }

  @Test
  public void shouldPass() throws GitAPIException {
    UsernamePasswordCredentialsProvider credentials =
        new UsernamePasswordCredentialsProvider("4c505c5251f9890c77cb3f8a5461889299d4c5cf", "");
    Git git =
        Git.cloneRepository()
            .setCredentialsProvider(credentials)
            .setURI("https://github.com/yogonza524/badge-dx-static.git")
            .setBranch("master")
            .call();

    Ref newBranch = git.branchCreate().setName("yogonza524/roman-code").call();
    git.push().setCredentialsProvider(credentials).add(newBranch).call();
  }

  @Test
  public void shouldPassOver() throws GitAPIException, IOException {
    // assertFalse(githubService.repoExists("yogonza524", "peeta1"));
    // System.out.println("Access Token: " + System.getenv("GIT_ACCESS_TOKEN"));
    // assertTrue(githubService.createBranchIfNotExistsForRepo("yogonza524", "peeta"));

    UsernamePasswordCredentialsProvider credentials =
        new UsernamePasswordCredentialsProvider("4c505c5251f9890c77cb3f8a5461889299d4c5cf", "");
    String repoFolder = "/tmp/badge-dx-static-" + UUID.randomUUID().toString();
    System.out.println("Repo folder: " + repoFolder);
    assertTrue(githubService.createBranchIfNotExistsForRepo("yogonza524", "peeta"));

    Git git =
        Git.cloneRepository()
            .setCredentialsProvider(credentials)
            .setDirectory(new File(repoFolder))
            .setURI("https://github.com/yogonza524/badge-dx-static.git")
            .setBranch("yogonza524/peeta")
            .call();

    githubService.pushFile(git, "/home/gonzalo.mendoza/Downloads/hola.svg", null);
  }

  @Test
  public void shouldCreateBadgeJsonFileIfDoesntExistsRemote() throws GitAPIException, IOException {

    assertTrue(Files.exists(Paths.get(repoFolder + "/" + "README.md")));

    githubService.createJsonBadgeFileIfDoesntExists(credentials, "badges");
    BadgeJsonStore store = githubService.readStore(repoFolder + "/badges.json");

    assertNull(store);
  }
}
