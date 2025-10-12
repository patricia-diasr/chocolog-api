package com.chocolog.api.repository;

import com.chocolog.api.model.Flavor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FlavorRepository extends JpaRepository<Flavor, Long> {

    Optional<Flavor> findByName(String name);

}