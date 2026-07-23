# syntax=docker/dockerfile:1

# ===== Build Stage =====
FROM node:22-alpine AS build
WORKDIR /app

# Build args (baked into the React bundle at build time)
ARG VITE_API_BASE_URL=/api/v1
ARG VITE_GOOGLE_CLIENT_ID=
ARG VITE_FACEBOOK_APP_ID=

ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
ENV VITE_GOOGLE_CLIENT_ID=$VITE_GOOGLE_CLIENT_ID
ENV VITE_FACEBOOK_APP_ID=$VITE_FACEBOOK_APP_ID

# Install dependencies
COPY package*.json ./
RUN npm ci

# Copy source and build
COPY . .
RUN npm run build

# ===== Runtime Stage =====
FROM nginx:1.27-alpine

# Copy built assets
COPY --from=build /app/dist /usr/share/nginx/html

# Copy nginx config
COPY nginx/default.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD wget -q --spider http://localhost/ || exit 1