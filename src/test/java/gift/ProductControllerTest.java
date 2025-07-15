package gift;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import gift.product.dto.ProductCreateRequestDto;
import gift.product.dto.ProductItemDto;
import gift.product.dto.ProductUpdateRequestDto;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProductControllerTest {

    @LocalServerPort
    private int port;

    private final RestClient restClient = RestClient.builder().build();
    private String baseUrl;

    private static final String VALID_PRODUCT_NAME = "아이스 카페 아메리카노 T";
    private static final Long VALID_PRODUCT_PRICE = 4500L;
    private static final String VALID_IMAGE_URL = "https://st.kakaocdn.net/product/gift/product/20231010111814_9a667f9eccc943648797925498bdd8a3.jpg";

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/products";
    }

    private ResponseEntity<ProductItemDto> postProductCreateRequest(ProductCreateRequestDto requestDto) {
        return restClient.post()
                .uri(baseUrl)
                .body(requestDto)
                .retrieve()
                .toEntity(ProductItemDto.class);
    }

    @Test
    public void createProduct() {
        var requestDto = new ProductCreateRequestDto(VALID_PRODUCT_NAME, VALID_PRODUCT_PRICE,
                VALID_IMAGE_URL);
        var response = postProductCreateRequest(requestDto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();

        assertThat(response.getBody().id()).isGreaterThan(0);
        assertThat(response.getBody().name()).isEqualTo(requestDto.name());
        assertThat(response.getBody().price()).isEqualTo(requestDto.price());
        assertThat(response.getBody().imageUrl()).isEqualTo(requestDto.imageUrl());
    }

    @Test
    public void createProductWithInvalidName() {
        var requestDto = new ProductCreateRequestDto("   ", VALID_PRODUCT_PRICE, VALID_IMAGE_URL);
        assertThrows(HttpClientErrorException.BadRequest.class, () -> postProductCreateRequest(requestDto));
    }

    @Test
    public void createProductWithInvalidPrice() {
        var requestDto = new ProductCreateRequestDto(VALID_PRODUCT_NAME, -1L, VALID_IMAGE_URL);
        assertThrows(HttpClientErrorException.BadRequest.class, () -> postProductCreateRequest(requestDto));
    }

    @Test
    public void getProduct() {
        var requestDto = new ProductCreateRequestDto(VALID_PRODUCT_NAME, VALID_PRODUCT_PRICE,
                VALID_IMAGE_URL);
        var createResponse = postProductCreateRequest(requestDto);

        var getResponse = restClient.get().uri(baseUrl + "/" + createResponse.getBody().id())
                .retrieve()
                .toEntity(ProductItemDto.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().id()).isEqualTo(createResponse.getBody().id());
        assertThat(getResponse.getBody().name()).isEqualTo(requestDto.name());
        assertThat(getResponse.getBody().price()).isEqualTo(requestDto.price());
        assertThat(getResponse.getBody().imageUrl()).isEqualTo(requestDto.imageUrl());
    }

    @Test
    public void getProducts() {
        var product1Response = postProductCreateRequest(
                new ProductCreateRequestDto("아이스 카페 아메리카노 T1", 4500L, VALID_IMAGE_URL));
        var product2Response = postProductCreateRequest(
                new ProductCreateRequestDto("아이스 카페 아메리카노 T2", 5000L, VALID_IMAGE_URL));

        var response = restClient.get().uri(baseUrl)
                .retrieve()
                .toEntity(ProductItemDto[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        var products = List.of(response.getBody());
        assertThat(products).isNotEmpty();

        assertThat(products).anyMatch(p -> p.id().equals(product1Response.getBody().id()) &&
                p.name().equals(product1Response.getBody().name()) &&
                p.price().equals(product1Response.getBody().price()));
        assertThat(products).anyMatch(p -> p.id().equals(product2Response.getBody().id()) &&
                p.name().equals(product2Response.getBody().name()) &&
                p.price().equals(product2Response.getBody().price()));
    }

    @Test
    public void updateProduct() {
        var createRequestDto = new ProductCreateRequestDto(VALID_PRODUCT_NAME, VALID_PRODUCT_PRICE,
                VALID_IMAGE_URL);
        var createResponse = postProductCreateRequest(createRequestDto);

        var updateRequestDto = new ProductUpdateRequestDto("아이스 카페 아메리카노 V", 6000L,
                VALID_IMAGE_URL);

        var updateResponse = restClient.patch()
                .uri(baseUrl + "/" + createResponse.getBody().id())
                .body(updateRequestDto)
                .retrieve()
                .toEntity(ProductItemDto.class);

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResponse.getBody()).isNotNull();
        assertThat(updateResponse.getBody().name()).isEqualTo(updateRequestDto.name());
        assertThat(updateResponse.getBody().price()).isEqualTo(updateRequestDto.price());
    }

    @Test
    public void deleteProduct() {
        var requestDto = new ProductCreateRequestDto(VALID_PRODUCT_NAME, VALID_PRODUCT_PRICE,
                VALID_IMAGE_URL);
        var createResponse = postProductCreateRequest(requestDto);

        var response = restClient.delete()
                .uri(baseUrl + "/" + createResponse.getBody().id())
                .retrieve()
                .toBodilessEntity();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        assertThrows(
                HttpClientErrorException.NotFound.class,
                () -> restClient.get()
                        .uri(baseUrl + "/" + createResponse.getBody().id())
                        .retrieve()
                        .toEntity(ProductItemDto.class)
        );
    }
}
