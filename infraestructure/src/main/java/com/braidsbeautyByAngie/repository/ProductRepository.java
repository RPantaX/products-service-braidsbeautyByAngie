package com.braidsbeautyByAngie.repository;

import com.braidsbeautyByAngie.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity,Long> {
    boolean existsByProductName(String productName);

    @Query(value = "SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM ProductEntity p WHERE p.productId = :productId AND p.state = true")
    boolean existsByProductIdWithStateTrue(Long productId);

    @Query(value = "SELECT p FROM ProductEntity p WHERE p.productId = :productId AND p.state = true")
    Optional<ProductEntity> findProductByProductIdWithStateTrue(Long productId);

    @Query(value = "SELECT p FROM ProductEntity p WHERE p.state=true")
    Page<ProductEntity> findAllByStateTrueAndPageable(Pageable pageable);
}
