package com.example.doan.Repository;

import com.example.doan.Model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository xử lý thao tác với bảng categories.
 * File: Repository/CategoryRepository.java
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
