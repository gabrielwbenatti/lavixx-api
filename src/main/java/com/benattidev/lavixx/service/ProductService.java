package com.benattidev.lavixx.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.benattidev.lavixx.dto.product.ProductRequest;
import com.benattidev.lavixx.dto.product.ProductResponse;
import com.benattidev.lavixx.entity.Product;
import com.benattidev.lavixx.entity.Tenant;
import com.benattidev.lavixx.exception.NotFoundException;
import com.benattidev.lavixx.mapper.ProductMapper;
import com.benattidev.lavixx.repository.ProductRepository;
import com.benattidev.lavixx.security.SecurityUtils;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<ProductResponse> list() {
        return productRepository.findAllByTenantId(SecurityUtils.currentTenantId()).stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(UUID id) {
        return productMapper.toResponse(loadOwned(id));
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        UUID tenantId = SecurityUtils.currentTenantId();
        Product product = Product.builder()
                .tenant(entityManager.getReference(Tenant.class, tenantId))
                .name(request.name())
                .price(request.price())
                .build();
        return productMapper.toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(UUID id, ProductRequest request) {
        Product product = loadOwned(id);
        product.setName(request.name());
        product.setPrice(request.price());
        return productMapper.toResponse(product);
    }

    @Transactional
    public void delete(UUID id) {
        productRepository.delete(loadOwned(id));
    }

    private Product loadOwned(UUID id) {
        return productRepository.findByIdAndTenantId(id, SecurityUtils.currentTenantId())
                .orElseThrow(() -> new NotFoundException("Produto nao encontrado"));
    }
}
