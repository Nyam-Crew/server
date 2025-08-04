package com.nyam.everyday.web.groub.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * 그룹 CRUD DTO
 *
 * @author : 이지은
 * @fileName : GroupDTO
 * @since : 25. 8. 4.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupDTO {
    private Long groupId;
    private String groupName;
    private String groupDescription;
    private int groupMaxMembers;
    private LocalDateTime groupCreatedAt;
    private LocalDateTime modifiedDate;
    private Long ownerId;
}