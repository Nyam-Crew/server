package com.nyam.everyday.web.mission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/*
 * 미션 완료 요청 DTO
 *
 * 설계 의도
 * - 특정 미션을 완료/미완료 상태로 변경할 때 사용
 * - complete: true=완료, false=미완료
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "미션 완료 요청 DTO")
public final class MissionCompleteRequestDto {

    @Schema(description = "완료 여부 (true=완료, false=미완료)", example = "true")
    private boolean complete;
}