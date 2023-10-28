package compec.ufam.recursos.io;

import java.io.*;
import java.text.*;
import java.time.LocalDateTime;
import java.util.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import com.phill.libs.StringUtils;
import com.phill.libs.time.PhillsDateParser;

import compec.ufam.recursos.model.Fields;
import compec.ufam.recursos.model.Recurso2;

public class ExcelReader {

	private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	private static final DataFormatter    DATA_FORMATTER = new DataFormatter(Locale.getDefault());
	
	public static List<Recurso2> read(final File planilha, final Integer[] indexes) {
		
		// Instanciando a lista de recursos
		final List<Recurso2> listaRecursos = new ArrayList<Recurso2>();
		
		try {
			
			// Abrindo a planilha para leitura
			FileInputStream stream    = new FileInputStream(planilha);
			XSSFWorkbook workbook     = new XSSFWorkbook(stream);
			XSSFSheet sheet           = workbook.getSheetAt(0);
			Iterator<Row> rowIterator = sheet.iterator();
			
			// Pulando o cabeçalho
			if (!parseHeader(rowIterator.next(), indexes)) {
				
				System.err.println("Arquivo fora de formato: " + planilha.getName());
				
				workbook.close();
				return null;
				
			}
			
			// Varrendo as linhas da planilha...
			while (rowIterator.hasNext()) {
								
				Row row = rowIterator.next();

				if (isEmptyRow(row)) break;
				
				final Recurso2 recurso = new Recurso2();
				
				// Carregando os dados de um recurso da linha atual da planilha
				recurso.setNomeCandidato(getNome(row, indexes));
				recurso.setDataRecurso(getDataRecurso(row, indexes));
				recurso.setInscricao(getInscricao(row, indexes));
				recurso.setCpfCandidato(getCPF(row, indexes));
				recurso.setObjeto(getCargo(row, indexes));
				recurso.setDisciplina(getDisciplina(row, indexes));
				recurso.setQuestao(getQuestao(row, indexes));
				recurso.setQuestionamentoCandidato(getQuestionamento(row, indexes));
				recurso.setAlteracaoCandidato(getRecurso(row, indexes));
				recurso.setAnexoCandidato(getAnexos(row, indexes));
				recurso.setParecerBanca(getParecer(row, indexes));
				recurso.setRespostaBanca(getDecisao(row, indexes));
				
				listaRecursos.add(recurso);
				
			}
			
			workbook.close();
			
		}
		catch (Exception exception) {
			
			exception.printStackTrace();
			return null;
			
		}
		
		return listaRecursos;
	}
	
	/** Verifica se uma linha da planilha é vazia.
	 *  @param row - linha da planilha
	 *  @return 'true' se <code>row</code> for nula, ou não conter dados */
	private static boolean isEmptyRow(final Row row) {
		
	    if (row == null)
	        return true;
	    
	    if (row.getLastCellNum() <= 0)
	        return true;
	    
	    for (int cellNum = row.getFirstCellNum(); cellNum < row.getLastCellNum(); cellNum++) {
	    	
	        Cell cell = row.getCell(cellNum);
	        
	        if (cell != null && cell.getCellType() != CellType.BLANK && !cell.toString().isBlank())
	            return false;
	        
	    }
	    
	    return true;
	}
	
	private static boolean parseHeader(final Row row, final Integer[] indexes) {
		
		for (Fields field: Fields.values())
			if (!field.getHeader().equals( getCellContent(row.getCell( indexes[field.getIndex()] )) ))
				return false;
		
		return true;
	}
	
	/****************** Bloco de Extração de Dados da Planilha *****************/
	
	private static LocalDateTime getDataRecurso(final Row row, final Integer[] indexes) {
		
		final Cell cell = row.getCell( indexes[Fields.TIMESTAMP.getIndex()] );
		
		return cell == null ? null : cell.getLocalDateTimeCellValue();
	}
	
	/** @return Nome do candidato.
	 *  @param row - linha da planilha */
	private static String getNome(final Row row, final Integer[] indexes) {
		return getCellContent(row.getCell( indexes[Fields.NOME.getIndex()] ));
	}
	
	/** @return CPF do candidato.
	 *  @param row - linha da planilha */
	private static String getCPF(final Row row, final Integer[] indexes) {
		
		final Cell cell = row.getCell( indexes[Fields.CPF.getIndex()] );
		String rawData = StringUtils.extractNumbers(getCellContent(cell));
		
		return rawData == null ? null : String.format("%011d", Long.parseLong(rawData));
	}
	
	private static Integer getInscricao(final Row row, final Integer[] indexes) {
		
		try { return Integer.parseInt( getCellContent(row.getCell( indexes[Fields.INSCRICAO.getIndex()] )) );	}
		catch (Exception exception) { return null; }
		
	}
	
	private static String getCargo(final Row row, final Integer[] indexes) {
		
		final Integer index = indexes[Fields.CARGO.getIndex()];
		
		return index == null ? null : getCellContent(row.getCell( index ));
		
	}
	
	private static String getDisciplina(final Row row, final Integer[] indexes) {
		return getCellContent(row.getCell( indexes[Fields.DISCIPLINA.getIndex()] ));
	}
	
	private static Integer getQuestao(final Row row, final Integer[] indexes) {
		
		try { return Integer.parseInt( getCellContent(row.getCell( indexes[Fields.QUESTAO.getIndex()] )) );	}
		catch (Exception exception) { return null; }
		
	}
	
	private static String getQuestionamento(final Row row, final Integer[] indexes) {
		return getCellContent(row.getCell( indexes[Fields.QUESTIONAMENTO.getIndex()] ));
	}
	
	private static String getAnexos(final Row row, final Integer[] indexes) {
		return getCellContent(row.getCell( indexes[Fields.ANEXOS.getIndex()] ));
	}
	
	private static String getRecurso(final Row row, final Integer[] indexes) {
		return getCellContent(row.getCell( indexes[Fields.RECURSO.getIndex()] ));
	}
	
	private static String getParecer(final Row row, final Integer[] indexes) {
		return getCellContent(row.getCell( indexes[Fields.PARECER.getIndex()] ));
	}
	
	private static String getDecisao(final Row row, final Integer[] indexes) {
		return getCellContent(row.getCell( indexes[Fields.DECISAO.getIndex()] ));
	}
	
	/** Extrai o conteúdo de uma célula do Excel.
	 *  @param cell - célula
	 *  @return String com o conteúdo de <code>cell</code>. */
	private static String getCellContent(final Cell cell) {
		
		if (cell == null)
			return null;
		
		switch (cell.getCellType()) {
		
			case STRING:
				return StringUtils.trim(cell.getStringCellValue());
			
			case NUMERIC:
				if (DateUtil.isCellDateFormatted(cell))
					return DATE_FORMATTER.format(cell.getDateCellValue());
				else
					return DATA_FORMATTER.formatCellValue(cell);
			
			case FORMULA:
				
				switch (cell.getCachedFormulaResultType()) {
				
					case STRING:
						return cell.getRichStringCellValue().toString();
					
					default:
						return null;
					
				}
				
			default:
				return null;
				
		}
		
	}

}