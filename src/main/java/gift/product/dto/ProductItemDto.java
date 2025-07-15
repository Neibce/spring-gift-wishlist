package gift.product.dto;


import gift.product.entity.Product;

public record ProductItemDto(
        Long id,
        String name,
        Long price,
        String imageUrl
) {

    public ProductItemDto(Product product) {
        this(product.getId(), product.getName(), product.getPrice(), product.getImageUrl());
    }
}
