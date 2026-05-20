import http from 'k6/http';

/*
Designed for experiments where:
- audit-service is slow/down
- retries accumulate
- outbox queue grows
*/

export const options = {
  vus: 200,
  duration: '90s',
};

const payload = JSON.stringify({
  ciphertext: 'YWJjZGVmZw==',
  iv: 'MDEyMzQ1Njc4OWFi',
  expiresIn: '24h',
  views: 1,
  burnAfterRead: false,
  kind: 'TEXT'
});

export default function () {
  http.post(
    'http://localhost:8080/api/pastes',
    payload,
    {
      headers: {
        'Content-Type': 'application/json',
      },
    }
  );
}