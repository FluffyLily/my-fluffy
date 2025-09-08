# My Fluffy Admin UI

이 디렉토리는 myFluffy 프로젝트의 관리자 시스템(`admin` 모듈) 내부에 위치한 Vue 기반 프론트엔드 애플리케이션입니다. 게시판 관리, 게시글 작성, 관리자 권한 설정, 사용자 관리 등의 운영 기능을 제공합니다.

빌드된 정적 리소스는 Spring Boot 애플리케이션(`admin`)의 `/resources/static/`에 포함되어, 관리자 API와 함께 하나의 애플리케이션으로 통합 배포됩니다.

## 기술 스택

* **Framework**: Vue 3
* **Build Tool**: Vite
* **State Management**: Pinia
* **CSS Preprocessor**: SCSS
* **UI Framework**: Bootstrap 5
* **Editor**: CKEditor 5 (커스텀 빌드)

## 폴더 구조

```
vue-admin/
├── public/             # 정적 리소스 (favicon 등)
├── src/
│   ├── api/            # Axios 기반 API 모듈
│   ├── assets/         # 이미지, 스타일 등 정적 리소스
│   ├── components/     # 재사용 가능한 컴포넌트
│   ├── router/         # Vue Router 설정
│   ├── stores/         # Pinia 스토어
│   ├── util/           # 유틸 함수
│   ├── views/          # 주요 라우트 페이지 컴포넌트
│   ├── App.vue         # 루트 컴포넌트
│   └── main.js         # Vue 앱의 진입점, App.vue를 마운트하고 라우터/스토어 초기화
├── index.html          # 앱 진입 HTML
├── vite.config.js      # Vite 설정
└── ...
```

## 개발 환경 실행

```bash
npm install
npm run dev
```

## 운영 환경 빌드

```bash
npm run build -- --mode production
```

빌드 결과물은 `dist/` 폴더에 생성되며, `admin` 모듈의 `/src/main/resources/static/` 경로로 복사하여 Spring Boot가 함께 서빙합니다.

## 환경 변수 설정

`.env.development`와 `.env.production` 파일을 다음과 같이 설정합니다:

```
# .env.development
VITE_API_BASE_URL=http://localhost:8081/api

# .env.production
VITE_API_BASE_URL=https://cms.myfluffy.site
```

## 주요 기능

* **관리자 로그인 및 인증 상태 유지**: JWT 기반 로그인, 토큰 만료 시 리다이렉트 처리
* **게시판 관리**: 게시판 목록 조회, 생성, 수정, 삭제 기능 제공. 각 게시판의 카테고리 관리 포함.
* **게시글 관리**: CKEditor5 기반의 리치 에디터 사용. 자동 태그 추천 기능과 이미지 업로드 포함. 게시글 목록, 검색, 수정, 삭제 기능.
* **태그 및 카테고리 관리**: 게시글 필터 검색을 위한 태그 및 카테고리 목록을 동적으로 추가/수정 가능
* **회원 목록 조회 및 상태 관리**: 일반 회원 및 소셜 로그인 회원 구분, 회원 활성/비활성 상태 제어
* **UI 내 권한 분기 처리**: 권한 등급(최고 관리자, 일반 관리자, 운영자)에 따른 기능 제한 및 접근 메뉴와 버튼 노출 제어
* **검색 및 정렬 기능**: 게시글/시스템 공지/회원 목록 등에서 필터 타입별 키워드 검색, 등록일 순 정렬 기능 제공
* **메인 대시보드**: 날짜별 게시글 수 통계를 차트로 시각화하여 한눈에 확인 가능

