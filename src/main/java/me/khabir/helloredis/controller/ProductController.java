package me.khabir.helloredis.controller;

import me.khabir.helloredis.entity.Product;
import me.khabir.helloredis.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService service;

    // Get products (with pagination)
    @GetMapping
    public List<Product> getProducts() {
        return service.findAll();
    }

    // Get one product
    @GetMapping("/{id}")
    public Product getProduct(@PathVariable Long id) {
        return service.findById(id);
    }

    // Create a product
    @PostMapping
    public Product createProduct(@RequestBody Product dto) {
        return service.create(dto);
    }

    // Update a product
    @PutMapping("/{id}")
    public Product updateProduct(
            @PathVariable Long id,
            @RequestBody Product p
    ) {
        return service.update(id, p);
    }

    // Delete a product
    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {
        service.delete(id);
    }
}
