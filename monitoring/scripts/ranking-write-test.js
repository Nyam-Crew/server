import http from 'k6/http';
import { check, sleep } from 'k6';
import { SharedArray } from 'k6/data';

// ===================================================================================
// 테스트 설정
// ===================================================================================

// 테스트에 사용할 JWT 토큰 목록을 불러옵니다.
// test-tokens.json 파일에 실제 테스트용 토큰을 배열 형태로 넣어주세요.
const userTokens = new SharedArray('userTokens', function () {
  try {
    return JSON.parse(open('./test-tokens.json'));
  } catch (e) {
    // 토큰 파일이 없으면 테스트를 중단합니다.
    throw new Error("Could not open test-tokens.json. Please create it based on test-tokens.json.");
  }
});

// 테스트 옵션 설정
export const options = {
  stages: [
    { duration: '30s', target: 50 },  // 30초 동안 가상 유저를 50명까지 늘립니다.
    { duration: '1m', target: 50 },   // 50명 상태로 1분간 부하를 유지합니다.
    { duration: '30s', target: 0 },    // 30초 동안 유저를 0으로 줄이며 종료합니다.
  ],
  thresholds: {
    // 테스트 성공/실패 기준
    'http_req_duration': ['p(95)<250'], // 95%의 요청이 250ms 안에 처리되어야 함
    'http_req_failed': ['rate<0.01'],   // 요청 실패율이 1% 미만이어야 함
    'checks': ['rate>0.99'],            // 모든 check의 성공률이 99% 이상이어야 함
  },
};

// ===================================================================================
// 테스트 실행 함수
// ===================================================================================

export default function () {
  // API 서버 주소. 'host.docker.internal'은 Docker 컨테이너가 호스트 머신을 가리키는 특별한 주소입니다.
  // 실제 테스트 환경에 맞게 주소와 포트를 수정해주세요.
  const API_URL = 'http://host.docker.internal:8080/api/test/scores';

  // 테스트에 사용할 사용자 토큰을 무작위로 하나 선택합니다.
  const authToken = userTokens[Math.floor(Math.random() * userTokens.length)];

  // API에 보낼 데이터 (Payload)
  const payload = JSON.stringify({
      score: Math.floor(Math.random() * 10) + 1
  });


  // HTTP 요청 헤더 설정
  const params = {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${authToken}`,
    },
  };

  // 점수 갱신 API 호출
  const res = http.post(API_URL, payload, params);

  // 응답 검증
  check(res, {
    'status is 200': (r) => r.status === 200,
    'response time is less than 500ms': (r) => r.timings.duration < 500,
  });

  // 각 가상 유저는 0.5초에서 1초 사이를 랜덤하게 대기한 후 다음 요청을 보냅니다.
  sleep(Math.random() * 0.5 + 0.5);
}
