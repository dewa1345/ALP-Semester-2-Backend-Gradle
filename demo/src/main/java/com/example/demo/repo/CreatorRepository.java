package com.example.demo.repo;

import com.example.demo.model.Creator;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreatorRepository extends JpaRepository<Creator, Long> {
}