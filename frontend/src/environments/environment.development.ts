// Stage 4 — Angular 22 shell
// Development environment (proxied by proxy.conf.json to localhost:8080).

import type { Environment } from './environment';

export const environment: Environment = {
  production: false,
  apiBaseUrl: '/api/v1',
  graphqlUrl: '/graphql',
  wsUrl: '/ws',
};
