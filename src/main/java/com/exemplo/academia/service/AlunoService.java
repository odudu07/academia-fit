package com.exemplo.academia.service;

import com.exemplo.academia.dto.AlunoRequest;
import com.exemplo.academia.dto.AlunoResponse;
import com.exemplo.academia.exception.ConflitoException;
import com.exemplo.academia.exception.RecursoNaoEncontradoException;
import com.exemplo.academia.model.Aluno;
import com.exemplo.academia.repository.AlunoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AlunoService {

    private final AlunoRepository repository;

    public AlunoService(AlunoRepository repository) {
        this.repository = repository;
    }

    public AlunoResponse criar(AlunoRequest request) {
        if (repository.existsByEmail(request.email())) {
            throw new ConflitoException("Já existe um aluno com o e-mail informado");
        }
        if (repository.existsByCpf(request.cpf())) {
            throw new ConflitoException("Já existe um aluno com o CPF informado");
        }
        Aluno salvo = repository.save(new Aluno(
                request.nome(), request.email(), request.cpf(), request.dataNascimento()));
        return AlunoResponse.from(salvo);
    }

    public AlunoResponse atualizar(Long id, AlunoRequest request) {
        Aluno aluno = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aluno não encontrado: " + id));

        if (repository.existsByEmailAndIdNot(request.email(), id)) {
            throw new ConflitoException("Já existe outro aluno com o e-mail informado");
        }
        if (repository.existsByCpfAndIdNot(request.cpf(), id)) {
            throw new ConflitoException("Já existe outro aluno com o CPF informado");
        }

        aluno.setNome(request.nome());
        aluno.setEmail(request.email());
        aluno.setCpf(request.cpf());
        aluno.setDataNascimento(request.dataNascimento());
        return AlunoResponse.from(repository.save(aluno));
    }

    @Transactional(readOnly = true)
    public AlunoResponse buscar(Long id) {
        return repository.findById(id)
                .map(AlunoResponse::from)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aluno não encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public List<AlunoResponse> listar() {
        return repository.findAll().stream().map(AlunoResponse::from).toList();
    }
}
