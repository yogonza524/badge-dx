package com.badge.dx.port.incoming;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IncomingRequest {
  private String project;
  private String repo;
  private String subject;
  private String status;
  private String color;
  private String icon;
  private String badgeName;
}
