package br.com.alunoonline.api.service;

import br.com.alunoonline.api.model.Professor;
import br.com.alunoonline.api.repository.ProfessorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class ProfessorService {

    @Autowired
    ProfessorRepository professorRepository;

    public void criarProfessor(Professor professor) {
        professorRepository.save(professor);
    }

    public List<Professor> listarTodosProfessores() {
        return professorRepository.findAll();
    }

    public Optional<Professor> buscarProfessorPorId(Long id) {
        return professorRepository.findById(id);
    }

    public void atualizarProfessorPorId(Long id, Professor professorAtualizado) {
        Optional<Professor> professorDoBanco = professorRepository.findById(id);

        if (professorDoBanco.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Professor não encontrado");
        }

        Professor professor = professorDoBanco.get();
        professor.setNome(professorAtualizado.getNome());
        professor.setCpf(professorAtualizado.getCpf());
        professor.setEmail(professorAtualizado.getEmail());

        professorRepository.save(professor);
    }

    public void deletarProfessorPorId(Long id) {
        if (!professorRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Professor não encontrado");
        }

        professorRepository.deleteById(id);
    }
}
