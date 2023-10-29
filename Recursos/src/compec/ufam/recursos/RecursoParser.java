package compec.ufam.recursos;

import com.phill.libs.br.*;

import java.util.regex.*;

import org.apache.poi.ss.usermodel.*;

import compec.ufam.recursos.view.*;
import compec.ufam.recursos.model.*;

/** Implementa os verificadores de integridade dos dados de um {@link Recurso2}.
 *  @author Felipe André - felipeandre.eng@gmail.com
 *  @version 3.0, 29/OUT/2023 */
public class RecursoParser {

	/** Realiza uma série de verificações de integridade dos dados de um <code>recurso</code> e exibe na <code>ui</code>.
	 *  @param recurso - recurso
	 *  @param row - linha da planilha de onde foi extraído o recurso
	 *  @param ui - interface gráfica principal, para exibição de resultados */
	public static void parse(final Recurso2 recurso, final Row row, final RecursosGUI ui) {
		
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
		
		// Verifica se o número da questão pertence à disciplina associada
		else {
			
			final Integer[] interval = getInterval(recurso);
			
			if (recurso.getQuestao() != null && !(recurso.getQuestao() >= interval[0] && recurso.getQuestao() <= interval[1]))
				ui.warning("Linha %d: Questão %d não pertence à disciplina '%s'", linha, recurso.getQuestao(), recurso.getDisciplina());
			
		}
		
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
		
	}
	
	/** Extrai um array com o intervalo de questões a partir da string da disciplina.
	 *  @param recurso - recurso
	 *  @return Array com dois inteiros, sendo o primeiro representando o número de questão
	 *  inicial e o segundo, o número da última questão contemplada pela disciplina do <code>recurso</code>,
	 *  ou 'null' caso não seja possível extrair os dois números a partir do <code>recurso</code>. */
	private static Integer[] getInterval(final Recurso2 recurso) {
		
		if (recurso != null) {
			
			final String disciplina = recurso.getDisciplina();
			
			Pattern pattern = Pattern.compile("\\d+");
				
			Matcher matcher = pattern.matcher(disciplina);
			matcher.find();
				
			String firstNumString = matcher.group();
				
			if (firstNumString != null) {
					
				String originalWithoutFirstNum = disciplina.substring(disciplina.indexOf(firstNumString)).replaceFirst(firstNumString, "");
					
				matcher = pattern.matcher(originalWithoutFirstNum);
				matcher.find();
					
				String secondNumString = matcher.group();
					
				if (secondNumString != null) {
						
					Integer firstNum  = Integer.valueOf(firstNumString );
					Integer secondNum = Integer.valueOf(secondNumString);
						
					return new Integer[] {firstNum, secondNum};
					
				}
					
			}
			
		}
		
		return null;
		
	}
	
}