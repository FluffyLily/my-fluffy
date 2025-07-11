# My Fluffy Admin

myFluffy 프로젝트의 관리자 시스템 모듈입니다. 게시판 관리, 게시글 작성 및 수정, 관리자의 권한 관리와 사용자 관리 등 운영을 위한 주요 기능을 제공합니다. 
이 모듈은 Spring Boot 기반의 관리자 시스템으로, Vue js로 작성된 프론트엔드를 빌드하여 정적 리소스로 포함하고, 관리자 전용 API와 함께 하나의 애플리케이션으로 통합 배포됩니다.

## 기술 스택

### 프론트엔드

* Framework: Vue 3
* Build Tool: Vite
* State Management: Pinia
* CSS Preprocessor: SCSS
* UI Framework: Bootstrap 5
* Editor: CKEditor 5 (커스텀 빌드)

### 백엔드

* Language/Framework: Java 17, Spring Boot 3.0.4
* Persistence Layer: MyBatis (Mapper 기반 SQL 처리)
* Database: MySQL (Asia/Seoul 타임존)
* Authentication: Spring Security + JWT
* Cache/Session: Redis (토큰 및 인증 관련 처리)
* 파일 업로드: Multipart 처리, /uploads/images/yyyy/MM/dd 경로 저장
* 아키텍처: 멀티 모듈 구조 (core + web/admin + web/front)

    * `core`: DTO, 서비스, 인터셉터, 핸들러, 매퍼 등 비즈니스 로직 전반을 담당
    * `web`: API 컨트롤러 및 설정 담당
    * `web/admin`: 관리자 전용 API 컨트롤러 및 관련 설정, 필터 포함
    * `web/front`: 사용자 요청을 처리하는 API 및 프론트 연동 기능 제공

## 폴더 구조

### 백엔드 (Spring Boot 기반 관리자 API)
```
src/main/
├── java/
│   └── com/renee328/admin/...        # 관리자 API 컨트롤러, 서비스, 설정, 필터 등
├── resources/
│   ├── static/                       # 빌드된 Vue 정적 리소스가 복사되어 배포되는 위치
│   └── application-admin-prod.yml    # Spring Boot 설정 파일
```

### 프론트엔드 (Vue 기반 관리자 UI)
```
vue-admin/
├── public/             # 정적 리소스
├── src/
│   ├── api/            # Axios 기반 API 모듈
│   ├── assets/         # 이미지, 스타일 등 정적 리소스
│   ├── components/     # 재사용 가능한 컴포넌트
│   ├── router/         # Vue Router 설정
│   ├── stores/         # Pinia 스토어
│   ├── util/           # 유틸 함수
│   ├── views/          # 주요 라우트 페이지 컴포넌트
│   ├── App.vue         # 루트 컴포넌트
│   └── main.js         # 애플리케이션 초기화 및 App.vue 마운트 진입점
├── index.html          # 앱 진입점
└── vite.config.js      # Vite 설정
```

## vue-admin 보러 가기

[vue-admin](./vue-admin) README 참고:

- [myFluffy-web/admin/vue-admin](./vue-admin/README.md)


## 개발 환경 실행

```bash
npm install
npm run dev
```

## 운영 환경 빌드

```bash
npm run build -- --mode production
```

빌드 결과물은 dist/ 폴더에 생성됩니다.

## 환경 변수 설정

.env.development 와 .env.production에 다음과 같이 설정합니다:

```
.env.development
VITE_API_BASE_URL=http://localhost:8081/api

.env.production
VITE_API_BASE_URL=https://cms.myfluffy.site
```

운영 환경(admin-prod)은 Spring Boot 운영 서버에서 HTTPS로 정적 파일과 API를 하나의 애플리케이션에서 서빙하는 구조입니다.

## 주요 기능

* 게시판 목록/생성/수정/삭제
* 게시글 CRUD - CKEditor5 기반
* 태그 및 카테고리 관리 및 필터 적용 검색
* 관리자 로그인 기능 및 권한 관리(JWT 기반)
* 회원 관리(일반 사용자 + Google 로그인 사용자 포함)

## 개발 시 유의사항

* 스타일은 SCSS 문법을 기준으로 통일합니다.
* API 요청은 api/axios.js에서 모듈화되어 있음
* 게시글 작성 시 CKEditor5 이미지 업로드 경로 /uploads/images/... 주의
* 서버에서 JWT 만료, 권한 오류 등에 대한 에러 처리를 일관되게 받아 처리하도록 구성됨 (401, 403, 500 등)
