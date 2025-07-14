package gift.wishlist.entity;

import gift.product.entity.Product;
import gift.wishlist.dto.WishlistUpdateRequestDto;
import java.time.LocalDateTime;
import java.util.UUID;

public class WishlistItem {

    private Long id;
    private UUID memberUuid;
    private Long productId;
    private int quantity;
    private LocalDateTime addedAt;
    private Product product;

    public WishlistItem() {

    }

    public WishlistItem(UUID memberUuid, Long productId, int quantity,
            LocalDateTime addedAt, Product product) {
        this.memberUuid = memberUuid;
        this.productId = productId;
        this.quantity = quantity;
        this.addedAt = addedAt;
        this.product = product;
    }

    public WishlistItem(UUID memberUuid, Long productId, int quantity) {
        this.memberUuid = memberUuid;
        this.productId = productId;
        this.quantity = quantity;
    }

    public UUID getMemberUuid() {
        return memberUuid;
    }

    public Long getProductId() {
        return productId;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }
}
