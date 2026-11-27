package com.exemplo.academia.service;

import com.exemplo.academia.dto.AulaRequest;
import com.exemplo.academia.dto.AulaResponse;
import com.exemplo.academia.exception.RecursoNaoEncontradoException;
import com.exemplo.academia.model.Aula;
import com.exemplo.academia.model.StatusReserva;
import com.exemplo.academia.repository.AulaRepository;
import com.exemplo.academia.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AulaService {

    private final AulaRepository aulaRepository;
    private final ReservaRepository reservaRepository;

    public AulaService(AulaRepository aulaRepository, ReservaRepository reservaRepository) {
        this.aulaRepository = aulaRepository;
        this.reservaRepository = reservaRepository;
    }

    public AulaResponse criar(AulaRequest request) {
        Aula salva = aulaRepository.save(new Aula(
                request.nome(), request.instrutor(), request.dataHora(), request.capacidade()));
        return AulaResponse.from(salva, 0);
    }

    @Transactional(readOnly = true)
    public AulaResponse buscar(Long id) {
        Aula aula = aulaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aula não encontrada: " + id));
        return comVagas(aula);
    }

    @Transactional(readOnly = true)
    public List<AulaResponse> listar() {
        return aulaRepository.findAll().stream().map(this::comVagas).toList();
    }

    private AulaResponse comVagas(Aula aula) {
        long ocupadas = reservaRepository.countByAulaIdAndStatus(aula.getId(), StatusReserva.CONFIRMADA);
        return AulaResponse.from(aula, ocupadas);
    }
}
