// Stage 4 — Angular 22 shell
// Production environment. Consumed via the @env/* path alias.

export interface Environment {
  production: boolean;
  apiBaseUrl: string;
  graphqlUrl: string;
  wsUrl: string;
}

export const environment: Environment = {
  production: true,
  apiBaseUrl: '/api/v1',
  graphqlUrl: '/graphql',
  wsUrl: '/ws',
};
