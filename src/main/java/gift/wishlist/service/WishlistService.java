package gift.wishlist.service;

import gift.exception.EntityNotFoundException;
import gift.member.entity.Member;
import gift.product.service.ProductService;
import gift.wishlist.dto.WishlistItemDto;
import gift.wishlist.dto.WishlistUpdateRequestDto;
import gift.wishlist.entity.WishlistItem;
import gift.wishlist.repository.WishlistRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductService productService;

    public WishlistService(WishlistRepository wishlistRepository, ProductService productService) {
        this.wishlistRepository = wishlistRepository;
        this.productService = productService;
    }

    @Transactional
    public WishlistItemDto updateItem(Member member, Long productId,
            WishlistUpdateRequestDto requestDto) {
        if (!productService.existsById(productId)) {
            throw new EntityNotFoundException("존재하지 않는 상품입니다.");
        }
        WishlistItem wishlistItem = new WishlistItem(member.getUuid(), productId, requestDto);
        Long itemId = wishlistRepository.upsert(wishlistItem);
        return new WishlistItemDto(getById(itemId));
    }

    public List<WishlistItemDto> getItems(Member member) {
        List<WishlistItem> wishlistItems = wishlistRepository.getByMemberUuidWithProduct(
                member.getUuid());
        return wishlistItems.stream()
                .map(WishlistItemDto::new)
                .toList();
    }

    @Transactional
    public void deleteItem(Member member, Long productId) {
        if (!wishlistRepository.existsByMemberUuidAndProductId(member.getUuid(), productId)) {
            throw new EntityNotFoundException("위시리스트 항목을 찾을 수 없습니다.");
        }

        wishlistRepository.deleteByMemberUuidAndProductId(member.getUuid(), productId);
    }

    public WishlistItem getById(Long id) throws EntityNotFoundException {
        return wishlistRepository.getById(id).orElseThrow(() ->
                new EntityNotFoundException("위시리스트 항목을 조회할 수 없습니다."));
    }
}
