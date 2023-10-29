package compec.ufam.recursos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import compec.ufam.recursos.model.Recurso;
import compec.ufam.recursos.model.Recurso2;

public class ListParser {

	public static void gabarito(List<Recurso2> listaRecursos) {
		
		/*Map<Object, Map<String, String>> mapao =
		
				listaRecursos.stream()
		        .collect(Collectors.groupingBy(s -> s.getQuestao(),
		            Collectors.toMap(Recurso2::getRespostaBanca, Recurso2.class)));*/
		
	}
	
	// consertar modelagem e aqui, o soft tem que fazer isso no ato da leitura em excelreader
	public static void parseIntervalo(ArrayList<Recurso> listaRecursos) {
		
		for (Recurso recurso: listaRecursos)
			recurso.foraIntervalo();
		
	}
	
	public static void parseDouble(ArrayList<Recurso> listaRecursos) {
		
		ArrayList<Recurso> listaAlteracoes = new ArrayList<Recurso>();
		
		for (Recurso recurso: listaRecursos) {
			
			boolean added = false;
			
			for (Recurso alteracao: listaAlteracoes) {
				
				if (alteracao.getQuestao() == recurso.getQuestao()) {
					
					if (!alteracao.getResposta().equals(recurso.getResposta())) {
						System.err.println("[WRN] Respostas de banca diferentes para a questão " + recurso.getQuestao() + " de " + recurso.getDisciplina());
						listaAlteracoes.add(recurso);
					}
					
					added = true;
					break;
					
				}
				
				added = false;
				
			}
			
			if (!added)
				listaAlteracoes.add(recurso);
			
		}
		
		System.out.println("==> Gabarito: " + listaAlteracoes.get(0).getDisciplina());
		System.out.println();
		
		for (Recurso alteracao: listaAlteracoes)
			System.out.printf("Questão %d: %s\n",alteracao.getQuestao(),alteracao.getResposta());
		System.out.println();
		
	}
	
}
