package com.benattidev.lavixx.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.benattidev.lavixx.entity.Vehicle;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {

    Optional<Vehicle> findByIdAndTenantId(UUID id, UUID tenantId);

    List<Vehicle> findAllByTenantId(UUID tenantId);

    List<Vehicle> findAllByTenantIdAndCustomerId(UUID tenantId, UUID customerId);

    @Query(value = """
            select v from Vehicle v join fetch v.customer cu
            where v.tenant.id = :tenantId
            order by lower(cu.name), v.createdAt, v.id
            """,
            countQuery = "select count(v) from Vehicle v where v.tenant.id = :tenantId")
    Page<Vehicle> findPageByTenantId(@Param("tenantId") UUID tenantId, Pageable pageable);

    @Query(value = """
            select v from Vehicle v join fetch v.customer cu
            where v.tenant.id = :tenantId and cu.id = :customerId
            order by v.createdAt, v.id
            """,
            countQuery = "select count(v) from Vehicle v where v.tenant.id = :tenantId and v.customer.id = :customerId")
    Page<Vehicle> findPageByTenantIdAndCustomerId(
            @Param("tenantId") UUID tenantId,
            @Param("customerId") UUID customerId,
            Pageable pageable);

    /**
     * Busca por placa (plate: so letras/numeros, maiusculo; comparada sem hifen/espaco) ou por
     * apelido, fabricante, modelo, identificador e nome do cliente (pattern, ja em minusculas).
     * Padroes montados por PageParams.
     */
    @Query(value = """
            select v from Vehicle v join fetch v.customer cu
            where v.tenant.id = :tenantId
              and (replace(replace(upper(v.plate), '-', ''), ' ', '') like :plate
                   or lower(v.nickname) like :pattern escape '\\'
                   or lower(v.manufacturer) like :pattern escape '\\'
                   or lower(v.model) like :pattern escape '\\'
                   or lower(v.identifier) like :pattern escape '\\'
                   or lower(cu.name) like :pattern escape '\\')
            order by lower(cu.name), v.createdAt, v.id
            """,
            countQuery = """
            select count(v) from Vehicle v join v.customer cu
            where v.tenant.id = :tenantId
              and (replace(replace(upper(v.plate), '-', ''), ' ', '') like :plate
                   or lower(v.nickname) like :pattern escape '\\'
                   or lower(v.manufacturer) like :pattern escape '\\'
                   or lower(v.model) like :pattern escape '\\'
                   or lower(v.identifier) like :pattern escape '\\'
                   or lower(cu.name) like :pattern escape '\\')
            """)
    Page<Vehicle> search(
            @Param("tenantId") UUID tenantId,
            @Param("pattern") String pattern,
            @Param("plate") String plate,
            Pageable pageable);

    Optional<Vehicle> findByTenantIdAndPlate(UUID tenantId, String plate);
}
