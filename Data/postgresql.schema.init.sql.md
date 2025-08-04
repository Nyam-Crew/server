# ⚙️ DDL 수정 내역 ⚙️

## 2025/08/04 
### 사용자 랭킹 저장을 위한 아래의 테이블 추가
  - user_global_ranking 전체 사용자 월간 랭킹 — 월간 (YYYY-MM)
  - user_group_ranking 특정 그룹 내 사용자 랭킹 — 주간 or 월간
  - group_global_ranking 그룹 단위 총점 랭킹 — 주간 or 월간

### member_daily_summary 수정
쿼리속도 이슈로 인하여 아래 컬럼 추가
- total_carbohydrate  (탄수화물)
- total_protein (단백질)
- total_fat (지방)

### summary, 집계 테이블에서 total_ prefix를 사용하는 것은 업계에서도 매우 널리 쓰이는 관례로 인하여 아래 컬럼명 수정
- water_intake → total_water
- kcal_intake → total_kcal

### 아래의 테이블 추가
- team_activity_feed
- team_ranking_history
- team_notice

### 테이블에 컬럼 추가
- team_member_status
  - team_role 
- member
  - email

### 오탈자 수정
- food
  - unit_weight -> unit_weight
- nutrition_detail
  - unit_wegiht -> unit_weight