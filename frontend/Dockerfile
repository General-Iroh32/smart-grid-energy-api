FROM node:24-alpine AS build
ARG BUILD_CONFIGURATION=production
WORKDIR /workspace
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npm run build -- --configuration ${BUILD_CONFIGURATION}

FROM nginx:1.29-alpine
ARG NGINX_CONFIG=nginx.conf
COPY ${NGINX_CONFIG} /etc/nginx/conf.d/default.conf
COPY --from=build /workspace/dist/smart-grid-dashboard/browser /usr/share/nginx/html
EXPOSE 80
