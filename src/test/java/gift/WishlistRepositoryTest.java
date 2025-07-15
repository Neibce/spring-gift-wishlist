package gift;

import gift.wishlist.dto.WishlistItemDto;
import gift.wishlist.entity.WishlistItem;
import gift.wishlist.repository.WishlistRepository;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.context.annotation.Import;

@SuppressWarnings("NonAsciiCharacters")
@JdbcTest
@Import(WishlistRepository.class)
public class WishlistRepositoryTest {

    private WishlistRepository wishlistRepository;

    @Autowired
    private JdbcClient jdbcClient;

    private UUID member1Uuid;
    private UUID member2Uuid;
    private Long product1Id;
    private Long product2Id;

    @BeforeEach
    void setUp() {
        wishlistRepository = new WishlistRepository(jdbcClient);

        member1Uuid = UUID.randomUUID();
        member2Uuid = UUID.randomUUID();

        product1Id = createTestProduct("테스트 상품 1", 10000L, "https://example.com/image1.jpg");
        product2Id = createTestProduct("테스트 상품 2", 20000L, "https://example.com/image2.jpg");

        createTestMember(member1Uuid, "test1@example.com", "테스트 회원 1");
        createTestMember(member2Uuid, "test2@example.com", "테스트 회원 2");
    }

    private Long createTestProduct(String name, Long price, String imageUrl) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcClient.sql("INSERT INTO product (name, price, image_url) VALUES (?, ?, ?)")
                .param(name)
                .param(price)
                .param(imageUrl)
                .update(keyHolder);
        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    private void createTestMember(UUID uuid, String email, String name) {
        jdbcClient.sql(
                        "INSERT INTO member (uuid, email, password, name, created_at, updated_at) VALUES (?, ?, ?, ?, NOW(), NOW())")
                .param(uuid.toString())
                .param(email)
                .param("mypassword12!@")
                .param(name)
                .update();
    }

    @Test
    void 위시리스트_상품_추가() {
        // given
        var wishlistItem = new WishlistItem(member1Uuid, product1Id, 2);

        // when
        Long savedId = wishlistRepository.upsert(wishlistItem);

        // then
        assertThat(savedId).isNotNull();
        assertThat(wishlistRepository.existsByMemberUuidAndProductId(member1Uuid,
                product1Id)).isTrue();
    }

    @Test
    void 위시리스트_상품_수량_변경() {
        // given
        var initialItem = new WishlistItem(member1Uuid, product1Id, 2);
        wishlistRepository.upsert(initialItem);

        // when
        var updateItem = new WishlistItem(member1Uuid, product1Id, 5);
        wishlistRepository.upsert(updateItem);

        // then
        var result = wishlistRepository.getByMemberUuidWithProduct(member1Uuid);
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().quantity()).isEqualTo(5);
    }

    @Test
    void 위시리스트_ID로_상품_조회() {
        // given
        var wishlistItem = new WishlistItem(member1Uuid, product1Id, 3);
        Long savedId = wishlistRepository.upsert(wishlistItem);

        // when
        Optional<WishlistItemDto> result = wishlistRepository.getById(savedId);

        // then
        assertThat(result).isPresent();
        var dto = result.get();
        assertThat(dto.quantity()).isEqualTo(3);
        assertThat(dto.product().name()).isEqualTo("테스트 상품 1");
        assertThat(dto.product().price()).isEqualTo(10000L);
        assertThat(dto.addedAt()).isNotNull();
    }

    @Test
    void 위시리스트_상품_목록_조회() {
        // given
        var item1 = new WishlistItem(member1Uuid, product1Id, 2);
        var item2 = new WishlistItem(member1Uuid, product2Id, 1);
        var item3 = new WishlistItem(member2Uuid, product1Id, 3);

        wishlistRepository.upsert(item1);
        wishlistRepository.upsert(item2);
        wishlistRepository.upsert(item3);

        // when
        List<WishlistItemDto> result = wishlistRepository.getByMemberUuidWithProduct(member1Uuid);

        // then
        assertThat(result).hasSize(2);

        assertThat(result)
                .extracting(item -> item.product().name())
                .containsExactlyInAnyOrder("테스트 상품 1", "테스트 상품 2");

        assertThat(result)
                .extracting(WishlistItemDto::quantity)
                .containsExactlyInAnyOrder(2, 1);
    }

    @Test
    void 위시리스트_상품_삭제() {
        // given
        var item1 = new WishlistItem(member1Uuid, product1Id, 2);
        var item2 = new WishlistItem(member1Uuid, product2Id, 1);
        var item3 = new WishlistItem(member2Uuid, product1Id, 3);

        wishlistRepository.upsert(item1);
        wishlistRepository.upsert(item2);
        wishlistRepository.upsert(item3);

        // when
        wishlistRepository.deleteByMemberUuidAndProductId(member1Uuid, product1Id);

        // then
        assertThat(wishlistRepository.existsByMemberUuidAndProductId(member1Uuid,
                product1Id)).isFalse();
        assertThat(wishlistRepository.existsByMemberUuidAndProductId(member1Uuid,
                product2Id)).isTrue();
        assertThat(wishlistRepository.existsByMemberUuidAndProductId(member2Uuid,
                product1Id)).isTrue();
    }
}
