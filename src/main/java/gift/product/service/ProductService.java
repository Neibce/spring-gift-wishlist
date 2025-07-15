package gift.product.service;

import gift.exception.EntityNotFoundException;
import gift.exception.MdApprovalRequiredException;
import gift.product.dto.ProductCreateRequestDto;
import gift.product.dto.ProductItemDto;
import gift.product.dto.ProductUpdateRequestDto;
import gift.product.entity.Product;
import gift.product.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private static final String PRODUCT_NOT_FOUND_MESSAGE = "해당 상품을 찾을 수 없습니다.";
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ProductItemDto createProduct(ProductCreateRequestDto requestDto) {
        checkRestrictedWords(requestDto.name());

        Long newProductId = productRepository.save(requestDto);
        return new ProductItemDto(new Product(newProductId, requestDto));
    }

    @Transactional
    public ProductItemDto updateProduct(Long id, ProductUpdateRequestDto requestDto) {
        checkRestrictedWords(requestDto.name());

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(PRODUCT_NOT_FOUND_MESSAGE));
        product.update(requestDto);
        productRepository.update(product);
        return getProductById(id);
    }

    public void checkRestrictedWords(String name) {
        if (name.contains("카카오")) {
            throw new MdApprovalRequiredException("'카카오'가 포함된 문구는 담당 MD와 협의한 경우에만 사용할 수 있습니다.");
        }
    }

    @Transactional
    public void deleteProduct(Long id) {
        getProductById(id);
        productRepository.delete(id);
    }

    public List<ProductItemDto> getProducts() {
        return productRepository.findAll().stream()
                .map(ProductItemDto::new)
                .toList();
    }

    public ProductItemDto getProductById(Long id) {
        return new ProductItemDto(
                productRepository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException(PRODUCT_NOT_FOUND_MESSAGE)));
    }

    public boolean existsById(Long id) {
        return productRepository.existsById(id);
    }
}
