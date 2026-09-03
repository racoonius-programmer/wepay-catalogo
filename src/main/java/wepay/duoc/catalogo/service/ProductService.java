package wepay.duoc.catalogo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import wepay.duoc.catalogo.model.Product;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ProductService {
    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final Map<Long, Product> store = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(1);

    public ProductService() {
        // Intentar cargar catálogo de ejemplo desde resources/catalogo-gamer.json
        try (InputStream is = getClass().getResourceAsStream("/catalogo-gamer.json")) {
            if (is != null) {
                ObjectMapper mapper = new ObjectMapper();
                Product[] products = mapper.readValue(is, Product[].class);
                for (Product p : products) create(p);
                log.info("Cargado catálogo de ejemplo con {} productos desde resources/catalogo-gamer.json", products.length);
                return;
            } else {
                log.warn("No se encontró resources/catalogo-gamer.json — no se crearán productos por defecto.");
            }
        } catch (IOException e) {
            log.error("Error leyendo catalogo-gamer.json: {}", e.getMessage());
        }

        // No crear productos genéricos por defecto para evitar confusión.
    }

    public List<Product> findAll() {
        return new ArrayList<>(store.values());
    }

    public Optional<Product> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public Product create(Product p) {
        long id = seq.getAndIncrement();
        p.setId(id);
        store.put(id, p);
        return p;
    }

    public Optional<Product> update(Long id, Product p) {
        if (!store.containsKey(id)) return Optional.empty();
        p.setId(id);
        store.put(id, p);
        return Optional.of(p);
    }

    public boolean delete(Long id) {
        return store.remove(id) != null;
    }
}
