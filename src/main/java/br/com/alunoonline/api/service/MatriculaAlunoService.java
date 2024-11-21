package br.com.alunoonline.api.service;

import br.com.alunoonline.api.dtos.AtualizarNotasRequest;
import br.com.alunoonline.api.dtos.DisciplinasAlunoResponse;
import br.com.alunoonline.api.dtos.HistoricoAlunoResponse;
import br.com.alunoonline.api.enums.MatriculaAlunoStatusEnum;
import br.com.alunoonline.api.model.MatriculaAluno;
import br.com.alunoonline.api.repository.MatriculaAlunoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class MatriculaAlunoService {

    public static final double MEDIA_PARA_APROVACAO = 7.0;

    @Autowired
    MatriculaAlunoRepository matriculaAlunoRepository;

    /*
    É aqui que o aluno vai se matricular
     */
    public void criarMatricula(MatriculaAluno matriculaAluno) {
        matriculaAluno.setStatus(MatriculaAlunoStatusEnum.MATRICULADO);
        matriculaAlunoRepository.save(matriculaAluno);
    }

    /*
    É aqui que o aluno vai trancar sua matricula em alguma disciplina
     */
    public void trancarMatricula(Long matriculaAlunoId) {
        MatriculaAluno matriculaAluno =
                matriculaAlunoRepository.findById(matriculaAlunoId)
                        .orElseThrow(() ->
                                new ResponseStatusException(HttpStatus.NOT_FOUND,
                                        "Matricula Aluno não encontrada!"));

        if (!MatriculaAlunoStatusEnum.MATRICULADO.equals(matriculaAluno.getStatus())) {
            // Lançar o erro se o status não for matriculado
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Só é possível trancar uma matricula com o status MATRICULADO");
        }

        matriculaAluno.setStatus(MatriculaAlunoStatusEnum.TRANCADO);
        matriculaAlunoRepository.save(matriculaAluno);
    }

    public void atualizaNotas(Long matriculaAlunoId,
                              AtualizarNotasRequest atualizarNotasRequest) {
        MatriculaAluno matriculaAluno =
                matriculaAlunoRepository.findById(matriculaAlunoId)
                        .orElseThrow(() ->
                                new ResponseStatusException(HttpStatus.NOT_FOUND,
                                        "Matricula Aluno não encontrada!"));

        // Verifica se o front tá mandando a nota1
        // atualizarNotasRequest.getNota1(): Traz a nota que vem do front
        if (atualizarNotasRequest.getNota1() != null) {
            // matriculaAluno.setNota1: Atualiza a nota1 que vem atualmente do BD
            matriculaAluno.setNota1(atualizarNotasRequest.getNota1());
        }

        // Verifica se o front tá mandando a nota2
        // atualizarNotasRequest.getNota2(): Traz a nota que vem do front
        if (atualizarNotasRequest.getNota2() != null) {
            // matriculaAluno.setNota2: Atualiza a nota1 que vem atualmente do BD
            matriculaAluno.setNota2(atualizarNotasRequest.getNota2());
        }

        calculaMedia(matriculaAluno);
        matriculaAlunoRepository.save(matriculaAluno);

    }

    private void calculaMedia(MatriculaAluno matriculaAluno) {
        Double nota1 = matriculaAluno.getNota1();
        Double nota2 = matriculaAluno.getNota2();

        if (nota1 != null && nota2 != null) {
            Double media = (nota1 + nota2) / 2;
            matriculaAluno.setStatus(media >= MEDIA_PARA_APROVACAO ? MatriculaAlunoStatusEnum.APROVADO : MatriculaAlunoStatusEnum.REPROVADO);

        }
    }

    public HistoricoAlunoResponse emitirHistorico(Long alunoId) {
        List<MatriculaAluno> matriculasDoAluno = matriculaAlunoRepository.findByAlunoId(alunoId);

        if (matriculasDoAluno.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Esse aluno não possui matriculas");
        }

        HistoricoAlunoResponse historicoAluno = new HistoricoAlunoResponse();
        historicoAluno.setNomeAluno(matriculasDoAluno.get(0).getAluno().getNome());
        historicoAluno.setCpfAluno(matriculasDoAluno.get(0).getAluno().getCpf());
        historicoAluno.setEmailAluno(matriculasDoAluno.get(0).getAluno().getEmail());

        List<DisciplinasAlunoResponse> displinasList = new ArrayList<>();

        for (MatriculaAluno matriculaAluno : matriculasDoAluno) {
            DisciplinasAlunoResponse disciplinasAlunoResponse = new DisciplinasAlunoResponse();
            disciplinasAlunoResponse.setNomeDisciplina(matriculaAluno.getDisciplina().getNome());
            disciplinasAlunoResponse.setNomeProfessor(matriculaAluno.getDisciplina().getProfessor().getNome());
            disciplinasAlunoResponse.setNota1(matriculaAluno.getNota1());
            disciplinasAlunoResponse.setNota2(matriculaAluno.getNota2());

            // não quero isso nesse método, MAS eu (prof) vou fazer
            // se possível, reutilize o método de calcula média acima
            // refatore ele.

            if (matriculaAluno.getNota1() != null && matriculaAluno.getNota2() != null) {
                disciplinasAlunoResponse.setMedia((matriculaAluno.getNota1() + matriculaAluno.getNota2()) / 2.0);
            } else {
                disciplinasAlunoResponse.setMedia(null);
            }

            disciplinasAlunoResponse.setStatus(matriculaAluno.getStatus());

            displinasList.add(disciplinasAlunoResponse);
        }

        historicoAluno.setDisciplinasAlunoResponses(displinasList);

        return historicoAluno;
    }


}
