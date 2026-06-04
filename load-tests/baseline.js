import http from 'k6/http';

// Steady healthy traffic

export const options = {
  vus: 10,
  duration: '30s',
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
    'http://localhost/api/pastes',
    payload,
    {
      headers: {
        'Content-Type': 'application/json',
      },
    }
  );
}