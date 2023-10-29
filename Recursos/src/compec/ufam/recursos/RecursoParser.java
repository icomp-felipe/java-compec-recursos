package compec.ufam.recursos;

import com.phill.libs.br.CPFParser;

import org.apache.poi.ss.usermodel.Row;

import compec.ufam.recursos.model.Recurso2;
import compec.ufam.recursos.view.RecursosGUI;

public class RecursoParser {

	public static boolean parse(final Recurso2 recurso, final Row row, final RecursosGUI ui) {
		
		final int linha = row.getRowNum() + 1;
		
		// Validação da data de envio do recurso
		if (recurso.getDataRecurso() == null)
			ui.warning("Linha %d: Impossível recuperar data de envio", linha);
		
		// Validação do nome do candidato
		if (recurso.getNomeCandidato() == null || recurso.getNomeCandidato().isBlank())
			ui.warning("Linha %d: Nome de candidato vazio", linha);
		
		// Validação do número de CPF do candidato
		if (!CPFParser.parse(recurso.getCPFCandidato()))
			ui.warning("Linha %d: CPF inválido", linha);
		
		// Validação do número de inscrição do candidato
		if (recurso.getInscricao() == null)
			ui.warning("Linha %d: Número de inscrição vazio", linha);
		
		else if (recurso.getInscricao() < 1)
			ui.warning("Linha %d: Número de inscrição inválido", linha);
		
		// Validação da disciplina
		if (recurso.getDisciplina() == null || recurso.getDisciplina().isBlank())
			ui.warning("Linha %d: Disciplina vazia", linha);
		
		// Validação do questionamento do candidato
		if (recurso.getQuestionamento() == null || recurso.getQuestionamento().isBlank())
			ui.warning("Linha %d: Questionamento vazio", linha);
		
		// Validação do recurso do candidato
		if (recurso.getRecurso() == null || recurso.getRecurso().isBlank())
			ui.warning("Linha %d: Recurso vazio", linha);
		
		// Validação parecer da banca examinadora
		if (recurso.getParecerBanca() == null || recurso.getParecerBanca().isBlank())
			ui.warning("Linha %d: Parecer de banca vazio", linha);
		
		// Validação da decisão da banca examinadora
		if (recurso.getRespostaBanca() == null || recurso.getRespostaBanca().isBlank())
			ui.warning("Linha %d: Decisão de banca vazia", linha);
		
		return true;
	}
	
}