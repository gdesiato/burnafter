import http from 'k6/http';

// Higher sustained pressure

export const options = {
  vus: 100,
  duration: '60s',
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