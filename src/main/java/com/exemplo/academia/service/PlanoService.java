package com.exemplo.academia.service;

import com.exemplo.academia.dto.PlanoRequest;
import com.exemplo.academia.dto.PlanoResponse;
import com.exemplo.academia.exception.ConflitoException;
import com.exemplo.academia.exception.RecursoNaoEncontradoException;
import com.exemplo.academia.model.Plano;
import com.exemplo.academia.repository.MatriculaRepository;
import com.exemplo.academia.repository.PlanoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PlanoService {

    private final PlanoRepository planoRepository;
    private final MatriculaRepository matriculaRepository;

    public PlanoService(PlanoRepository planoRepository, MatriculaRepository matriculaRepository) {
        this.planoRepository = planoRepository;
        this.matriculaRepository = matriculaRepository;
    }

    public PlanoResponse criar(PlanoRequest request) {
        if (planoRepository.existsByNome(request.nome())) {
            throw new ConflitoException("Já existe um plano com o nome informado");
        }
        Plano salvo = planoRepository.save(
                new Plano(request.nome(), request.valorMensal(), request.duracaoMeses()));
        return PlanoResponse.from(salvo);
    }

    @Transactional(readOnly = true)
    public PlanoResponse buscar(Long id) {
        return planoRepository.findById(id)
                .map(PlanoResponse::from)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Plano não encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public List<PlanoResponse> listar() {
        return planoRepository.findAll().stream().map(PlanoResponse::from).toList();
    }

    public void deletar(Long id) {
        if (!planoRepository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Plano não encontrado: " + id);
        }
        if (matriculaRepository.existsByPlanoId(id)) {
            throw new ConflitoException("Não é possível remover um plano que possui matrículas");
        }
        planoRepository.deleteById(id);
    }
}
