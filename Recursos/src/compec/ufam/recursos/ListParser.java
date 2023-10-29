package compec.ufam.recursos;

import java.io.File;
import java.util.*;
import java.util.stream.*;

import compec.ufam.recursos.view.*;
import compec.ufam.recursos.model.*;

/** Implementa verificadores gerais de integridade de uma lista de recursos.
 *  @author Felipe André - felipeandre.eng@gmail.com
 *  @version 3.0, 29/OUT/2023 */
public class ListParser {

	/** Realiza verificação de duplicidade de decisões pra mesma questão e se há diferentes disciplinas na mesma lista.
	 *  @param listaRecursos - recursos extraídos da planilha
	 *  @param ui - interface gráfica principal para exibição de resultados */
	public static void parse(List<Recurso2> listaRecursos, final RecursosGUI ui) {
		
		// Verificando se existem diferentes disciplinas
		Map<String, Recurso2> mapaDisciplinas = listaRecursos.stream().collect(Collectors.toMap(Recurso2::getDisciplina, recurso -> recurso, (recurso1, recurso2) -> recurso1));
		
		if (mapaDisciplinas.size() > 1)
			ui.warning("Existem %d disciplinas no mesmo arquivo: %s", mapaDisciplinas.size(), Arrays.toString(mapaDisciplinas.keySet().toArray()));
		
		// Verificando decisões diferentes pra mesma questão
		
		// Agrupando recursos por número de questão 
		Map<Integer, List<Recurso2>> mapaQuestoes = listaRecursos.stream().filter(recurso -> recurso.getQuestao() != null).collect(Collectors.groupingBy(Recurso2::getQuestao, LinkedHashMap::new, Collectors.toCollection(ArrayList::new)));
		
		for (List<Recurso2> recursosAgrupados: mapaQuestoes.values()) {
			
			// Agrupando recursos por decisão
			Map<String, Recurso2> mapaDecisoes = recursosAgrupados.stream().collect(Collectors.toMap(Recurso2::getDecisaoBanca, recurso -> recurso, (recurso1, recurso2) -> recurso1));
			
			if (mapaDecisoes.size() > 1)
				ui.warning("Questão %d possui diferentes decisões: %s", recursosAgrupados.getFirst().getQuestao(), Arrays.toString(mapaDecisoes.keySet().toArray()));
			
		}
		
	}
	
	/** Imprime o gabarito, calculado a partir da <code>listaRecursos</code>.
	 *  @param listaRecursos - recursos extraídos da planilha
	 *  @return String contendo o gabarito processado. */
	public static String gabarito(List<Recurso2> listaRecursos, File planilha) {

		// Calculando o título do objeto
		final String objeto = listaRecursos.getFirst().getObjeto();
		final String titulo = (objeto == null) ? planilha.getName() : objeto;
		
		final StringBuilder builder = new StringBuilder("==> " + titulo + "\n\n");
		
		// Filtra a lista por questão e decisão da banca
		Map<Integer, Map<String, Recurso2>> filtroQuestaoDecisao = listaRecursos.stream().filter(recurso -> recurso.getQuestao() != null).collect(Collectors.groupingBy(Recurso2::getQuestao, LinkedHashMap::new, Collectors.toMap(Recurso2::getDecisaoBanca, recurso -> recurso, (recurso1, recurso2) -> recurso1)));

		// Imprime o gabarito para cada questão
		for (Map.Entry<Integer, Map<String, Recurso2>> mapaQuestaoDecisoes: filtroQuestaoDecisao.entrySet()) {
			
			Map<String, Recurso2> mapaDecisoes = mapaQuestaoDecisoes.getValue();

			builder.append(String.format("Questão %d: %s\n", mapaQuestaoDecisoes.getKey(), mapaDecisoes.keySet()));
			
		}
		
		return builder.toString();
		
	}
	
}