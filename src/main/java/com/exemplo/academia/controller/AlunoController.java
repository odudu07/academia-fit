package com.exemplo.academia.controller;

import com.exemplo.academia.dto.AlunoRequest;
import com.exemplo.academia.dto.AlunoResponse;
import com.exemplo.academia.service.AlunoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/alunos")
public class AlunoController {

    private final AlunoService service;

    public AlunoController(AlunoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<AlunoResponse> criar(@Valid @RequestBody AlunoRequest request) {
        AlunoResponse criado = service.criar(request);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(uri).body(criado);
    }

    @PutMapping("/{id}")
    public AlunoResponse atualizar(@PathVariable Long id, @Valid @RequestBody AlunoRequest request) {
        return service.atualizar(id, request);
    }

    @GetMapping
    public List<AlunoResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public AlunoResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }
}
