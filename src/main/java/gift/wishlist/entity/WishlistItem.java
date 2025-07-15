package gift.wishlist.entity;

import java.time.LocalDateTime;
import java.util.UUID;

public class WishlistItem {

    private Long id;
    private UUID memberUuid;
    private Long productId;
    private int quantity;
    private LocalDateTime addedAt;

    public WishlistItem() {

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

    public int getQuantity() {
        return quantity;
    }
}
