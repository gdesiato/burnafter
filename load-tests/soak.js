import http from 'k6/http';

// Long-duration stability test

export const options = {
  vus: 20,
  duration: '15m',
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