package com.exemplo.academia.controller;

import com.exemplo.academia.dto.PlanoRequest;
import com.exemplo.academia.dto.PlanoResponse;
import com.exemplo.academia.service.PlanoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/planos")
public class PlanoController {

    private final PlanoService service;

    public PlanoController(PlanoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PlanoResponse> criar(@Valid @RequestBody PlanoRequest request) {
        PlanoResponse criado = service.criar(request);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(uri).body(criado);
    }

    @GetMapping
    public List<PlanoResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public PlanoResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
