// Used by the docker-compose build: the UI is served by Nginx on the same
// origin as the API, so requests go to relative /api/... paths.
export const environment = {
  production: true,
  apiBase: ''
};
