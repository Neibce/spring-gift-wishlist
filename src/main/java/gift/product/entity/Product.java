package gift.product.entity;

import gift.product.dto.ProductCreateRequestDto;
import gift.product.dto.ProductUpdateRequestDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;

public class Product {

    private Long id;

    @NotBlank
    @Length(max = 15)
    @Pattern(regexp = "[0-9a-zA-Zㄱ-ㅎ가-힣 ()\\[\\]+\\-&/_]+")
    private String name;

    @NotNull
    @PositiveOrZero
    private Long price;

    @URL
    private String imageUrl;

    public Product() {
    }

    public Product(Long id, String name, Long price, String imageUrl) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
    }

    public Product(Long id, ProductCreateRequestDto dto) {
        this.id = id;
        this.name = dto.name();
        this.price = dto.price();
        this.imageUrl = dto.imageUrl();
    }

    public void update(ProductUpdateRequestDto dto) {
        if (dto.name() != null) {
            this.name = dto.name();
        }
        if (dto.price() != null) {
            this.price = dto.price();
        }
        if (dto.imageUrl() != null) {
            this.imageUrl = dto.imageUrl();
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Long getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
