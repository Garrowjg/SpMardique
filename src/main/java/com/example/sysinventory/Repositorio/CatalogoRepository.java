package com.example.sysinventory.Repositorio;

import com.example.sysinventory.Modelos.Catalogo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CatalogoRepository extends MongoRepository<Catalogo, String> {
    Optional<Catalogo> findByTipo(String tipo);
    boolean existsByTipo(String tipo);
}