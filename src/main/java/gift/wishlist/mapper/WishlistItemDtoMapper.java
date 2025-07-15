package gift.wishlist.mapper;

import gift.product.dto.ProductItemDto;
import gift.product.entity.Product;
import gift.wishlist.dto.WishlistItemDto;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

public class WishlistItemDtoMapper implements RowMapper<WishlistItemDto> {

    @Override
    public WishlistItemDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        Product product = new Product(
                rs.getLong("product_id"),
                rs.getString("name"),
                rs.getLong("price"),
                rs.getString("image_url"));

        return new WishlistItemDto(
                new ProductItemDto(product),
                rs.getInt("quantity"),
                rs.getTimestamp("added_at").toLocalDateTime());
    }
}
