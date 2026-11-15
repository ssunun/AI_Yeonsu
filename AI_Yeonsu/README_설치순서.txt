나의 AI 연수 아저씨 - 소스 설치 순서 (automotive 모듈)
=====================================================

이 ZIP 안에는 3가지가 있어요.
  automotive/src/main/java/com/example/aiyeonsu/   코드 파일 13개
  automotive/src/main/res/drawable-nodpi/          연곰 이미지 6장
  automotive/src/main/AndroidManifest.xml          실행 설정 파일

[0] 먼저 확인할 것 (1분)
  - 프로젝트의 automotive/build.gradle.kts 를 열어 namespace 가 "com.example.aiyeonsu" 인지 봅니다.
    다르면 이 파일을 그대로 두고 알려 주세요. (코드 맨 윗줄 package 이름을 같이 맞춰야 해요)

[1] 파일 복사 (Android Studio 왼쪽 프로젝트 창에서 끌어다 놓기)
  1) 연곰 이미지 6장 -> automotive/src/main/res/drawable-nodpi/
     (폴더가 없으면 res 폴더에서 New > Android Resource Directory 로 drawable-nodpi 를 만들어요)
  2) 코드 파일 13개 -> automotive/src/main/java/com/example/aiyeonsu/
     (같은 이름 파일이 있으면 덮어쓰기)
  3) AndroidManifest.xml -> automotive/src/main/ 의 기존 파일을 지우고(또는 내용을 통째로 바꿔) 넣어요.

[2] 빌드 설정 확인: automotive/build.gradle.kts
  아래 3가지가 있는지 봐요. 있으면 건드리지 마세요. 없는 것만 추가해요.
  (1) plugins { } 안에:   alias(libs.plugins.kotlin.compose)
  (2) android { } 안에:   buildFeatures { compose = true }
  (3) dependencies { } 안에:
        implementation(platform(libs.androidx.compose.bom))
        implementation(libs.androidx.activity.compose)
        implementation(libs.androidx.compose.material3)
        implementation(libs.androidx.compose.ui)
        implementation(libs.androidx.compose.ui.graphics)
        implementation(libs.androidx.core.ktx)
  libs.어쩌고 가 빨갛게 나오면 build.gradle.kts 내용을 통째로 보내 주세요. 거기에 맞춰 고쳐 드려요.

[3] 실행
  1) 상단에서 실행 대상 = automotive, 기기 = Pleos Connect 에뮬레이터
  2) 에뮬레이터가 홈 화면까지 완전히 뜬 뒤에 Sync Now -> ▶ Run
  3) 설치만 되고 안 뜨면, 에뮬레이터 앱 목록에서 "나의 AI 연수 아저씨" 를 눌러요.

[4] 시연 영상 찍기
  - 에뮬레이터 오른쪽 도구 줄의 ... (Extended controls) > Record and Playback > Screen recording > Start Recording
  - 순서: 출발 전(P) 화면 설명 -> 시작 버튼 -> 주행(D) 화면(지도·3D 도로, 음성 안내, 주유소 안내) -> 사고 시연 -> AI에게 질문 -> 주행 완료 -> 성장·멘토
  - 속도 버튼 3x/6x/10x 로 영상 길이를 조절해요. 주행은 6x 에서 약 2분이에요.
  - 저장 형식은 WebM 이에요. 제출용은 MP4 로 변환해요.

[알아둘 것]
  - 속도, 앞차 거리, 점수, 합류·사고다발·주유소 지점은 전부 시연용 가상 데이터예요.
  - 지도는 코드로 직접 그린 시연용 지도예요. 경로선만 평택대 -> 안성팜랜드 코스를 따랐어요.
  - 김서림 제어, 보험사 연결, 음성 질문은 화면과 흐름만 시연해요. 실제 차량 연동은 하지 않았어요.
  - 한국어 음성(TTS)이 에뮬레이터에 없으면 소리 없이 자막만 나와요.
  - 이 코드는 아직 컴파일해 보지 못했어요. 빨간 줄이나 에러는 캡처해서 보내 주세요.
