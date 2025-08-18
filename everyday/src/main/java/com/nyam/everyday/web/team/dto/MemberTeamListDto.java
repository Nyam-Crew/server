package com.nyam.everyday.web.team.dto;

import com.nyam.everyday.module.team.entity.TeamMemberStatus;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberTeamListDto {
  List<Long> teamList;

  public static MemberTeamListDto of(List<TeamMemberStatus> teamMemberStatusList) {
    List<Long> temp = new ArrayList<>();
    for (TeamMemberStatus teamMemberStatus : teamMemberStatusList) {
      temp.add(teamMemberStatus.getTeam().getTeamId());
    }

    return MemberTeamListDto.builder()
        .teamList(temp)
        .build();
  }
}
