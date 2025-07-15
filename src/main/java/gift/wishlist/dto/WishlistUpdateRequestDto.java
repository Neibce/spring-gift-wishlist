package gift.wishlist.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record WishlistUpdateRequestDto(
        @NotNull(message = "quantity는 필수 입력값입니다.")
        @Positive(message = "quantity는 양수여야 합니다.")
        int quantity
) {

}
