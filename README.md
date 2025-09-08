# My Fluffy (마이 플러피)

`My Fluffy`는 반려생활 커뮤니티 기반의 블로그형 웹사이트입니다.  
관리자 시스템 모듈과 사용자 프론트 모듈이 분리되어 있으며, Spring Boot와 Vue 3 기반으로 구성되어 있습니다.

---

## 프로젝트 구조

```
myFluffy/
  ├── myFluffy-core/         # 공통 비즈니스 로직 및 설정 (Spring Boot)
  ├── myFluffy-web/          # 웹 모듈 (관리자/사용자 백엔드 및 프론트)
  │   ├── admin/             # 관리자 시스템 (Spring Boot + Vue 3)
  │   │   ├── src/           # 관리자 백엔드 (Spring Boot)
  │   │   └── vue-admin/     # 관리자 프론트엔드 (Vue 3 + Vite)
  │   ├── front/             # 사용자 화면 (Spring Boot + Vue 3)
  │   │   ├── src/           # 사용자 백엔드 (Spring Boot)
  │   └── └── vue-front/     # 사용자 프론트엔드 (Vue 3 + Vite)
  ├── pom.xml                # 최상위 Maven 설정
  └── README.md              # 현재 문서
```

---

## 기술 스택

### Backend
- Java 17
- Spring Boot 3.4.0
- MyBatis
- MySQL
- Redis
- Maven (멀티모듈 구조)

### Frontend
- Vue 3 + Vite
- Pinia
- SCSS + Bootstrap 5
- Axios, CKEditor 5

---

## 실행 방법

각 모듈별 README 참고:

- [myFluffy-web/admin](./myFluffy-web/admin/README.md)
- [myFluffy-web/front](./myFluffy-web/front/README.md)

---

## 주의 사항

- `application-*.yml` 설정 파일은 Git에 포함되지 않습니다.  
  각 모듈 내의 `application-*-example.yml`을 복사해서 실제 세부 설정을 수정해서 사용하세요.

- `.gitignore`에 따라 다음은 버전 관리에서 제외됩니다:
  - 빌드 산출물: `target/`, `build/`
  - IDE 설정: `.idea/`, `*.iml`
  - 환경 파일: `.env`, `application-*.yml`
  - 로그 및 캐시: `*.log`, `.DS_Store`, `node_modules/`

---

## TODO

- [✔] 사용자 프론트(front) 개발 시작
- [✔] OAuth 로그인 연동 (Google, Kakao)
- [ ] 관리자 시스템 회원 관리 기능 확장 및 고도화
