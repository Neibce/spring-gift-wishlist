# spring-gift-wishlist

## 0단계 - 기본 코드 준비

### 구현할 기능 목록

1. 상품 관리 레포지토리를 remote add, fetch 한다.
2. cherry-pick을 통해 상품 관리의 1,2,3단계 커밋들을 가져온다.
3. 해당 커밋들을 squash 한다.

## 1단계 - 유효성 검사

### 구현할 기능 목록

1. 기존 ProductCreateRequestDto, ProductUpdateRequestDto에 지정해둔 validation 어노테이션들을 과제 조건에 맞게 수정한다.
2. 상품명에 카카오가 포함된 경우는 Service 단에서 검증하여 exception을 던진다. (추후 회원 기능이 추가되면 MD 협의 여부를 체크하는 로직 작성하여 통과 여부 결정)

## 2단계 - 회원 로그인

### 구현할 기능 목록

1. JWT 및 Argon2 관련 의존성을 추가한다.
2. Member 엔티티를 생성한다.
3. JWT 토큰 프로바이더를 구현한다.
    - Access Token 발급 기능 구현
    - Refresh Token 발급 기능 구현
4. 회원가입 기능을 구현한다.
5. 로그인 기능을 구현한다.
6. Refresh Token을 이용한 Access Token 재발급 기능을 구현한다.

## 3단계 - 상품 위시리스트

### 구현할 기능 목록

1. wishlist_item 테이블을 생성한다. (schema.sql)
2. LoginMemberArgumentResolver를 구현한다.
3. WishlistItem 엔티티를 생성한다.
4. WishlistRepository를 구현한다.
5. WishlistService를 구현한다.
   - 위시리스트 아이템 추가/수정 기능
   - 위시리스트 조회 기능
   - 위시리스트 아이템 삭제 기능
6. WishlistController를 구현한다.
   - GET /api/wishlist - 로그인한 회원의 위시리스트 조회
   - PUT /api/wishlist/{productId} - 위시리스트 아이템 추가/수정
   - DELETE /api/wishlist/{productId} - 위시리스트 아이템 삭제
