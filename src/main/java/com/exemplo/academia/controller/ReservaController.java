package com.exemplo.academia.controller;

import com.exemplo.academia.dto.ReservaRequest;
import com.exemplo.academia.dto.ReservaResponse;
import com.exemplo.academia.service.ReservaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/reservas")
public class ReservaController {

    private final ReservaService service;

    public ReservaController(ReservaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ReservaResponse> reservar(@Valid @RequestBody ReservaRequest request) {
        ReservaResponse criada = service.reservar(request);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criada.id()).toUri();
        return ResponseEntity.created(uri).body(criada);
    }

    @GetMapping
    public List<ReservaResponse> listar(@RequestParam(required = false) Long alunoId) {
        return service.listar(alunoId);
    }

    @GetMapping("/{id}")
    public ReservaResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PatchMapping("/{id}/cancelar")
    public ReservaResponse cancelar(@PathVariable Long id) {
        return service.cancelar(id);
    }
}
