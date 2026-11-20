import http from 'k6/http';

// Steady, realistic load for the recorded demo.
// Constant arrival rate (not "as fast as possible" VUs), so the system is stable
// while healthy, the outbox backlog visibly grows during an audit outage,
// and visibly drains after recovery.
//
//   k6 run load-tests/demo.js
//   # ~30s in:  docker compose stop audit-service
//   # ~80s in:  docker compose start audit-service
//
// Override the rate with: k6 run -e RATE=50 load-tests/demo.js

export const options = {
  scenarios: {
    demo: {
      executor: 'constant-arrival-rate',
      rate: Number(__ENV.RATE || 30),
      timeUnit: '1s',
      duration: __ENV.DURATION || '3m',
      preAllocatedVUs: 10,
      maxVUs: 50,
    },
  },
};

const payload = JSON.stringify({
  ciphertext: 'YWJjZGVmZw==',
  iv: 'MDEyMzQ1Njc4OWFi',
  expiresIn: '24h',
  views: 1
});

export default function () {
  http.post('http://localhost/api/pastes', payload, {
    headers: { 'Content-Type': 'application/json' },
  });
}
