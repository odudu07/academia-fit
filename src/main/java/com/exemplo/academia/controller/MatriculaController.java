package com.exemplo.academia.controller;

import com.exemplo.academia.dto.MatriculaRequest;
import com.exemplo.academia.dto.MatriculaResponse;
import com.exemplo.academia.service.MatriculaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/matriculas")
public class MatriculaController {

    private final MatriculaService service;

    public MatriculaController(MatriculaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<MatriculaResponse> matricular(@Valid @RequestBody MatriculaRequest request) {
        MatriculaResponse criada = service.matricular(request);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criada.id()).toUri();
        return ResponseEntity.created(uri).body(criada);
    }

    @GetMapping
    public List<MatriculaResponse> listar(@RequestParam(required = false) Long alunoId) {
        return service.listar(alunoId);
    }

    @GetMapping("/{id}")
    public MatriculaResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PatchMapping("/{id}/cancelar")
    public MatriculaResponse cancelar(@PathVariable Long id) {
        return service.cancelar(id);
    }
}
