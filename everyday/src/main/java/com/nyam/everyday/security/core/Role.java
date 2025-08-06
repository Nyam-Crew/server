package com.nyam.everyday.security.core;

import lombok.Getter;

@Getter
public enum Role {

  ROLE_USER("USER"),
  ROLE_ADMIN("ADMIN");
  private String role;


  Role(String role){
    this.role = role;
  }

}