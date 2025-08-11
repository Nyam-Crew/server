package com.nyam.everyday.module.team.enums;

/**
 *
 * 그룹 내 권한 ENUM
 *
 * @author : 이지은
 * @fileName : TeamRole
 * @since : 25. 8. 7.
 *
 */
public enum TeamRole {
    LEADER,
    SUBLEADER,
    MEMBER;

    public boolean isManager() {
        return this == LEADER || this == SUBLEADER;
    }
    public boolean isLeader()     { return this == TeamRole.LEADER; }
    public boolean isSubLeader()  { return this == TeamRole.SUBLEADER; }
}
