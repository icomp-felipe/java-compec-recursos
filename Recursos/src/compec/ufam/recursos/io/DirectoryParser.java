package compec.ufam.recursos.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.util.CellReference;

import compec.ufam.recursos.model.Constants;
import compec.ufam.recursos.model.Recurso2;
import compec.ufam.recursos.view.RecursosGUI;

public class DirectoryParser {

	public static Map<File, List<Recurso2>> parse(final File directory, final String[] columns, final RecursosGUI ui) throws IOException {
		
		final Map<File, List<Recurso2>> mapaRecursos = new LinkedHashMap<File, List<Recurso2>>();
		final Integer[] indexes = utilGetExcelIndexes(columns);
		
		Files.walk(directory.toPath())
        	 .filter (path -> path.toFile().isFile() && path.toFile().getName().endsWith("xlsx"))
        	 .forEach(path -> mapaRecursos.put(path.toFile(), ExcelReader.read(path.toFile(), indexes, ui)));
		
		return mapaRecursos;
	}
	
	private static Integer[] utilGetExcelIndexes(final String[] columns) {
		
		final Integer[] indexes = new Integer[Constants.fieldCount];
		
		for (int i=0; i<Constants.fieldCount; i++)
			indexes[i] = columns[i] == null ? null : CellReference.convertColStringToIndex(columns[i]);
		
		return indexes;
	}
	
}