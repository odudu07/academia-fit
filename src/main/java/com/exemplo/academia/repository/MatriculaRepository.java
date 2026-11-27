package com.exemplo.academia.repository;

import com.exemplo.academia.model.Matricula;
import com.exemplo.academia.model.StatusMatricula;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface MatriculaRepository extends JpaRepository<Matricula, Long> {

    /** Verifica se o aluno tem matrícula com o status informado e vigente até a data informada. */
    boolean existsByAlunoIdAndStatusAndDataFimGreaterThanEqual(Long alunoId, StatusMatricula status, LocalDate data);

    boolean existsByPlanoId(Long planoId);

    List<Matricula> findByAlunoId(Long alunoId);
}
