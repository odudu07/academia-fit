package com.exemplo.academia.controller;

import com.exemplo.academia.dto.AulaRequest;
import com.exemplo.academia.dto.AulaResponse;
import com.exemplo.academia.service.AulaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/aulas")
public class AulaController {

    private final AulaService service;

    public AulaController(AulaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<AulaResponse> criar(@Valid @RequestBody AulaRequest request) {
        AulaResponse criada = service.criar(request);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criada.id()).toUri();
        return ResponseEntity.created(uri).body(criada);
    }

    @GetMapping
    public List<AulaResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public AulaResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }
}
