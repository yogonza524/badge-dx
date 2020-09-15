package com.badge.dx.domain;

import java.io.Serializable;
import java.util.Set;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BadgeJsonStore implements Serializable {
  private Set<BadgeForRepo> badgeForRepos;
}
