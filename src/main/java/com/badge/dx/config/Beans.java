package com.badge.dx.config;

import com.badge.dx.business.BadgeStoreService;
import com.badge.dx.business.CamoEraserService;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Beans {
  @Value("${github.personalAccesKey}")
  private String accessToken;

  @Value("${github.storage}")
  private String storageUrlRepo;

  @Value("${github.storageFileName}")
  private String storageFileName;

  @Bean
  String storageUrlRepo() {
    return storageUrlRepo;
  }

  @Bean
  UsernamePasswordCredentialsProvider gitCredentials() {
    return new UsernamePasswordCredentialsProvider(accessToken, "");
  }

  @Bean
  BadgeStoreService badgeStoreService() throws GitAPIException {
    return new BadgeStoreService(gitCredentials(), storageUrlRepo, storageFileName, camoEraserService());
  }

  @Bean
  CamoEraserService camoEraserService() {
    return new CamoEraserService();
  }
}
