package gift.wishlist.dto;

import gift.product.dto.ProductItemDto;
import java.time.LocalDateTime;

public record WishlistItemDto(
        ProductItemDto product,
        int quantity,
        LocalDateTime addedAt
) {

}
