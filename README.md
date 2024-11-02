casper.or.kr

캐스퍼 백엔드

단순 커밋은 빌드, 간단한 수정

## docker image
기본적으로 `ghcr.io/casper-repsac/casper-backend`에 업로드됩니다.
### Tag
- `latest`: 최신 릴리즈(`vX.Y.Z` 형식의 태그 푸시 시 자동으로 업데이트됨)
- `main`: 개발 중인 브랜치(`main` 브랜치에 푸시 시 자동으로 업데이트됨)

### Pull
`docker pull ghcr.io/casper-repsac/casper-backend:latest`로 이미지를 받을 수 있습니다.

다만, GitHub Container Registry에 접근하기 위해서는 `PAT(Personal Access Token)`이 필요합니다.
자세한 내용은 [여기](https://docs.github.com/ko/packages/working-with-a-github-packages-registry/working-with-the-container-registry#personal-access-token-classic%EC%9D%84%EB%A5%BC-%EC%82%AC%EC%9A%A9%ED%95%98%EC%97%AC-%EC%9D%B8%EC%A6%9D)를 참고하세요.
