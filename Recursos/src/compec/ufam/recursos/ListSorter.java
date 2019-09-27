package compec.ufam.recursos;

import java.util.*;
import java.util.stream.*;

public class ListSorter {
	
	public static ArrayList<Recurso> sort(ArrayList<Recurso> listaRecursos) {
		
		ArrayList <Recurso> listaOrdenada = new ArrayList<Recurso>(listaRecursos.size());
		Comparator<Recurso> comparator    = new NameComparator();
		
		// Aqui derivo várias listas de recursos em um Map, onde cada lista agrupa apenas um número de questão (mas as listas ainda estão desordenadas)
		Map<Integer,List<Recurso>> map_questoes = listaRecursos.stream().collect(Collectors.groupingBy(Recurso::getQuestao));
		
		// Nesta etapa faco a ordenacao do Map por ordem crescente de número de questões
		Map<Integer,List<Recurso>> sorted = map_questoes.entrySet().stream().sorted(Map.Entry.comparingByKey()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,(e1, e2) -> e2, LinkedHashMap::new));
		
		// Aqui ordeno todas as listas do Map por ordem alfabética de nomes de interessados
		sorted.forEach((key,value) -> Collections.sort(value,comparator));
		
		// Por fim, adiciono os elementos ordenados em uma nova lista
		sorted.forEach((key,value) -> merge(listaOrdenada,value));
		
		return listaOrdenada;
	}
	
	/** Apenas copia os dados de 'listaMap' para a 'listaRecursos' */
	private static void merge(ArrayList<Recurso> listaRecursos, List<Recurso> listaMap) {
		listaMap.forEach((recurso) -> listaRecursos.add(recurso));
	}
	
	/** Implementa o comparador de 'Retorno'. Trabalha com ordem alfabética do nome do candidato */
	private static class NameComparator implements Comparator<Recurso> {

		@Override
		public int compare(Recurso recurso1, Recurso recurso2) {
			return recurso1.getInteressado().compareTo(recurso2.getInteressado());
		}
		
	}

}
