# Stage 8 — Infrastructure: frontend container image
#
# Multi-stage build: compile the Angular app under Node 22, then serve the static, hashed
# production build with nginx. No Node toolchain, node_modules or source ships at runtime.

# ---- Builder -----------------------------------------------------------------------------
FROM node:22-alpine AS builder
WORKDIR /build

COPY package.json package-lock.json* ./
RUN npm ci

COPY . .
RUN npm run build

# ---- Runtime -------------------------------------------------------------------------------
FROM nginx:1.27-alpine AS runtime

# angular.json outputPath is dist/syncboard-web; the application build lives under /browser.
COPY --from=builder /build/dist/syncboard-web/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD wget -q -O /dev/null http://localhost:80/ || exit 1

CMD ["nginx", "-g", "daemon off;"]
