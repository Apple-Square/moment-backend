# 🎞️ Moment

![image](https://github.com/user-attachments/assets/0d2f156e-7473-4ac0-a0db-eb541db2c553)

<br/>

일상의 **✨특별한 순간**을 공유하는 **🪄시각 매체 기반 SNS**

<br/>

## 🍎 팀원 소개
|<img src="https://github.com/user-attachments/assets/9856718a-1113-42f4-a9fd-4efebbcee36c" width="180">|<img src="https://github.com/user-attachments/assets/7033ddfe-9613-41ae-b7f1-55f8138d02cd" width="180">|<img src="https://github.com/user-attachments/assets/180c06ef-e55a-48c2-ab2a-fb01e2ee17ea" width="180">|
|---|---|---|
|[**조현아 (BE)**](https://github.com/tenius10)|[**최건우 (FE)**](https://github.com/rjsdn031)|[**최현준 (FE)**](https://github.com/Hyeonjun0527)|
|▪️ **백엔드 개발**<br/>(인증, 소셜 로그인, 피드, <br/>쇼츠, 댓글, 태그, 유저 <br/>정보, 팔로우, 위치 검색,<br/>실시간 알림, 채팅, CI/CD)|▪️ **피드**<br/>▪️ **모먼트(쇼츠)**<br/>▪️ **댓글**<br/>▪️ **UI/UX**|▪️ **인증**<br/>▪️ **회원 관리**<br/>▪️ **채팅 관리**<br/>▪️ **UI/UX**|

<br/>

## 🛠️ 백엔드 아키텍처
![image](https://github.com/user-attachments/assets/a18cf69f-c32d-4aae-973c-751adc4384c6)


## 🛠️ 기술 스택

**Language**

![Java](https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=java&logoColor=white)

**Framework & Library**

![SpringBoot](https://img.shields.io/badge/SpringBoot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
<br/>
![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=json-web-tokens&logoColor=white)
![Querydsl](https://img.shields.io/badge/Querydsl-00599C?style=for-the-badge&logo=apachemaven&logoColor=white)
![SSE](https://img.shields.io/badge/SSE-FF6D00?style=for-the-badge&logo=googlenearby&logoColor=white)
![WebSocket](https://img.shields.io/badge/WebSocket-0088CC?style=for-the-badge&logo=socket.io&logoColor=white)

**Database**

![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)

**Deploy**

![AWS](https://img.shields.io/badge/AWS-232F3E?style=for-the-badge&logo=amazonaws&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/GitHub%20Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)

**Collaboration**

![Git](https://img.shields.io/badge/Git-F05032?style=for-the-badge&logo=git&logoColor=white)
![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white)
![Notion](https://img.shields.io/badge/Notion-000000?style=for-the-badge&logo=notion&logoColor=white)
![Figma](https://img.shields.io/badge/Figma-F24E1E?style=for-the-badge&logo=figma&logoColor=white)

<br/>

## 📒 API 명세서
https://nickel-confidence-5de.notion.site/API-008726751e004642976ff983cf98c5ee

<br/>

## 📖 주요 기능

### 🗝️ 인증 (JWT)
- 회원가입
- 로그인
- 토큰 재발급
- 로그아웃
- 계정 복구
- PW 재설정
- 토큰 블랙리스트
- 이메일 인증

### 🌐 소셜 로그인 (kakao, naver)
- 카카오 로그인
- 네이버 로그인

### 🎥 게시물 (쇼츠)
- 게시물 CRUD
- 이미지/비디오 파일 첨부
- 조회수
- 좋아요
- 댓글
- 해시태그
- 주소 첨부
- 키워드 기반 검색

### 🏷️ 해시태그
- 실시간 인기 태그 조회

### 👤 유저 관리
- 유저 프로필 관리
- 유저 페이지 조회
- 팔로우

### 🔔 실시간 알림 (SSE)
- 알림 이벤트 구독
- 알림 이벤트 발생 (댓글 알림, 좋아요 알림, 팔로우 알림, 피드 알림, 채팅 알림)
- 알림 조회 및 읽음 처리
- 알림 데이터 자동 삭제 (유지 기간 : 14일)

### 💬 실시간 채팅 (WebSocket, Stomp)
- 채팅방 CRUD (1:1, 그룹 채팅)
- 채팅방 멤버 조회
- 채팅방 구독
- 채팅방 멤버 활성화/비활성화
- 채팅방 초대
- 채팅방 나가기
- 채팅방 알림 설정
- 채팅 메세지 전송 (일반 텍스트, 이미지/비디오, 게시물 공유)


<br/>

## ⚙️ ERD
![Untitled](https://github.com/user-attachments/assets/db3afb8d-459f-4634-b2e2-ed7b4adacb44)

<br/>


