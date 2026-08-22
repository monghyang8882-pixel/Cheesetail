# CheeseTail · 치즈테일

치즈테일 Android 앱 개발 저장소입니다.

## 현재 구현

- 앱 실행 시 치즈테일 메인 화면 표시
- 메인 화면의 `게임 시작` 영역 터치 → 멀티플레이 로비 진입
- 멀티플레이 로비는 등록된 4개 이미지 중 매 진입 시 무작위로 1개 표시
- 직전에 표시된 로비 이미지는 연속해서 다시 나오지 않음
- `방만들기` → 방 이름 / 2·3·4인 / 일반전·친선전 / 비공개 방 설정
- 방 생성 후 게임 대기실 진입
- 대기실에서 테스트 봇 채우기 / 준비 상태 / 게임 시작 버튼 처리
- `자동매칭` → 매칭 안내 → 4인 대기실 자동 진입
- Android 뒤로가기: 대기실 → 로비 → 홈 순으로 이동
- GitHub Actions에서 Debug APK 자동 빌드

## 현재 버전

- Android 앱 버전: `0.2.0`
- `versionCode 2`

## Android 기준

- `compileSdk 35`
- `targetSdk 35`
- `minSdk 26`
- Java 11 소스 호환
- GitHub Actions JDK 17 / Gradle 8.7

## APK 자동 빌드

`main` 브랜치에 코드가 push되거나 Actions에서 수동 실행하면
`.github/workflows/android.yml`이 `app-debug.apk`를 생성합니다.

빌드 결과는 GitHub의 **Actions → Build CheeseTail APK → Artifacts → cheesetail-debug-apk**에서 받을 수 있습니다.

## 화면 리소스

- `home_main` : 치즈테일 메인 화면
- `lobby_01` ~ `lobby_04` : 랜덤 멀티플레이 로비

## 다음 개발 순서

1. 실제 고해상도 홈/로비 원본 리소스 최종 반영
2. 게임 대기실의 실제 네트워크 동기화
3. 실제 2~4인 방 목록/초대/비밀번호 처리
4. 카드 게임 화면 연결 및 원카드 규칙 엔진 적용
5. Joker 카드 및 캐릭터 패시브 연결

향후 치즈테일의 Android 코드와 게임 리소스는 이 저장소를 기준으로 관리합니다.
