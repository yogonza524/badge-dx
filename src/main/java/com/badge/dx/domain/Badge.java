package com.badge.dx.domain;

import java.io.Serializable;
import java.util.Objects;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Badge implements Serializable {
  private String badgeName;
  private String badgenUrl;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Badge badge = (Badge) o;
    return badgeName.equals(badge.badgeName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(badgeName);
  }
}
