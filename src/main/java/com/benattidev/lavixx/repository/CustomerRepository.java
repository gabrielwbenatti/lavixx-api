package com.benattidev.lavixx.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.benattidev.lavixx.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Optional<Customer> findByIdAndTenantId(UUID id, UUID tenantId);

    List<Customer> findAllByTenantId(UUID tenantId);

    @Query("select c from Customer c where c.tenant.id = :tenantId order by lower(c.name), c.id")
    Page<Customer> findPageByTenantId(@Param("tenantId") UUID tenantId, Pageable pageable);

    /**
     * Busca por nome (pattern, ja em minusculas) ou por documento/telefone (digits).
     * Padroes montados por PageParams.
     */
    @Query(value = """
            select c from Customer c
            where c.tenant.id = :tenantId
              and (lower(c.name) like :pattern escape '\\'
                   or c.document like :digits
                   or c.phone like :digits)
            order by lower(c.name), c.id
            """,
            countQuery = """
            select count(c) from Customer c
            where c.tenant.id = :tenantId
              and (lower(c.name) like :pattern escape '\\'
                   or c.document like :digits
                   or c.phone like :digits)
            """)
    Page<Customer> search(
            @Param("tenantId") UUID tenantId,
            @Param("pattern") String pattern,
            @Param("digits") String digits,
            Pageable pageable);

    Optional<Customer> findByTenantIdAndDocument(UUID tenantId, String document);
}
