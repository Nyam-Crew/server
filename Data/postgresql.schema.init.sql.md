# ⚙️ DDL 수정 내역 ⚙️
수정사항은 최신순으로 정렬합니다.

> ### 0804 1606 수정
### 테이블 추가
- daily_mission
- behavior
- daily_mission_behavior
- member_behavior_log

### 컬럼 추가
- member_stamp_status
  - created_date

> ### 오탈자 수정
- food
  - wegiht -> weight
- nutrition_detail
  - wegiht -> weight

>### 컬럼 추가
- team_member_status
  - team_role
- member
  - email
- team
  - member.member_id (FK)

> ### 테이블 추가
- team_activity_feed
- team_ranking_history
- team_notice

> ### summary, 집계 테이블에서 total_ prefix를 사용하는 것은 업계에서도 매우 널리 쓰이는 관례로 인하여 아래 컬럼명 수정
- water_intake → total_water
- kcal_intake → total_kcal

> ### member_daily_summary 수정
쿼리속도 이슈로 인하여 아래 컬럼 추가
- total_carbohydrate  (탄수화물)
- total_protein (단백질)
- total_fat (지방)
- 
> ### 사용자 랭킹 저장을 위한 아래의 테이블 추가
- user_global_ranking 전체 사용자 월간 랭킹 — 월간 (YYYY-MM)
- user_group_ranking 특정 그룹 내 사용자 랭킹 — 주간 or 월간
- group_global_ranking 그룹 단위 총점 랭킹 — 주간 or 월간