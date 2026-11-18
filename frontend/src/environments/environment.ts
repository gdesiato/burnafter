export const environment = {
  production: false,
  apiBase: (globalThis as any).API_BASE_URL || 'http://localhost:8080' // dev default
};