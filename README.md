# 집체크 (ZipCheck) 안드로이드 시제품

PPT 톤앤매너를 반영한 안드로이드 WebView 기반 시연용 앱입니다.

- 임차인 · 공인중개사 · 임대인 역할 전환
- 공간별 체크리스트, 사진, 공동 확인 기록
- 실제 로그인/공유 서버/AI 분석은 미구현이며 데이터는 기기 내부에 저장됩니다.
- 실제 주소나 개인정보, 민감한 사진을 입력하지 마세요.

## APK 내려받기

GitHub Actions 빌드와 Release 등록이 성공한 이후 **Releases**에서 시연용 디버그 APK를 내려받을 수 있습니다. 이 저장소에 APK가 아직 없다면 설치 링크는 작동하지 않습니다.

## 소스

`app/src/main/assets/index.html`이 웹앱 화면 및 기능이고 `MainActivity.java`가 Android WebView 앱 컨테이너입니다.

## 개발 환경

- Android SDK 35 / Gradle 8.9 / JDK 17
- 디버그 APK 빌드: `gradle assembleDebug`
