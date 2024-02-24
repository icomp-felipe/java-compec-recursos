package compec.ufam.recursos.parser;

import java.io.*;
import java.util.*;
import java.nio.file.*;

import org.apache.poi.ss.util.*;

import compec.ufam.recursos.io.*;
import compec.ufam.recursos.view.*;
import compec.ufam.recursos.model.*;

/** Implementa métodos para análise e carregamento de dados das planilhas de recursos.
 *  @author Felipe André - felipeandre.eng@gmail.com
 *  @version 3.5, 24/FEV/2024 */
public class DirectoryParser {

	/** Extrai os dados de todas as planilhas contidas em <code>directory</code> e seus subdiretórios par um mapa arquivo-recursos.
	 *  @param directory - diretório contendo as planilhas para análise e extração de dados
	 *  @param columns - índices das colunas
	 *  @param ignoreHeader - ativa ou desativa a verificação de cabeçalho das planilhas de entrada
	 *  @param ui - interface gráfica principal, para exibição de resultados
	 *  @return Mapeamento 'arquivo-recursos' contendo em cada entrada o arquivo da planilha seguido com uma lista de recursos carregados a partir dela.
	 *  @throws IOException quando as planilhas ou o diretório não podem ser lidos.  */
	public static Map<File, List<Recurso>> parse(final File directory, final String[] columns, final boolean ignoreHeader, final RecursysMainUI ui) throws IOException {
		
		final Map<File, List<Recurso>> mapaRecursos = new LinkedHashMap<File, List<Recurso>>();
		final Integer[] indexes = utilGetExcelIndexes(columns);
		
		Files.walk(directory.toPath())
        	 .filter (path -> path.toFile().isFile() && path.toFile().getName().endsWith("xlsx"))
        	 .forEach(path -> mapaRecursos.put(path.toFile(), ExcelReader.read(path.toFile(), indexes, ignoreHeader, ui)));
		
		return mapaRecursos;
	}
	
	/** Converte os índices das colunas da planilha de String pra int.
	 *  @param columns - índices das colunas no formato de String
	 *  @return Array de inteiros representando os índices no formato do Apache POI. */
	private static Integer[] utilGetExcelIndexes(final String[] columns) {
		
		final Integer[] indexes = new Integer[Constants.fieldCount];
		
		for (int i=0; i<Constants.fieldCount; i++)
			indexes[i] = columns[i] == null ? null : CellReference.convertColStringToIndex(columns[i]);
		
		return indexes;
	}
	
}