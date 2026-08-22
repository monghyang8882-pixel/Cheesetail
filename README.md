# CheeseTail · 치즈테일

치즈테일 Android 앱 개발 저장소입니다.

## 현재 구현

- 앱 실행 시 치즈테일 메인 화면 표시
- 메인 화면의 `게임 시작` 영역 터치 → 멀티플레이 로비 진입
- 멀티플레이 로비는 등록된 4개 이미지 중 매 진입 시 무작위로 1개 표시
- 직전에 표시된 로비 이미지는 연속해서 다시 나오지 않음
- Android 뒤로가기 → 홈 화면 복귀
- GitHub Actions에서 Debug APK 자동 빌드

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

## 현재 화면 리소스

- `home_main` : 치즈테일 메인 화면
- `lobby_01` ~ `lobby_04` : 랜덤 멀티플레이 로비

향후 치즈테일의 Android 코드와 게임 리소스는 이 저장소를 기준으로 관리합니다.
