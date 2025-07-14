package gift.product.controller;

import gift.product.dto.ProductCreateRequestDto;
import gift.product.dto.ProductItemDto;
import gift.product.dto.ProductUpdateRequestDto;
import gift.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductItemDto> createProduct(@Valid @RequestBody ProductCreateRequestDto product) {
        var createdProduct = productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @PatchMapping("{id}")
    public ResponseEntity<ProductItemDto> updateProduct(@PathVariable Long id,
                                              @Valid @RequestBody ProductUpdateRequestDto product) {
        var updatedProduct = productService.updateProduct(id, product);
        return ResponseEntity.status(HttpStatus.OK).body(updatedProduct);
    }


    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping
    public ResponseEntity<List<ProductItemDto>> getProducts() {
        var products = productService.getProducts();
        return ResponseEntity.status(HttpStatus.OK).body(products);
    }

    @GetMapping("{id}")
    public ResponseEntity<ProductItemDto> getProduct(@PathVariable Long id) {
        var product = productService.getProductById(id);
        return ResponseEntity.status(HttpStatus.OK).body(product);
    }
}
