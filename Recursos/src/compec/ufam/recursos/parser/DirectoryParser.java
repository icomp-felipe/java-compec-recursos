package compec.ufam.recursos.parser;

import java.io.*;
import java.util.*;
import java.nio.file.*;

import org.apache.poi.ss.util.*;

import compec.ufam.recursos.io.*;
import compec.ufam.recursos.view.*;
import compec.ufam.recursos.model.*;

public class DirectoryParser {

	public static Map<File, List<Recurso>> parse(final File directory, final String[] columns, final RecursosGUI ui) throws IOException {
		
		final Map<File, List<Recurso>> mapaRecursos = new LinkedHashMap<File, List<Recurso>>();
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