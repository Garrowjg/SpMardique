package com.example.sysinventory.Repositorio;

import com.example.sysinventory.Modelos.Equipo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipoRepository extends MongoRepository<Equipo, String> {

    Optional<Equipo> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<Equipo> findByArea(String area);

    List<Equipo> findByEstado(String estado);

    List<Equipo> findByResponsableContainingIgnoreCase(String responsable);
}

