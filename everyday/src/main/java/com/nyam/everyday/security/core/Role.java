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

  public boolean isAdmin(){return this == ROLE_ADMIN;}// 권한 체크용
  public String toAuthority(){return name();}// enum 내장 메서드. 상수명 그대로 반환

}