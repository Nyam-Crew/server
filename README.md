# '매일같이' 식단 관리 플랫폼

## 1. 프로젝트 개요

'매일같이'는 사용자들이 실시간으로 식단을 공유하고, 팀을 구성해 식습관 배틀을 진행하거나, 자유롭게 정보를 공유하고 피드백을 받을 수 있는 식단 커뮤니티로도 사용할 수 있는 하이브리드 식단 관리 플랫폼입니다.

- **주요 기능**: 식단 기록, 외부 API 기반 영양소 정보 분석, 실시간 그룹 채팅 및 알림, 팀 챌린지 배틀, 식단 커뮤니티 게시판/댓글 시스템이 포함됩니다.
- **목표**: 건강한 식습관 형성을 돕고, 사용자 간의 상호작용을 통해 동기부여와 지속 가능성을 높입니다.

## 2. 기술 스택 (Tech Stack)

### Backend
- **Framework**: Spring Boot 3.5.4
- **Language**: Java 17
- **Database**: PostgreSQL (Primary), MongoDB (Chatting/Notifications)
- **Cache & Real-time Ranking**: Redis
- **Search Engine**: Elasticsearch
- **API Documentation**: SpringDoc (Swagger-UI)
- **Authentication**: Spring Security, JWT, OAuth2
- **Real-time Communication**: Spring WebSocket
- **Cloud Storage**: AWS S3

### Infrastructure & Monitoring
- **Containerization**: Docker, Docker Compose
- **Load Testing**: k6
- **Monitoring**: Grafana

### Frontend
- React.js, Tailwind

## 3. 프로젝트 구조

```
/
├── everyday/            # 메인 백엔드 Spring Boot 애플리케이션
│   ├── src/main/java    # 자바 소스 코드
│   └── src/main/resources # 설정 파일 (application.yml 등)
├── Data/                # 데이터베이스 서비스 (PostgreSQL, MongoDB)
│   └── docker-compose.data.yml
├── Infra/               # 인프라 서비스 (Elasticsearch)
│   └── docker-compose.infra.yml
└── monitoring/          # 모니터링 시스템 (Grafana, k6)
    └── docker-compose.yml
```

- **everyday/**: Spring Boot 기반의 핵심 백엔드 서버입니다. API, 비즈니스 로직, 데이터 처리를 담당합니다.
- **Data/**: `docker-compose`를 통해 PostgreSQL과 MongoDB 데이터베이스를 실행합니다.
- **Infra/**: `docker-compose`를 통해 Elasticsearch 검색 엔진을 실행합니다.
- **monitoring/**: `docker-compose`를 통해 Grafana 대시보드와 k6 부하 테스트 환경을 실행합니다.

## 4. 프로젝트 실행 방법

### 사전 요구사항
- **Java 17**: 백엔드 서버 실행에 필요합니다.
- **Docker & Docker Compose**: 데이터베이스 및 기타 인프라 실행에 필요합니다.
- **Node.js & npm/yarn**: 프론트엔드 개발 환경에 필요합니다.

### 1단계: 데이터베이스 및 인프라 실행
프로젝트에 필요한 PostgreSQL, MongoDB, Elasticsearch를 Docker Compose를 사용하여 실행합니다.

1.  터미널을 열고 프로젝트 루트 디렉토리로 이동합니다.
2.  아래 명령어를 순서대로 실행하여 각 서비스를 백그라운드에서 실행합니다.

```bash
# 데이터베이스 (PostgreSQL, MongoDB) 실행
docker-compose -f ./Data/docker-compose.data.yml up -d

# 데이터베이스 초기 데이터 입력
[postgresql.schema.init.sql](Data/postgresql.schema.init.sql)
[postgre.data.init.sql](Data/postgre.data.init.sql)

# 인프라 (Elasticsearch, Redis) 실행
docker-compose -f ./Infra/docker-compose.infra.yml up -d
```

### 2단계: 백엔드 서버 실행
Spring Boot 애플리케이션을 실행합니다.

1.  터미널에서 `everyday` 디렉토리로 이동합니다.
    ```bash
    cd everyday
    ```
2.  Gradle을 사용하여 애플리케이션을 실행합니다.
    ```bash
    ./gradlew bootRun
    ```
3.  서버가 성공적으로 실행되면 기본적으로 `localhost:8080`에서 API 요청을 수신 대기합니다.

## 5. API 문서
백엔드 서버가 실행 중일 때, 아래 URL에서 Swagger UI를 통해 API 명세를 확인하고 직접 테스트해볼 수 있습니다.

- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## 6. 초기 데이터 입력
* 기록 페이지에서 음식 등록/데일리 미션 실행 시 아래와 같은 사전 작업 필수.

1. postgre.data.init.sql 실행
2. OpenAPI의 모든 페이지를 순회하여 식품/영양소 데이터를 DB와 Elasticsearch에 업서트
3. api 실행 :  http://localhost:8080/api/admin/etl/foods/run?dryRun=false
 <br> Swagger : http://localhost:8080/swagger-ui/index.html#/Food-Etl-Controller/run

- 최초 한번만 실행. 

