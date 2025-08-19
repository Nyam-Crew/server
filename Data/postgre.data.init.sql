INSERT INTO nutrition_category (food_cate_id, category) VALUES
                                                            (1, '탄수화물'),
                                                            (2, '단백질'),
                                                            (3, '지방'),
                                                            (4, '무기질'),
                                                            (5, '기타')
    ON CONFLICT (food_cate_id) DO NOTHING;

insert into mission (category, title, type, is_active) values
                                                           ('FOOD',     '물 1L 마시기',          'AUTO',   true),
                                                           ('FOOD',     '음식 기록하기',   'AUTO', true),
                                                           ('ACTIVITY', '10분 산책하기',           'MANUAL', true),
                                                           ('ACTIVITY', '자전거 타기 20분',        'MANUAL', true),
                                                           ('ACTIVITY', '계단 오르내리기 10분',    'MANUAL', true),
                                                           ('ACTIVITY', '푸쉬업 20개',             'MANUAL', true),
                                                           ('ACTIVITY', '스쿼트 30개',             'MANUAL', true),
                                                           ('ACTIVITY', '스트레칭 5분',            'MANUAL', true),
                                                           ('ACTIVITY', '요가 15분',               'MANUAL', true),
                                                           ('MIND', '일기 쓰기',                   'MANUAL', true),
                                                           ('MIND', '오늘 기분 기록하기',          'MANUAL', true),
                                                           ('MIND', '좋은 글귀 읽기',              'MANUAL', true),
                                                           ('MIND', '책 10쪽 읽기',                'MANUAL', true),
                                                           ('MIND', '휴대폰 1시간 줄이기',         'MANUAL', true),
                                                           ('MIND', '자기 전 스트레칭',            'MANUAL', true),
                                                           ('MIND', '하루 마무리 감사 인사하기',   'MANUAL', true);
INSERT INTO nutrition_category (food_cate_id, category) VALUES
                                                            (1, '탄수화물'),
                                                            (2, '단백질'),
                                                            (3, '지방'),
                                                            (4, '무기질'),
                                                            (5, '기타')
    ON CONFLICT (food_cate_id) DO NOTHING;

insert into mission (category, title, type, is_active) values
                                                           ('FOOD',     '물 1L 마시기',          'AUTO',   true),
                                                           ('FOOD',     '음식 기록하기',   'AUTO', true),
                                                           ('ACTIVITY', '10분 산책하기',           'MANUAL', true),
                                                           ('ACTIVITY', '자전거 타기 20분',        'MANUAL', true),
                                                           ('ACTIVITY', '계단 오르내리기 10분',    'MANUAL', true),
                                                           ('ACTIVITY', '푸쉬업 20개',             'MANUAL', true),
                                                           ('ACTIVITY', '스쿼트 30개',             'MANUAL', true),
                                                           ('ACTIVITY', '스트레칭 5분',            'MANUAL', true),
                                                           ('ACTIVITY', '요가 15분',               'MANUAL', true),
                                                           ('MIND', '일기 쓰기',                   'MANUAL', true),
                                                           ('MIND', '오늘 기분 기록하기',          'MANUAL', true),
                                                           ('MIND', '좋은 글귀 읽기',              'MANUAL', true),
                                                           ('MIND', '책 10쪽 읽기',                'MANUAL', true),
                                                           ('MIND', '휴대폰 1시간 줄이기',         'MANUAL', true),
                                                           ('MIND', '자기 전 스트레칭',            'MANUAL', true),
                                                           ('MIND', '하루 마무리 감사 인사하기',   'MANUAL', true);

-- 이벤트 챌린지 (EVENT_CHALLENGE)
INSERT INTO everyday.badge (name, description, badge_image, badge_type, created_date) VALUES
                                                                                          ('새해 다짐 챌린지', '1월 첫 주, 매일 운동하고 식단 기록하기 - 뱃지: 작심삼일 극복', 'https://nyamnyambucket.s3.ap-northeast-2.amazonaws.com/a8000079-2dec-4c23-9ab9-4c31a0dea01c.png', 'EVENT_CHALLENGE', CURRENT_TIMESTAMP),
                                                                                          ('여름맞이 오운완ㅁ 챌린지', '한 달 동안 오늘 운동 완료 게시글 15회 이상 인증하기 - 뱃지: 선글라스 아보카도', 'https://nyamnyambucket.s3.ap-northeast-2.amazonaws.com/a8000079-2dec-4c23-9ab9-4c31a0dea01c.png', 'EVENT_CHALLENGE', CURRENT_TIMESTAMP),
                                                                                          ('추석 죄책감 덜기 챌린지', '추석 연휴 동안 송편 5개 칼로리(1000kcal)만큼 운동 소모 - 뱃지: 보름달 토끼', 'https://nyamnyambucket.s3.ap-northeast-2.amazonaws.com/a8000079-2dec-4c23-9ab9-4c31a0dea01c.png', 'EVENT_CHALLENGE', CURRENT_TIMESTAMP),
                                                                                          ('크리스마스 스텝 챌린지', '12월 1일부터 25일까지 누적 25만 보 걷기 - 뱃지: 산타 운동화', 'https://nyamnyambucket.s3.ap-northeast-2.amazonaws.com/a8000079-2dec-4c23-9ab9-4c31a0dea01c.png', 'EVENT_CHALLENGE', CURRENT_TIMESTAMP),
                                                                                          ('수분 히어로 챌린지', '일주일 동안 매일 2L 물 마시기 성공 - 뱃지: 망토 물방울', 'https://nyamnyambucket.s3.ap-northeast-2.amazonaws.com/a8000079-2dec-4c23-9ab9-4c31a0dea01c.png', 'EVENT_CHALLENGE', CURRENT_TIMESTAMP),
                                                                                          ('단백질 파워 위크', '5일 연속 매끼 단백질 20g 이상 섭취 - 뱃지: 근육질 달걀', 'https://nyamnyambucket.s3.ap-northeast-2.amazonaws.com/a8000079-2dec-4c23-9ab9-4c31a0dea01c.png', 'EVENT_CHALLENGE', CURRENT_TIMESTAMP),
                                                                                          ('클린이팅 챌린지', '주말 동안 가공식품, 배달음식 없이 직접 만든 건강식 기록 - 뱃지: 황금 사과', 'https://nyamnyambucket.s3.ap-northeast-2.amazonaws.com/a8000079-2dec-4c23-9ab9-4c31a0dea01c.png', 'EVENT_CHALLENGE', CURRENT_TIMESTAMP),
                                                                                          ('디지털 디톡스 챌린지', '3일 동안 식사 시간에는 스마트폰 없이 식사 집중 (인증샷 첨부) - 뱃지: 명상 브로콜리', 'https://nyamnyambucket.s3.ap-northeast-2.amazonaws.com/a8000079-2dec-4c23-9ab9-4c31a0dea01c.png', 'EVENT_CHALLENGE', CURRENT_TIMESTAMP);

-- 상시 챌린지 (REGULAR_CHALLENGE)
INSERT INTO  everyday.badge (name, description, badge_image, badge_type, created_date) VALUES
                                                                                           ('첫 식단 기록', '처음으로 식단을 기록', 'https://nyamnyambucket.s3.ap-northeast-2.amazonaws.com/8708f0af-47a3-4ac7-bd09-5da1d69529af.png', 'REGULAR_CHALLENGE', CURRENT_TIMESTAMP),
                                                                                           ('식단 기록ㄴ 10회 달성', '누적 식단 기록 10회 달성', 'https://nyamnyambucket.s3.ap-northeast-2.amazonaws.com/8708f0af-47a3-4ac7-bd09-5da1d69529af.png', 'REGULAR_CHALLENGE', CURRENT_TIMESTAMP),
                                                                                           ('물 1L 마시기 30일 달성', '누적 30일 동안 1L 물 마시기 성공', 'https://nyamnyambucket.s3.ap-northeast-2.amazonaws.com/57114865-9868-4ee8-8039-fda33db686eb.png', 'REGULAR_CHALLENGE', CURRENT_TIMESTAMP),
                                                                                           ('누적 걸음 10만 보 달성', '누적 걸음 수 10만 보 달성', 'https://nyamnyambucket.s3.ap-northeast-2.amazonaws.com/a8000079-2dec-4c23-9ab9-4c31a0dea01c.png', 'REGULAR_CHALLENGE', CURRENT_TIMESTAMP),
                                                                                           ('데일리 미션 100% 완료 10회', '데일리 미션 100% 완료 10회 달성', 'https://nyamnyambucket.s3.ap-northeast-2.amazonaws.com/a8000079-2dec-4c23-9ab9-4c31a0dea01c.png', 'REGULAR_CHALLENGE', CURRENT_TIMESTAMP),
                                                                                           ('주말 식단 모두 기록', '토/일 주말 동안 모든 식단 기록', 'https://nyamnyambucket.s3.ap-northeast-2.amazonaws.com/a8000079-2dec-4c23-9ab9-4c31a0dea01c.png', 'REGULAR_CHALLENGE', CURRENT_TIMESTAMP),
                                                                                           ('30일 연속 앱 접속', '30일 연속으로 앱 접속하기', 'https://nyamnyambucket.s3.ap-northeast-2.amazonaws.com/a8000079-2dec-4c23-9ab9-4c31a0dea01c.png', 'REGULAR_CHALLENGE', CURRENT_TIMESTAMP),
                                                                                           ('첫 좋아요 받기', '게시글에 첫 좋아요 받기', 'https://nyamnyambucket.s3.ap-northeast-2.amazonaws.com/a8000079-2dec-4c23-9ab9-4c31a0dea01c.png', 'REGULAR_CHALLENGE', CURRENT_TIMESTAMP),
                                                                                           ('응원 댓글 50개 작성', '다른 사람 게시글에 응원 댓글 50개 작성', 'https://nyamnyambucket.s3.ap-northeast-2.amazonaws.com/a8000079-2dec-4c23-9ab9-4c31a0dea01c.png', 'REGULAR_CHALLENGE', CURRENT_TIMESTAMP),
                                                                                           ('레시피 게시글 10개 북마크', '유용한 레시피 게시글 10개 북마크', 'https://nyamnyambucket.s3.ap-northeast-2.amazonaws.com/a8000079-2dec-4c23-9ab9-4c31a0dea01c.png', 'REGULAR_CHALLENGE', CURRENT_TIMESTAMP),
                                                                                           ('내 게시글 5회 북마크', '내 게시글이 다른 사람에게 5번 북마크 당하기', 'https://nyamnyambucket.s3.ap-northeast-2.amazonaws.com/a8000079-2dec-4c23-9ab9-4c31a0dea01c.png', 'REGULAR_CHALLENGE', CURRENT_TIMESTAMP),
                                                                                           ('그룹 목표 달성', '그룹 멤버들과 함께 목표 달성', 'https://nyamnyambucket.s3.ap-northeast-2.amazonaws.com/a8000079-2dec-4c23-9ab9-4c31a0dea01c.png', 'REGULAR_CHALLENGE', CURRENT_TIMESTAMP),
                                                                                           ('그룹 정원 모두 채우기', '그룹 정원 모두 채우기 (그룹장 전용)', 'https://nyamnyambucket.s3.ap-northeast-2.amazonaws.com/a8000079-2dec-4c23-9ab9-4c31a0dea01c.png', 'REGULAR_CHALLENGE', CURRENT_TIMESTAMP),
                                                                                           ('비포&애프터 게시글 작성', '비포&애프터 게시글 처음 작성하기', 'https://nyamnyambucket.s3.ap-northeast-2.amazonaws.com/a8000079-2dec-4c23-9ab9-4c31a0dea01c.png', 'REGULAR_CHALLENGE', CURRENT_TIMESTAMP),
                                                                                           ('목표 체중 설정', '나의 목표 체중 처음 설정하기', 'https://nyamnyambucket.s3.ap-northeast-2.amazonaws.com/a8000079-2dec-4c23-9ab9-4c31a0dea01c.png', 'REGULAR_CHALLENGE', CURRENT_TIMESTAMP),
                                                                                           ('커스텀 음식 등록', '나만의 커스텀 음식 처음 등록하기', 'https://nyamnyambucket.s3.ap-northeast-2.amazonaws.com/a8000079-2dec-4c23-9ab9-4c31a0dea01c.png', 'REGULAR_CHALLENGE', CURRENT_TIMESTAMP),
                                                                                           ('그룹 공지 작성', '그룹 공지사항 처음 작성하기 (그룹장 전용)', 'https://nyamnyambucket.s3.ap-northeast-2.amazonaws.com/a8000079-2dec-4c23-9ab9-4c31a0dea01c.png', 'REGULAR_CHALLENGE', CURRENT_TIMESTAMP),
                                                                                           ('프로필 정보 입력 완료', '프로필 정보(키, 몸무게, 활동량) 모두 입력하기', 'https://nyamnyambucket.s3.ap-northeast-2.amazonaws.com/a8000079-2dec-4c23-9ab9-4c31a0dea01c.png', 'REGULAR_CHALLENGE', CURRENT_TIMESTAMP),
                                                                                           ('단백질 권장량 3일 달성', '일일 단백질 권장량 3일 연속 달성', 'https://nyamnyambucket.s3.ap-northeast-2.amazonaws.com/a8000079-2dec-4c23-9ab9-4c31a0dea01c.png', 'REGULAR_CHALLENGE', CURRENT_TIMESTAMP),
                                                                                           ('균형 잡힌 하루 식단', '하루 세 끼 탄수화물, 단백질, 지방 균형 맞추기', 'https://nyamnyambucket.s3.ap-northeast-2.amazonaws.com/a8000079-2dec-4c23-9ab9-4c31a0dea01c.png', 'REGULAR_CHALLENGE', CURRENT_TIMESTAMP),
                                                                                           ('목표 체중 50% 달성', '목표 체중까지 50% 도달', 'https://nyamnyambucket.s3.ap-northeast-2.amazonaws.com/a8000079-2dec-4c23-9ab9-4c31a0dea01c.png', 'REGULAR_CHALLENGE', CURRENT_TIMESTAMP),
                                                                                           ('퍼펙트 데이', '하루 물, 식단, 운동 목표 모두 달성', 'https://nyamnyambucket.s3.ap-northeast-2.amazonaws.com/a8000079-2dec-4c23-9ab9-4c31a0dea01c.png', 'REGULAR_CHALLENGE', CURRENT_TIMESTAMP);

-- 이벤트 챌린지 (EVENT_CHALLENGE)
INSERT INTO everyday.challenge (badge_id, title, description, type, start_date, end_date) VALUES
                                                                                              (1, '새해 다짐 챌린지',         '1월 첫 주, 매일 운동하고 식단 기록하기 - 뱃지: 작심삼일 극복',                      'EVENT_CHALLENGE', NULL, NULL),
                                                                                              (2, '여름맞이 오운완 챌린지', '한 달 동안 오늘 운동 완료 게시글 15회 이상 인증하기 - 뱃지: 선글라스 아보카도',      'EVENT_CHALLENGE', NULL, NULL),
                                                                                              (3, '추석 죄책감 덜기 챌린지', '추석 연휴 동안 송편 5개 칼로리(1000kcal)만큼 운동 소모 - 뱃지: 보름달 토끼',         'EVENT_CHALLENGE', NULL, NULL),
                                                                                              (4, '크리스마스 스텝 챌린지',   '12월 1일부터 25일까지 누적 25만 보 걷기 - 뱃지: 산타 운동화',                      'EVENT_CHALLENGE', NULL, NULL),
                                                                                              (5, '수분 히어로 챌린지',       '일주일 동안 매일 2L 물 마시기 성공 - 뱃지: 망토 물방울',                            'EVENT_CHALLENGE', NULL, NULL),
                                                                                              (6, '단백질 파워 위크',         '5일 연속 매끼 단백질 20g 이상 섭취 - 뱃지: 근육질 달걀',                            'EVENT_CHALLENGE', NULL, NULL),
                                                                                              (7, '클린이팅 챌린지',          '주말 동안 가공식품, 배달음식 없이 직접 만든 건강식 기록 - 뱃지: 황금 사과',          'EVENT_CHALLENGE', NULL, NULL),
                                                                                              (8, '디지털 디톡스 챌린지',     '3일 동안 식사 시간에는 스마트폰 없이 식사 집중 (인증샷 첨부) - 뱃지: 명상 브로콜리', 'EVENT_CHALLENGE', NULL, NULL);

INSERT INTO everyday.challenge (badge_id, title, description, type, start_date, end_date) VALUES
                                                                                              (9 , '첫 식단 기록 챌린지',            '처음으로 식단을 기록하자',                               'REGULAR_CHALLENGE', NULL, NULL),
                                                                                              (10, '식단 기록 10회 달성 챌린지',   '식단을 총 10회 기록해보자',                          'REGULAR_CHALLENGE', NULL, NULL),
                                                                                              (11, '물 1L 마시기 30일 달성 챌린지',  '누적 30일 동안 1L씩 물을 마시자',                  'REGULAR_CHALLENGE', NULL, NULL),
                                                                                              (12, '누적 걸음 10만 보 달성 챌린지',  '누적 걸음 수 10만보를 채워보자',                         'REGULAR_CHALLENGE', NULL, NULL),
                                                                                              (13, '데일리 미션 100% 완료 10회 챌린지', '데일리 미션 100% 완료를 10회 달성하자',                'REGULAR_CHALLENGE', NULL, NULL),
                                                                                              (14, '주말 식단 모두 기록 챌린지',     '주말(토, 일)동안 동안 모든 식사를 기록해보자',                    'REGULAR_CHALLENGE', NULL, NULL),
                                                                                              (15, '30일 연속 앱 접속 챌린지',       '30일 연속으로 접속해보자',                         'REGULAR_CHALLENGE', NULL, NULL),
                                                                                              (16, '첫 좋아요 받기 챌린지',          '게시글에 첫 좋아요 받기',                           'REGULAR_CHALLENGE', NULL, NULL),
                                                                                              (17, '응원 댓글 50개 작성 챌린지',     '다른 사람 게시글에 응원 댓글 50개 작성',            'REGULAR_CHALLENGE', NULL, NULL),
                                                                                              (18, '레시피 게시글 10개 북마크 챌린지', '유용한 레시피 게시글 10개 북마크',                 'REGULAR_CHALLENGE', NULL, NULL),
                                                                                              (19, '내 게시글 5회 북마크 챌린지',    '내 게시글이 다른 사람에게 5번 북마크 당하기',       'REGULAR_CHALLENGE', NULL, NULL),
                                                                                              (20, '그룹 목표 달성 챌린지',          '그룹 멤버들과 함께 목표 달성',                      'REGULAR_CHALLENGE', NULL, NULL),
                                                                                              (21, '그룹 정원 모두 채우기 챌린지',   '그룹 정원 모두 채우기 (그룹장 전용)',               'REGULAR_CHALLENGE', NULL, NULL),
                                                                                              (22, '비포&애프터 게시글 작성 챌린지', '비포&애프터 게시글 처음 작성하기',                  'REGULAR_CHALLENGE', NULL, NULL),
                                                                                              (23, '목표 체중 설정 챌린지',          '나의 목표 체중 처음 설정하기',                      'REGULAR_CHALLENGE', NULL, NULL),
                                                                                              (24, '커스텀 음식 등록 챌린지',        '나만의 커스텀 음식 처음 등록하기',                  'REGULAR_CHALLENGE', NULL, NULL),
                                                                                              (25, '그룹 공지 작성 챌린지',          '그룹 공지사항 처음 작성하기 (그룹장 전용)',         'REGULAR_CHALLENGE', NULL, NULL),
                                                                                              (26, '프로필 정보 입력 완료 챌린지',   '프로필 정보(키, 몸무게, 활동량) 모두 입력하기',     'REGULAR_CHALLENGE', NULL, NULL),
                                                                                              (27, '단백질 권장량 3일 달성 챌린지',  '일일 단백질 권장량 3일 연속 달성',                  'REGULAR_CHALLENGE', NULL, NULL),
                                                                                              (28, '균형 잡힌 하루 식단 챌린지',     '하루 세 끼 탄수화물, 단백질, 지방 균형 맞추기',      'REGULAR_CHALLENGE', NULL, NULL),
                                                                                              (29, '목표 체중 50% 달성 챌린지',      '목표 체중까지 50% 도달',                            'REGULAR_CHALLENGE', NULL, NULL),
                                                                                              (30, '퍼펙트 데이 챌린지',             '하루 물, 식단, 운동 목표 모두 달성',                'REGULAR_CHALLENGE', NULL, NULL);