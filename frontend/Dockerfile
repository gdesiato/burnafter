FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npx ng build --configuration docker

FROM nginx:alpine
COPY --from=build /app/dist/burnafter-ui/browser /usr/share/nginx/html
