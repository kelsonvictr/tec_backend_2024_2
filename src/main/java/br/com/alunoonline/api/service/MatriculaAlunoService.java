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

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MatriculaAlunoService {

    private static final double MEDIA_PARA_APROVACAO = 7.0;

    @Autowired
    private MatriculaAlunoRepository matriculaAlunoRepository;

    /**
     * Realiza a matrícula do aluno em uma disciplina.
     * Define o status inicial como MATRICULADO e salva no banco.
     */
    public void criarMatricula(MatriculaAluno matriculaAluno) {
        matriculaAluno.setStatus(MatriculaAlunoStatusEnum.MATRICULADO);
        matriculaAlunoRepository.save(matriculaAluno);
    }

    /**
     * Permite que um aluno tranque sua matrícula em uma disciplina.
     * Apenas matrículas com status MATRICULADO podem ser trancadas.
     */
    public void trancarMatricula(Long matriculaAlunoId) {
        MatriculaAluno matriculaAluno = buscarMatriculaOuLancarExcecao(matriculaAlunoId);

        if (!matriculaAluno.getStatus().equals(MatriculaAlunoStatusEnum.MATRICULADO)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Só é possível trancar uma matrícula com o status MATRICULADO");
        }

        matriculaAluno.setStatus(MatriculaAlunoStatusEnum.TRANCADO);
        matriculaAlunoRepository.save(matriculaAluno);
    }

    /**
     * Atualiza as notas de um aluno em uma disciplina.
     * Se a nota não for enviada, mantém o valor atual.
     * Após atualizar as notas, recalcula a média e ajusta o status.
     */
    public void atualizarNotas(Long matriculaAlunoId, AtualizarNotasRequest atualizarNotasRequest) {
        MatriculaAluno matriculaAluno = buscarMatriculaOuLancarExcecao(matriculaAlunoId);

        if (atualizarNotasRequest.getNota1() != null) {
            matriculaAluno.setNota1(atualizarNotasRequest.getNota1());
        }

        if (atualizarNotasRequest.getNota2() != null) {
            matriculaAluno.setNota2(atualizarNotasRequest.getNota2());
        }

        calcularMediaEModificarStatus(matriculaAluno);
        matriculaAlunoRepository.save(matriculaAluno);
    }

    /**
     * Emite o histórico escolar do aluno, incluindo suas disciplinas, notas e status.
     */
    public HistoricoAlunoResponse emitirHistorico(Long alunoId) {
        List<MatriculaAluno> matriculasDoAluno = matriculaAlunoRepository.findByAlunoId(alunoId);

        if (matriculasDoAluno.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Esse aluno não possui matrículas");
        }

        HistoricoAlunoResponse historicoAluno = new HistoricoAlunoResponse();
        historicoAluno.setNomeAluno(matriculasDoAluno.get(0).getAluno().getNome());
        historicoAluno.setCpfAluno(matriculasDoAluno.get(0).getAluno().getCpf());
        historicoAluno.setEmailAluno(matriculasDoAluno.get(0).getAluno().getEmail());

        // Aqui estamos utilizando a API de Stream do Java para transformar uma lista de objetos em outra lista, com outro tipo de objeto.
        // O método "stream()" cria um fluxo (Stream) a partir da lista de matrículas do aluno.
        // Esse fluxo nos permite aplicar operações de transformação em cada elemento da lista original.

        // O método "map" é usado para transformar cada elemento da lista original (que é do tipo MatriculaAluno)
        // em um novo tipo de objeto (DisciplinasAlunoResponse).
        // Para isso, chamamos a função "mapearParaDisciplinasAlunoResponse", que converte os dados de uma matrícula
        // em um objeto com os dados da disciplina, nota, status, etc.
        // Ou seja, para cada matrícula, criamos um "resumo" da disciplina cursada pelo aluno.

        // O método "collect" serve para **coletar** os elementos processados no Stream e montar uma nova coleção.
        // No caso, usamos "Collectors.toList()" para dizer que queremos uma nova lista com os objetos transformados.
        // Ao final, teremos uma lista de DisciplinasAlunoResponse pronta para ser usada no histórico do aluno.

        List<DisciplinasAlunoResponse> disciplinas = matriculasDoAluno.stream()
                .map(this::mapearParaDisciplinasAlunoResponse) // transforma cada MatriculaAluno em DisciplinasAlunoResponse
                .collect(Collectors.toList()); // coleta os resultados em uma nova lista


        historicoAluno.setDisciplinasAlunoResponses(disciplinas);
        return historicoAluno;
    }

    /**
     * Busca uma matrícula pelo ID ou lança uma exceção caso não exista.
     */
    private MatriculaAluno buscarMatriculaOuLancarExcecao(Long matriculaAlunoId) {
        return matriculaAlunoRepository.findById(matriculaAlunoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Matrícula do aluno não encontrada!"));
    }

    /**
     * Calcula a média das notas e ajusta o status da matrícula (APROVADO ou REPROVADO).
     */
    private void calcularMediaEModificarStatus(MatriculaAluno matriculaAluno) {
        Double nota1 = matriculaAluno.getNota1();
        Double nota2 = matriculaAluno.getNota2();

        if (nota1 != null && nota2 != null) {
            double media = (nota1 + nota2) / 2;
            matriculaAluno.setStatus(media >= MEDIA_PARA_APROVACAO ?
                    MatriculaAlunoStatusEnum.APROVADO : MatriculaAlunoStatusEnum.REPROVADO);
        }
    }

    /**
     * Mapeia a matrícula para um objeto de resposta contendo informações sobre a disciplina.
     */
    private DisciplinasAlunoResponse mapearParaDisciplinasAlunoResponse(MatriculaAluno matriculaAluno) {
        DisciplinasAlunoResponse response = new DisciplinasAlunoResponse();
        response.setNomeDisciplina(matriculaAluno.getDisciplina().getNome());
        response.setNomeProfessor(matriculaAluno.getDisciplina().getProfessor().getNome());
        response.setNota1(matriculaAluno.getNota1());
        response.setNota2(matriculaAluno.getNota2());
        response.setMedia(calcularMedia(matriculaAluno.getNota1(), matriculaAluno.getNota2()));
        response.setStatus(matriculaAluno.getStatus());
        return response;
    }

    /**
     * Calcula a média de notas de um aluno. Retorna null se alguma das notas for inexistente.
     */
    private Double calcularMedia(Double nota1, Double nota2) {
        return (nota1 != null && nota2 != null) ? (nota1 + nota2) / 2.0 : null;
    }
}
