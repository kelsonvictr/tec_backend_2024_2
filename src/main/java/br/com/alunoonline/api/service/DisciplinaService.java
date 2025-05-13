package br.com.alunoonline.api.service;

import br.com.alunoonline.api.model.Disciplina;
import br.com.alunoonline.api.repository.DisciplinaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class DisciplinaService {

    @Autowired
    DisciplinaRepository disciplinaRepository;

    public void criarDisciplina(Disciplina disciplina) {
        disciplinaRepository.save(disciplina);
    }

    public List<Disciplina> listarDisciplinasDoProf(Long professorId) {
        return disciplinaRepository.findByProfessorId(professorId);
    }

    public List<Disciplina> listarTodasDisciplinas() {
        return disciplinaRepository.findAll();
    }

    public Optional<Disciplina> buscarDisciplinaPorId(Long id) {
        return disciplinaRepository.findById(id);
    }

    public void atualizarDisciplinaPorId(Long id, Disciplina disciplinaAtualizada) {
        Optional<Disciplina> disciplinaDoBanco = disciplinaRepository.findById(id);

        if (disciplinaDoBanco.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Disciplina não encontrada");
        }

        Disciplina disciplina = disciplinaDoBanco.get();
        disciplina.setNome(disciplinaAtualizada.getNome());
        disciplina.setProfessor(disciplinaAtualizada.getProfessor());

        disciplinaRepository.save(disciplina);
    }

    public void deletarDisciplinaPorId(Long id) {
        if (!disciplinaRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Disciplina não encontrada");
        }

        disciplinaRepository.deleteById(id);
    }

}
