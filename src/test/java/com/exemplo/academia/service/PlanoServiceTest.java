package com.exemplo.academia.service;

import com.exemplo.academia.dto.PlanoRequest;
import com.exemplo.academia.dto.PlanoResponse;
import com.exemplo.academia.exception.ConflitoException;
import com.exemplo.academia.exception.RecursoNaoEncontradoException;
import com.exemplo.academia.model.Plano;
import com.exemplo.academia.repository.MatriculaRepository;
import com.exemplo.academia.repository.PlanoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlanoServiceTest {

    @Mock PlanoRepository planoRepository;
    @Mock MatriculaRepository matriculaRepository;
    @InjectMocks PlanoService service;

    @Test
    void deveCriarPlano() {
        var request = new PlanoRequest("Mensal", new BigDecimal("99.90"), 1);
        when(planoRepository.existsByNome("Mensal")).thenReturn(false);
        when(planoRepository.save(any(Plano.class))).thenAnswer(inv -> {
            Plano p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        PlanoResponse resposta = service.criar(request);

        assertEquals(1L, resposta.id());
        assertEquals("Mensal", resposta.nome());
    }

    @Test
    void naoDeveCriarPlanoComNomeDuplicado() {
        when(planoRepository.existsByNome("Mensal")).thenReturn(true);

        assertThrows(ConflitoException.class,
                () -> service.criar(new PlanoRequest("Mensal", new BigDecimal("99.90"), 1)));

        verify(planoRepository, never()).save(any());
    }

    @Test
    void naoDeveRemoverPlanoComMatriculas() {
        when(planoRepository.existsById(1L)).thenReturn(true);
        when(matriculaRepository.existsByPlanoId(1L)).thenReturn(true);

        assertThrows(ConflitoException.class, () -> service.deletar(1L));

        verify(planoRepository, never()).deleteById(any());
    }

    @Test
    void deveRemoverPlanoSemMatriculas() {
        when(planoRepository.existsById(1L)).thenReturn(true);
        when(matriculaRepository.existsByPlanoId(1L)).thenReturn(false);

        service.deletar(1L);

        verify(planoRepository).deleteById(1L);
    }

    @Test
    void naoDeveRemoverPlanoInexistente() {
        when(planoRepository.existsById(99L)).thenReturn(false);

        assertThrows(RecursoNaoEncontradoException.class, () -> service.deletar(99L));
    }
}
