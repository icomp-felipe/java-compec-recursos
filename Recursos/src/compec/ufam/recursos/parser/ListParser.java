package compec.ufam.recursos.parser;

import java.io.File;
import java.util.*;
import java.util.stream.*;

import org.apache.commons.io.FilenameUtils;

import compec.ufam.recursos.view.*;
import compec.ufam.recursos.model.*;

/** Implementa verificadores gerais de integridade de uma lista de recursos.
 *  @author Felipe André - felipeandre.eng@gmail.com
 *  @version 3.0, 29/OUT/2023 */
public class ListParser {

	/** Realiza verificação de duplicidade de decisões pra mesma questão e se há diferentes disciplinas na mesma lista.
	 *  @param listaRecursos - recursos extraídos da planilha
	 *  @param ui - interface gráfica principal para exibição de resultados */
	public static void parse(final List<Recurso> listaRecursos, final RecursosGUI ui) {
		
		// Verificando se existem diferentes disciplinas
		Map<String, Recurso> mapaDisciplinas = listaRecursos.stream().collect(Collectors.toMap(Recurso::getDisciplina, recurso -> recurso, (recurso1, recurso2) -> recurso1));
		
		if (mapaDisciplinas.size() > 1)
			ui.warning("Existem %d disciplinas no mesmo arquivo: %s", mapaDisciplinas.size(), Arrays.toString(mapaDisciplinas.keySet().toArray()));
		
		// Verificando decisões diferentes pra mesma questão
		
		// Agrupando recursos por número de questão 
		Map<Integer, List<Recurso>> mapaQuestoes = listaRecursos.stream().filter(recurso -> recurso.getQuestao() != null).collect(Collectors.groupingBy(Recurso::getQuestao, LinkedHashMap::new, Collectors.toCollection(ArrayList::new)));
		
		for (List<Recurso> recursosAgrupados: mapaQuestoes.values()) {
			
			// Agrupando recursos por decisão
			Map<String, Recurso> mapaDecisoes = recursosAgrupados.stream().collect(Collectors.toMap(Recurso::getDecisaoBanca, recurso -> recurso, (recurso1, recurso2) -> recurso1));
			
			if (mapaDecisoes.size() > 1)
				ui.warning("Questão %d possui diferentes decisões: %s", recursosAgrupados.getFirst().getQuestao(), Arrays.toString(mapaDecisoes.keySet().toArray()));
			
		}
		
	}
	
	/** Imprime o gabarito, calculado a partir da <code>listaRecursos</code>.
	 *  @param listaRecursos - recursos extraídos da planilha
	 *  @return String contendo o gabarito processado. */
	public static String gabarito(List<Recurso> listaRecursos, File planilha) {

		// Calculando o título do objeto
		final StringBuilder builder = new StringBuilder("==> " + FilenameUtils.removeExtension(planilha.getName()) + "\n\n");
		
		// Filtra a lista por questão e decisão da banca
		Map<Integer, Map<String, Recurso>> filtroQuestaoDecisao = listaRecursos.stream().filter(recurso -> recurso.getQuestao() != null).collect(Collectors.groupingBy(Recurso::getQuestao, LinkedHashMap::new, Collectors.toMap(Recurso::getDecisaoBanca, recurso -> recurso, (recurso1, recurso2) -> recurso1)));

		// Imprime o gabarito para cada questão
		for (Map.Entry<Integer, Map<String, Recurso>> mapaQuestaoDecisoes: filtroQuestaoDecisao.entrySet()) {
			
			Map<String, Recurso> mapaDecisoes = mapaQuestaoDecisoes.getValue();

			builder.append(String.format("Questão %02d: %s\n", mapaQuestaoDecisoes.getKey(), mapaDecisoes.keySet()));
			
		}
		
		System.out.println(builder.toString());
		
		return builder.toString();
		
	}
	
}