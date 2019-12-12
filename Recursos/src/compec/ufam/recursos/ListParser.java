package compec.ufam.recursos;

import java.util.ArrayList;
import compec.ufam.recursos.model.Recurso;

public class ListParser {

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
