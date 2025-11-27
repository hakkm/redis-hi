# 🛑 The "Stale Data" Trap in Spring Boot + Redis

When using caching in distributed applications, two common pitfalls can cause **stale or inconsistent data**. Both arise from **race conditions** between application threads and cache updates.

---

## Problem 1: Race Condition with @CachePut

### What Happens

Using `@CachePut` naïvely seems like a good idea—you update the cache every time the database is updated:

```java
// ❌ DANGEROUS
@CachePut(cacheNames = "product", key = "#id")
public Product update(Long id, Product p) {
    p.setId(id);
    return repository.save(p); // Updates DB and cache
}
```

**Scenario:**

Two threads update the same product simultaneously:

| Thread | Operation              |
| ------ | ---------------------- |
| A      | Rename to "Blue Shirt" |
| B      | Rename to "Red Shirt"  |

If Thread A is slower, it may **overwrite the cache** after Thread B finishes:

```
DB: "Red Shirt" (correct)
Redis: "Blue Shirt" (stale)
```

> Users now see **wrong cached data**, even though the database is correct.

### The Fix: Use Cache Eviction

Instead of updating the cache, **delete it** so the next read fetches fresh data:

```java
// ✅ SAFER
@CacheEvict(cacheNames = "product", key = "#id")
public void update(Long id, Product p) {
    p.setId(id);
    repository.save(p);
    // Cache key deleted; next read repopulates safely
}
```

* Thread order doesn’t matter
* Cache misses after deletion always fetch the latest DB state

---

## Problem 2: Stale List Caching (findAll)

### What Happens

Caching a list separately from individual items can create **missing or stale entries**:

```java
@Cacheable(value = "products", key = "'all'")
public List<Product> findAll() {
    return repository.findAll();
}

@CachePut(value = "product", key = "#result.id")
public Product create(Product p) {
    return repository.save(p);
}
```

**Bug Sequence:**

1. `findAll()` → caches 10 items under `products:all`
2. `create(new Product)` → caches only `product:11`
3. `findAll()` → returns old list of 10 items, **missing the new item**

### The Fix: Evict List on Changes

Whenever an item is created, updated, or deleted, **evict the list key**:

```java
@Caching(
    put = { @CachePut(value = "product", key = "#result.id") },
    evict = { @CacheEvict(value = "products", key = "'all'") } // force refresh
)
public Product create(Product p) {
    return repository.save(p);
}
```

* Individual cache (`product:id`) is updated
* List cache (`products:all`) is evicted → next `findAll()` fetches fresh data

---

✅ **Summary**

| Problem                         | Cause                                 | Fix                                      |
| ------------------------------- | ------------------------------------- | ---------------------------------------- |
| Race condition with `@CachePut` | Multiple threads overwrite cache      | Use `@CacheEvict` instead of `@CachePut` |
| Stale list caching (`findAll`)  | List cache not updated on item change | Evict list cache when items change       |
