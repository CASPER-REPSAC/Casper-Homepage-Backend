# 빠르게 시작하기
길게 적기 귀찮아요.
```shell
$ git clone https://github.com/CASPER-REPSAC/Casper-Homepage-Backend.git
$ cd Casper-Homepage-Backend
# .env 파일과 .database.env 파일을 적절히 수정해주세요.
$ docker-compose up -d
```
## 메모
- `SECRET_KEY`는 반드시 설정해야 합니다. `tr -dc A-Za-z0-9 </dev/urandom | head -c 128; echo` 명령어로 생성할 수 있습니다.