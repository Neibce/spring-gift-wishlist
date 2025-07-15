package gift.wishlist.controller;

import gift.annotation.LoginMember;
import gift.member.entity.Member;
import gift.wishlist.dto.WishlistItemDto;
import gift.wishlist.dto.WishlistUpdateRequestDto;
import gift.wishlist.service.WishlistService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping
    public ResponseEntity<List<WishlistItemDto>> getWishlistItems(
            @LoginMember Member member) {
        var wishlistItems = wishlistService.getWishlistItems(member);
        return ResponseEntity.ok(wishlistItems);
    }

    @PutMapping("/products/{productId}")
    public ResponseEntity<WishlistItemDto> upsertWishlistItem(
            @LoginMember Member member,
            @PathVariable Long productId,
            @Valid @RequestBody WishlistUpdateRequestDto requestDto) {
        var updatedItem = wishlistService.upsertWishlistItem(member, productId, requestDto);
        return ResponseEntity.ok(updatedItem);
    }

    @DeleteMapping("/products/{productId}")
    public ResponseEntity<Void> deleteWishlistItem(
            @LoginMember Member member,
            @PathVariable Long productId) {
        wishlistService.deleteWishlistItem(member, productId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
