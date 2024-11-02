# 개발하기
## 직접 돌리기
도커를 사용하지 않고 직접 실행하는 방법입니다.
JetBrains IDEA를 사용할 경우 `.env` 파일과 `.database.env` 파일을 사용하도록 설정해주세요. (`EnvFile` 플러그인을 사용하면 편리합니다.)

## 도커로 돌리기
도커를 사용하면 개발 환경을 쉽게 구축할 수 있습니다.
```shell
$ docker-compose up -d
```
`.env` 파일과 `.database.env` 파일을 적절히 수정해주세요.