package com.nyam.everyday.web.team.mapper;

import com.nyam.everyday.common.util.FileNameGenerator;
import com.nyam.everyday.module.team.enums.ActivityType;
import com.nyam.everyday.web.team.dto.TeamActivityFeedItem;
import org.mapstruct.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

/**
 *
 * Redis 전용 팀피드 Mapper
 *
 * @author : 이지은
 * @fileName : TeamActivityFeedItemMapper
 * @since : 25. 8. 13.
 *
 */
@Mapper(componentModel = "spring", imports = {LocalDateTime.class, ActivityType.class})
public interface TeamActivityFeedItemMapper {

    /**
     * record(...)에서 바로 사용하기 쉽게 파라미터형 팩토리 메서드
     * - feedId: UUID
     * - feedCreatedDate: now()
     */
    @Mapping(target = "feedId", expression = "java(fileNameGenerator.generateUuid())")
    @Mapping(target = "feedCreatedDate", expression = "java(LocalDateTime.now())")
    TeamActivityFeedItem create(
            Long teamId,
            Long memberId,
            ActivityType activityType,
            String activityContent,
            @Context FileNameGenerator fileNameGenerator
    );
}
