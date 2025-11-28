package me.khabir.helloredis.service;

import me.khabir.helloredis.entity.Product;
import me.khabir.helloredis.repos.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@CacheConfig(cacheNames = "product")
public class ProductService {

    @Autowired
    private ProductRepo repository;

    @Cacheable(value = "product", key = "'all'")
    public List<Product> findAll() {
        return repository.findAll();
    }

    @Cacheable(value = "product", key = "#id")
    public Product findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    @Caching(
            put = {@CachePut(key = "#result.id")},
            evict = {@CacheEvict(key = "'all'")}
    )
    public Product create(Product p) {
        return repository.save(p);
    }

    @Caching(
            evict = {
                    @CacheEvict(key = "#id"),
                    @CacheEvict(key = "'all'")
            }
    )
    public Product update(Long id, Product p) {
        // ensure the passed ID is used for the update
        p.setId(id);
        return repository.save(p);
    }

    @Caching(
            evict = {
                    @CacheEvict(key = "#id"),
                    @CacheEvict(key = "'all'")
            }
    )
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
