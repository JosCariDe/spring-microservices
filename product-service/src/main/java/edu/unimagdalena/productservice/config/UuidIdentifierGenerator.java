package edu.unimagdalena.productservice.config;

import edu.unimagdalena.productservice.model.Product;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UuidIdentifierGenerator extends AbstractMongoEventListener<Object> {

    @Override
    public void onBeforeConvert(BeforeConvertEvent<Object> event) {
        Object source = event.getSource();
        if (source instanceof Product product) {
            if (product.getId() == null) {
                product.setId(UUID.randomUUID().toString());
            }
        }
    }
}