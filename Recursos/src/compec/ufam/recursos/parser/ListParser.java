package compec.ufam.recursos.parser;

import java.util.*;
import java.util.stream.*;

import compec.ufam.recursos.view.*;
import compec.ufam.recursos.model.*;

/** Implementa verificadores gerais de integridade de uma lista de recursos.
 *  @author Felipe André - felipeandre.eng@gmail.com
 *  @version 3.0, 31/OUT/2023 */
public class ListParser {

	/** Realiza verificação de duplicidade de decisões pra mesma questão e se há diferentes disciplinas na mesma lista.
	 *  @param listaRecursos - recursos extraídos da planilha
	 *  @param ui - interface gráfica principal para exibição de resultados */
	public static void parse(final List<Recurso> listaRecursos, final RecursysMainUI ui) {
		
		// Verificando se existem diferentes disciplinas
		Map<String, Recurso> mapaDisciplinas = listaRecursos.stream().collect(Collectors.toMap(Recurso::getDisciplina, recurso -> recurso, (recurso1, _) -> recurso1));
		
		if (mapaDisciplinas.size() > 1)
			ui.warning("Existem %d disciplinas no mesmo arquivo: %s", mapaDisciplinas.size(), Arrays.toString(mapaDisciplinas.keySet().toArray()));
		
		// Verificando decisões diferentes pra mesma questão
		
		// Agrupando recursos por número de questão 
		Map<Integer, List<Recurso>> mapaQuestoes = listaRecursos.stream().filter(recurso -> recurso.getQuestao() != null).collect(Collectors.groupingBy(Recurso::getQuestao, LinkedHashMap::new, Collectors.toCollection(ArrayList::new)));
		
		for (List<Recurso> recursosAgrupados: mapaQuestoes.values()) {
			
			// Agrupando recursos por decisão
			Map<String, Recurso> mapaDecisoes = recursosAgrupados.stream().collect(Collectors.toMap(Recurso::getDecisaoBanca, recurso -> recurso, (recurso1, _) -> recurso1));
			
			if (mapaDecisoes.size() > 1)
				ui.warning("Questão %d possui diferentes decisões: %s", recursosAgrupados.getFirst().getQuestao(), Arrays.toString(mapaDecisoes.keySet().toArray()));
			
		}
		
	}
	
}