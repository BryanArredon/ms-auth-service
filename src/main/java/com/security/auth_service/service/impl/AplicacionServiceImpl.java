package com.security.auth_service.service.impl;

import com.security.auth_service.dto.CrearAplicacionRequest;
import com.security.auth_service.entity.AplicacionEntity;
import com.security.auth_service.repository.AplicacionRepository;
import com.security.auth_service.service.AplicacionService;
import com.security.auth_service.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AplicacionServiceImpl implements AplicacionService {

    private final AplicacionRepository aplicacionRepository;
    private final AuditoriaService auditoriaService;

    @Override
    public List<AplicacionEntity> listarTodas() {
        return aplicacionRepository.findAll();
    }

    @Override
    public AplicacionEntity obtenerPorId(UUID id) {
        return aplicacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Aplicación no encontrada"));
    }

    @Override
    @Transactional
    public AplicacionEntity crear(CrearAplicacionRequest request) {
        if (aplicacionRepository.findByNombre(request.getNombre()).isPresent()) {
            throw new RuntimeException("Ya existe una aplicación con ese nombre");
        }
        
        AplicacionEntity aplicacion = AplicacionEntity.builder()
                .nombre(request.getNombre())
                .activa(true)
                .build();
                
        AplicacionEntity guardada = aplicacionRepository.save(aplicacion);
        
        auditoriaService.registrarAccion("CREATE", "aplicaciones", guardada.getId().toString(), null, guardada);
        
        return guardada;
    }

    @Override
    @Transactional
    public AplicacionEntity actualizar(UUID id, AplicacionEntity aplicacionActualizada) {
        AplicacionEntity existente = obtenerPorId(id);
        
        // Clonar para auditoría antes de modificar
        AplicacionEntity anterior = AplicacionEntity.builder()
                .id(existente.getId())
                .nombre(existente.getNombre())
                .activa(existente.getActiva())
                .build();

        existente.setNombre(aplicacionActualizada.getNombre());
        existente.setActiva(aplicacionActualizada.getActiva());
        
        AplicacionEntity guardada = aplicacionRepository.save(existente);
        
        auditoriaService.registrarAccion("UPDATE", "aplicaciones", guardada.getId().toString(), anterior, guardada);
        
        return guardada;
    }

    @Override
    @Transactional
    public void eliminar(UUID id) {
        AplicacionEntity existente = obtenerPorId(id);
        aplicacionRepository.deleteById(id);
        auditoriaService.registrarAccion("DELETE", "aplicaciones", id.toString(), existente, null);
    }
}
