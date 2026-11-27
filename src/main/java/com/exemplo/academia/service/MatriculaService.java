package com.exemplo.academia.service;

import com.exemplo.academia.dto.MatriculaRequest;
import com.exemplo.academia.dto.MatriculaResponse;
import com.exemplo.academia.exception.ConflitoException;
import com.exemplo.academia.exception.RecursoNaoEncontradoException;
import com.exemplo.academia.exception.RegraNegocioException;
import com.exemplo.academia.model.Aluno;
import com.exemplo.academia.model.Matricula;
import com.exemplo.academia.model.Plano;
import com.exemplo.academia.model.StatusMatricula;
import com.exemplo.academia.repository.AlunoRepository;
import com.exemplo.academia.repository.MatriculaRepository;
import com.exemplo.academia.repository.PlanoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

/**
 * Regras de negócio:
 * 1. O aluno deve ter no mínimo 16 anos para se matricular.
 * 2. O aluno só pode ter uma matrícula ativa por vez.
 * 3. A data de término é calculada a partir da duração do plano.
 * 4. Apenas matrículas ativas podem ser canceladas.
 */
@Service
@Transactional
public class MatriculaService {

    static final int IDADE_MINIMA = 16;

    private final MatriculaRepository matriculaRepository;
    private final AlunoRepository alunoRepository;
    private final PlanoRepository planoRepository;

    public MatriculaService(MatriculaRepository matriculaRepository,
                            AlunoRepository alunoRepository,
                            PlanoRepository planoRepository) {
        this.matriculaRepository = matriculaRepository;
        this.alunoRepository = alunoRepository;
        this.planoRepository = planoRepository;
    }

    public MatriculaResponse matricular(MatriculaRequest request) {
        Aluno aluno = alunoRepository.findById(request.alunoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aluno não encontrado: " + request.alunoId()));
        Plano plano = planoRepository.findById(request.planoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Plano não encontrado: " + request.planoId()));

        int idade = Period.between(aluno.getDataNascimento(), LocalDate.now()).getYears();
        if (idade < IDADE_MINIMA) {
            throw new RegraNegocioException("O aluno deve ter no mínimo " + IDADE_MINIMA + " anos para se matricular");
        }

        if (matriculaRepository.existsByAlunoIdAndStatusAndDataFimGreaterThanEqual(
                aluno.getId(), StatusMatricula.ATIVA, LocalDate.now())) {
            throw new ConflitoException("O aluno já possui uma matrícula ativa");
        }

        Matricula matricula = new Matricula();
        matricula.setAluno(aluno);
        matricula.setPlano(plano);
        matricula.setDataInicio(request.dataInicio());
        matricula.setDataFim(request.dataInicio().plusMonths(plano.getDuracaoMeses()));
        matricula.setStatus(StatusMatricula.ATIVA);

        return MatriculaResponse.from(matriculaRepository.save(matricula));
    }

    public MatriculaResponse cancelar(Long id) {
        Matricula matricula = buscarEntidade(id);

        if (matricula.getStatus() != StatusMatricula.ATIVA) {
            throw new RegraNegocioException("Apenas matrículas ativas podem ser canceladas");
        }

        matricula.setStatus(StatusMatricula.CANCELADA);
        return MatriculaResponse.from(matriculaRepository.save(matricula));
    }

    @Transactional(readOnly = true)
    public MatriculaResponse buscar(Long id) {
        return MatriculaResponse.from(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public List<MatriculaResponse> listar(Long alunoId) {
        List<Matricula> matriculas = alunoId != null
                ? matriculaRepository.findByAlunoId(alunoId)
                : matriculaRepository.findAll();
        return matriculas.stream().map(MatriculaResponse::from).toList();
    }

    private Matricula buscarEntidade(Long id) {
        return matriculaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Matrícula não encontrada: " + id));
    }
}
