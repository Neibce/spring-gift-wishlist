package gift.wishlist.repository;

import gift.wishlist.dto.WishlistItemDto;
import gift.wishlist.entity.WishlistItem;
import gift.wishlist.mapper.WishlistItemDtoMapper;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class WishlistRepository {

    private final JdbcClient jdbcClient;

    public WishlistRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Long upsert(WishlistItem wishlistItem) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcClient.sql("MERGE INTO wishlist_item (member_uuid, product_id, quantity) "
                        + "KEY (member_uuid, product_id) VALUES (?, ?, ?)")
                .param(wishlistItem.getMemberUuid())
                .param(wishlistItem.getProductId())
                .param(wishlistItem.getQuantity())
                .update(keyHolder);

        return (Long) Objects.requireNonNull(keyHolder.getKeys()).get("id");
    }

    public Optional<WishlistItemDto> getById(Long id) {
        return jdbcClient.sql("""
                        SELECT
                            w.member_uuid, w.product_id, w.quantity, w.added_at,
                            p.id as product_id, p.name, p.price, p.image_url
                        FROM wishlist_item w
                        JOIN product p ON w.product_id = p.id
                        WHERE w.id = ?
                        ORDER BY w.added_at DESC
                        """)
                .param(id)
                .query(new WishlistItemDtoMapper())
                .optional();
    }

    public List<WishlistItemDto> getByMemberUuidWithProduct(UUID memberUuid) {
        return jdbcClient.sql("""
                        SELECT
                            w.member_uuid, w.product_id, w.quantity, w.added_at,
                            p.id as product_id, p.name, p.price, p.image_url
                        FROM wishlist_item w
                        JOIN product p ON w.product_id = p.id
                        WHERE w.member_uuid = ?
                        ORDER BY w.added_at DESC
                        """)
                .param(memberUuid)
                .query(new WishlistItemDtoMapper())
                .list();
    }

    public boolean existsByMemberUuidAndProductId(UUID memberUuid, Long productId) {
        return jdbcClient.sql(
                        "SELECT EXISTS(SELECT 1 FROM wishlist_item WHERE member_uuid = ? AND product_id = ?)")
                .param(memberUuid.toString())
                .param(productId)
                .query(Boolean.class)
                .single();
    }

    public void deleteByMemberUuidAndProductId(UUID memberUuid, Long productId) {
        jdbcClient.sql("DELETE FROM wishlist_item WHERE member_uuid = ? AND product_id = ?")
                .param(memberUuid.toString())
                .param(productId)
                .update();
    }
}
