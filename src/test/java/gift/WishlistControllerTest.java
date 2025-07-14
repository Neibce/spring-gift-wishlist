package gift;

import static org.assertj.core.api.Assertions.assertThat;

import gift.common.dto.ErrorResponseDto;
import gift.exception.ErrorStatus;
import gift.member.dto.MemberLoginRequestDto;
import gift.member.dto.MemberLoginResponseDto;
import gift.member.dto.MemberRegisterRequestDto;
import gift.wishlist.dto.WishlistItemDto;
import gift.wishlist.dto.WishlistUpdateRequestDto;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

@SuppressWarnings("NonAsciiCharacters")
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WishlistControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    TestRestTemplate restTemplate;

    private String baseUrl;
    private String memberBaseUrl;

    private static final String VALID_PW = "qwerty12345678";
    private static final String VALID_NAME = "양준영";
    private static final Long VALID_PRODUCT_ID = 1L;
    private static final int VALID_QUANTITY = 20;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/wishlist";
        memberBaseUrl = "http://localhost:" + port + "/api/members";
    }

    private String generateUniqueValidEmail() {
        return "test_" + UUID.randomUUID() + "@example.com";
    }

    private String getAccessToken() {
        var registerDto = new MemberRegisterRequestDto(generateUniqueValidEmail(), VALID_NAME,
                VALID_PW);
        restTemplate.postForEntity(memberBaseUrl, registerDto, Object.class);

        var loginDto = new MemberLoginRequestDto(registerDto.email(), registerDto.password());
        var loginResponse = restTemplate.postForEntity(memberBaseUrl + "/login", loginDto,
                MemberLoginResponseDto.class);

        return Objects.requireNonNull(loginResponse.getBody()).tokenInfo().accessToken();
    }

    private HttpHeaders createAuthHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        return headers;
    }

    @Test
    void 위시리스트_조회_성공() {
        String accessToken = getAccessToken();
        HttpHeaders headers = createAuthHeaders(accessToken);

        var response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<List<WishlistItemDto>>() {
                }
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();

        var addRequest = new WishlistUpdateRequestDto(VALID_QUANTITY);
        restTemplate.exchange(baseUrl + "/products/" + VALID_PRODUCT_ID, HttpMethod.PUT,
                new HttpEntity<>(addRequest, headers), WishlistItemDto.class);

        var responseWithItem = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<List<WishlistItemDto>>() {
                }
        );

        assertThat(responseWithItem.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseWithItem.getBody()).hasSize(1);
        assertThat(Objects.requireNonNull(responseWithItem.getBody()).getFirst().product().id())
                .isEqualTo(VALID_PRODUCT_ID);
    }

    @Test
    void 위시리스트_품목_추가_성공() {
        String accessToken = getAccessToken();
        HttpHeaders headers = createAuthHeaders(accessToken);

        var requestDto = new WishlistUpdateRequestDto(VALID_QUANTITY);
        HttpEntity<WishlistUpdateRequestDto> entity = new HttpEntity<>(requestDto, headers);

        var response = restTemplate.exchange(
                baseUrl + "/products/" + VALID_PRODUCT_ID,
                HttpMethod.PUT,
                entity,
                WishlistItemDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().product().id()).isEqualTo(VALID_PRODUCT_ID);
        assertThat(response.getBody().quantity()).isEqualTo(VALID_QUANTITY);
        assertThat(response.getBody().addedAt()).isNotNull();
    }

    @Test
    void 위시리스트_품목_수량_업데이트_성공() {
        String accessToken = getAccessToken();
        HttpHeaders headers = createAuthHeaders(accessToken);

        var initialRequest = new WishlistUpdateRequestDto(VALID_QUANTITY);
        HttpEntity<WishlistUpdateRequestDto> initialEntity = new HttpEntity<>(initialRequest,
                headers);
        restTemplate.exchange(baseUrl + "/products/" + VALID_PRODUCT_ID, HttpMethod.PUT, initialEntity,
                WishlistItemDto.class);

        int updatedQuantity = 5;
        var updateRequest = new WishlistUpdateRequestDto(updatedQuantity);
        HttpEntity<WishlistUpdateRequestDto> updateEntity = new HttpEntity<>(updateRequest,
                headers);

        var response = restTemplate.exchange(
                baseUrl + "/products/" + VALID_PRODUCT_ID,
                HttpMethod.PUT,
                updateEntity,
                WishlistItemDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(response.getBody()).product().id())
                .isEqualTo(VALID_PRODUCT_ID);
        assertThat(response.getBody().quantity()).isEqualTo(updatedQuantity);
    }

    @Test
    void 위시리스트_품목_삭제_성공() {
        String accessToken = getAccessToken();
        HttpHeaders headers = createAuthHeaders(accessToken);

        var addRequest = new WishlistUpdateRequestDto(VALID_QUANTITY);
        HttpEntity<WishlistUpdateRequestDto> addEntity = new HttpEntity<>(addRequest, headers);
        restTemplate.exchange(baseUrl + "/products/" + VALID_PRODUCT_ID, HttpMethod.PUT, addEntity,
                WishlistItemDto.class);

        HttpEntity<Void> deleteEntity = new HttpEntity<>(headers);
        var response = restTemplate.exchange(
                baseUrl + "/products/" + VALID_PRODUCT_ID,
                HttpMethod.DELETE,
                deleteEntity,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        HttpEntity<Void> getEntity = new HttpEntity<>(headers);
        var getResponse = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                getEntity,
                new ParameterizedTypeReference<List<WishlistItemDto>>() {
                }
        );

        assertThat(getResponse.getBody()).isEmpty();
    }

    @Test
    void 위시리스트_조회_실패_인증없음() {
        var response = restTemplate.getForEntity(baseUrl, ErrorResponseDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void 위시리스트_품목_추가_실패_존재하지_않는_아이템() {
        String accessToken = getAccessToken();
        HttpHeaders headers = createAuthHeaders(accessToken);

        long nonExistentProductId = 999L;
        var requestDto = new WishlistUpdateRequestDto(VALID_QUANTITY);
        HttpEntity<WishlistUpdateRequestDto> entity = new HttpEntity<>(requestDto, headers);

        var response = restTemplate.exchange(
                baseUrl + "/products/" + nonExistentProductId,
                HttpMethod.PUT,
                entity,
                ErrorResponseDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(Objects.requireNonNull(response.getBody()).statusCode()).isEqualTo(
                ErrorStatus.ENTITY_NOT_FOUND.getCode());
    }

    @Test
    void 위시리스트_품목_추가_실패_잘못된_수량() {
        String accessToken = getAccessToken();
        HttpHeaders headers = createAuthHeaders(accessToken);

        var requestDto = new WishlistUpdateRequestDto(-1);
        HttpEntity<WishlistUpdateRequestDto> entity = new HttpEntity<>(requestDto, headers);

        var response = restTemplate.exchange(
                baseUrl + "/products/" + VALID_PRODUCT_ID,
                HttpMethod.PUT,
                entity,
                ErrorResponseDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().statusCode()).isEqualTo(
                ErrorStatus.VALIDATION_ERROR.getCode());
    }

    @Test
    void 위시리스트_품목_삭제_실패_존재하지_않는_아이템() {
        String accessToken = getAccessToken();
        HttpHeaders headers = createAuthHeaders(accessToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        var response = restTemplate.exchange(
                baseUrl + "/products/" + VALID_PRODUCT_ID,
                HttpMethod.DELETE,
                entity,
                ErrorResponseDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(Objects.requireNonNull(response.getBody()).statusCode()).isEqualTo(
                ErrorStatus.ENTITY_NOT_FOUND.getCode());
    }
}
