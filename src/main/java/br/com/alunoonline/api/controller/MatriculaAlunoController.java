package br.com.alunoonline.api.controller;

import br.com.alunoonline.api.dtos.AtualizarNotasRequest;
import br.com.alunoonline.api.dtos.HistoricoAlunoResponse;
import br.com.alunoonline.api.model.MatriculaAluno;
import br.com.alunoonline.api.service.MatriculaAlunoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/matriculas-alunos")
public class MatriculaAlunoController {

    @Autowired
    MatriculaAlunoService matriculaAlunoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void criarMatricula(@RequestBody MatriculaAluno matriculaAluno) {
        matriculaAlunoService.criarMatricula(matriculaAluno);
    }

    @PatchMapping("/trancar/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void trancarMatricula(@PathVariable Long id) {
        matriculaAlunoService.trancarMatricula(id);
    }

    @PatchMapping("/atualiza-notas/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizaNotas(@RequestBody AtualizarNotasRequest atualizarNotasRequest,
                                @PathVariable Long id)
    {
        matriculaAlunoService.atualizaNotas(id, atualizarNotasRequest);
    }

    @GetMapping("/emitir-historico/{alunoId}")
    @ResponseStatus(HttpStatus.OK)
    public HistoricoAlunoResponse emitirHistorico(@PathVariable Long alunoId) {
        return matriculaAlunoService.emitirHistorico(alunoId);
    }
}
