package com.badge.dx.domain;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BadgeForRepo implements Serializable {
  private String repo;
  private Set<Badge> badges;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BadgeForRepo that = (BadgeForRepo) o;
    return repo.equals(that.repo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(repo);
  }
}
