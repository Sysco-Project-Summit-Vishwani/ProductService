package com.product.product_service.config;
import com.product.product_service.dto.ProductDTO;
import com.product.product_service.model.Product;
import org.modelmapper.convention.MatchingStrategies;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration()
                .setSkipNullEnabled(true)
                .setMatchingStrategy(MatchingStrategies.STRICT);

        mapper.typeMap(ProductDTO.class, Product.class)
                .addMappings(m -> {
                    m.skip(Product::setId);
                    m.skip(Product::setImageUrl);
                });
        return mapper;
    }
}