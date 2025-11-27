package me.khabir.helloredis.service;

import me.khabir.helloredis.entity.Product;
import me.khabir.helloredis.repos.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@CacheConfig(cacheNames = "product")
public class ProductService {

    @Autowired
    private ProductRepo repository;

    @Cacheable(value = "product")
    public List<Product> findAll() {
        return repository.findAll();
    }

    @Cacheable(value = "product", key = "#id")
    public Product findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    @CachePut(cacheNames = "product", key = "#result.id")
    public Product create(Product p) {
        return repository.save(p);
    }

    @CachePut(cacheNames = "product", key = "#id")
    public Product update(Long id, Product p) {
        // ensure the passed ID is used for the update
        p.setId(id);
        return repository.save(p);
    }

    @CacheEvict(cacheNames = "product", key = "#id", beforeInvocation = true)
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
