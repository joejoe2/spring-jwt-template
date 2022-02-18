# spring-jwt-template

[![codecov](https://codecov.io/gh/joejoe2/spring-jwt-template/branch/main/graph/badge.svg?token=24IMFJ0D50)](https://codecov.io/gh/joejoe2/spring-jwt-template)
 
## Description

This is a template to help you to get started with jwt-based spring boot backend.

Including:
- `basic models` for user, access token, and refresh token
- `login api` issue access token and refresh token
- `register api` with email verification
- `logout api` revoke access token via redis blacklist
- `change password api`
- `admin api` change role and getUserList
- validation on `@RequestBody`
- validation on service layer

## Get started

1. clone the repo
2. copy ./src/resources/application-dev.properties to application.properties
3. modify `db related settings`, `jwt related settings`, `default admin account` and `mail sender` in application.properties depends on your need
4. set up a postgresql server on localhost:5432
5. set up a redis server on localhost:6379
6. now you can start to develop your own project

## Testing

`mvn test` or `./mvnw test`

## Deploy

1. install docker and docker-compose
2. edit `./env/application.env` (just like application.properties)
3. edit `./nginx/nginx-certbot.env` (just set value at first line)
4. edit `./nginx/user_conf.d/server.conf` (just change server_name)
5. make sure that `POSTGRES_PASSWORD` and `POSTGRES_DB` in `./docker-compose.yml` is same with settings in `./env/application.env`
6. `docker-compose up` or `docker-compose up -d`

## ToDo

1. add test for controllers
2. add example frontend
