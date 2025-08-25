package com.nyam.everyday.search.board.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Mapping;
import org.springframework.data.elasticsearch.annotations.Setting;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = "board")
@Setting(settingPath = "/elasticsearch/board/board-settings.json")
@Mapping(mappingPath = "/elasticsearch/board/board-mappings.json")
public class BoardDocument {

  @Id
  private Long boardId;

  @Field(type = FieldType.Text)
  private String title;

  @Field(type = FieldType.Text)
  private String content;

  @Field(type = FieldType.Keyword)
  private String nickname;

  @Field(type = FieldType.Keyword)
  private String boardType;

  @Field(type = FieldType.Date,format = DateFormat.date)
  private java.time.LocalDate createdDate;

  @Field(type = FieldType.Long)
  private Long viewCount;

  @Field(type = FieldType.Long)
  private Long likeCount;

  @Field(type = FieldType.Long)
  private Long commentCount;
}
