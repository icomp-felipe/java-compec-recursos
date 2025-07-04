package compec.ufam.recursos.io;

import java.io.*;
import java.text.*;
import java.time.*;
import java.util.*;

import com.phill.libs.*;
import com.phill.libs.ui.AlertDialog;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import compec.ufam.recursos.view.*;
import compec.ufam.recursos.model.*;
import compec.ufam.recursos.parser.*;

/** Implementa os métodos de extração de recursos de uma planilha do Excel.
 *  @author Felipe André - felipeandre.eng@gmail.com
 *  @version 3.5, 18/JUN/2024 */
public class ExcelReader {

	private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	private static final DataFormatter    DATA_FORMATTER = new DataFormatter(Locale.getDefault());
	
	/** Realiza a leitura da planilha e extrai os dados para uma lista de recursos.
	 *  @param planilha - planilha de dados
	 *  @param indexes - índices das colunas
	 *  @param ignoreHeader - ativa ou desativa a verificação de cabeçalho da planilha de entrada
	 *  @param ui - interface gráfica principal
	 *  @return Lista com todos os recursos contidos na planilha, ou 'null' se ocorrer alguma exceção ou se a planilha não estiver no formato certo. */
	public static List<Recurso> read(final File planilha, final Integer[] indexes, final boolean ignoreHeader, final RecursysMainUI ui) {
		
		// Instanciando a lista de recursos
		final List<Recurso> listaRecursos = new ArrayList<Recurso>();
		
		try {
			
			ui.log("Processando planilha '%s'", planilha.getName());
			
			// Abrindo a planilha para leitura
			FileInputStream stream    = new FileInputStream(planilha);
			XSSFWorkbook workbook     = new XSSFWorkbook(stream);
			XSSFSheet sheet           = workbook.getSheetAt(0);
			Iterator<Row> rowIterator = sheet.iterator();
			
			// Analisando o cabeçalho, caso seja diferente do declarado em 'Fields' a leitura é encerrada por aqui
			if (!parseHeader(rowIterator.next(), ignoreHeader, indexes))
				ui.error("Arquivo fora de formato!");
			
			// Caso o cabeçalho seja válido, é iniciada a extração dos dados a partir das linhas da planilha
			else {
				
				while (rowIterator.hasNext()) {
									
					Row row = rowIterator.next();

					// Encerra a leitura quando encontra a primeira linha em branco
					if (isEmptyRow(row)) break;
					
					// Carregando os dados de um recurso da linha atual da planilha
					final Recurso recurso = new Recurso();
					
					recurso.setNomeCandidato (getNomeCandidato (row, indexes));
					recurso.setDataRecurso   (getDataRecurso   (row, indexes));
					recurso.setInscricao     (getInscricao     (row, indexes));
					recurso.setCPFCandidato  (getCPFCandidato  (row, indexes));
					recurso.setObjeto        (getObjeto        (row, indexes));
					recurso.setDisciplina    (getDisciplina    (row, indexes));
					recurso.setQuestao       (getQuestao       (row, indexes));
					recurso.setQuestionamento(getQuestionamento(row, indexes));
					recurso.setRecurso       (getRecurso       (row, indexes));
					recurso.setAnexoCandidato(getAnexoCandidato(row, indexes));
					recurso.setParecerBanca  (getParecerBanca  (row, indexes));
					recurso.setDecisaoBanca  (getDecisaoBanca  (row, indexes));
					
					// Realizando validação dos dados
					RecursoParser.parse(recurso, row, ui);
					
					listaRecursos.add(recurso);
					
				}
				
			}
			
			// Fechando a planilha
			workbook.close();
			
			// Realizando validação geral dos dados
			ListParser.parse(listaRecursos, ui);
			
		}
		catch (Exception exception) {
			
			exception.printStackTrace();
			AlertDialog.error(ui, ui.getTitle(), ui.getBundle().getFormattedString("xlsread-error", planilha.getName()));
			return null;
			
		}
		
		// Ordenando lista por número de questão e por nome de candidato
		Collections.sort(listaRecursos, Comparator.comparing    (Recurso::getQuestao)
				                                  .thenComparing(Recurso::getNomeNormalizado));
		
		return listaRecursos;
	}
	
	/************************* Bloco de Métodos Utilitários *******************************/
	
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
	
	/** Verifica se o cabeçalho da planilha é igual ao declarado em 'Fields'.
	 *  @param row - primeira linha da planilha
	 *  @param ignoreHeader - ativa ou desativa a verificação de cabeçalho da planilha
	 *  @param indexes - índices das colunas */
	private static boolean parseHeader(final Row row, final boolean ignoreHeader, final Integer[] indexes) {
		
		if (!ignoreHeader)
		
			for (Fields field: Fields.values())
				
				// O único campo que pode ser nulo é o cargo, pois só o PSTEC possui
				if (field == Fields.CARGO && indexes[field.getIndex()] == null) continue;
			
				// Caso contrário, verificar se existem as demais colunas na planilha
				else if (!field.getHeader().toLowerCase().equals( getCellContent(row.getCell( indexes[field.getIndex()] )).toLowerCase() ))
					return false;
		
		return true;
	}
	
	/*********************** Bloco de Extração de Dados da Planilha ***********************/
	
	/** Extrai a data de envio do recurso.
	 *  @param row - linha da planilha
	 *  @param indexes - índices das colunas
	 *  @return Data de envio do recurso. */
	private static LocalDateTime getDataRecurso(final Row row, final Integer[] indexes) {
		
		final Cell cell = row.getCell( indexes[Fields.TIMESTAMP.getIndex()] );
		
		try { return cell.getLocalDateTimeCellValue(); } catch (Exception exception) { return null; }
	}
	
	/** Extrai o nome do candidato.
	 *  @param row - linha da planilha
	 *  @param indexes - índices das colunas
	 *  @return Nome do candidato. */
	private static String getNomeCandidato(final Row row, final Integer[] indexes) {
		return getCellContent(row.getCell( indexes[Fields.NOME.getIndex()] ));
	}
	
	/** Extrai o número de CPF do candidato.
	 *  @param row - linha da planilha
	 *  @param indexes - índices das colunas
	 *  @return String representando o número de CPF do candidato. */
	private static String getCPFCandidato(final Row row, final Integer[] indexes) {
		
		final Cell cell = row.getCell( indexes[Fields.CPF.getIndex()] );
		String rawData = StringUtils.extractNumbers(getCellContent(cell));
		
		try { return String.format("%011d", Long.parseLong(rawData)); }
		catch (Exception exception) { return null; }
	}
	
	/** Extrai o número de inscrição do candidato.
	 *  @param row - linha da planilha
	 *  @param indexes - índices das colunas
	 *  @return Número de inscrição do candidato. */
	private static Integer getInscricao(final Row row, final Integer[] indexes) {
		
		try { return Integer.parseInt( getCellContent(row.getCell( indexes[Fields.INSCRICAO.getIndex()] )) );	}
		catch (Exception exception) { return null; }
		
	}
	
	/** Extrai objeto de execução do concurso (cargo ou 'null', no caso dos processos seletivos para graduação).
	 *  @param row - linha da planilha
	 *  @param indexes - índices das colunas
	 *  @return Objeto de execução do concurso. */
	private static String getObjeto(final Row row, final Integer[] indexes) {
		
		final Integer index = indexes[Fields.CARGO.getIndex()];
		
		return index == null ? null : getCellContent(row.getCell( index ));
		
	}
	
	/** Extrai a disciplina recursada.
	 *  @param row - linha da planilha
	 *  @param indexes - índices das colunas
	 *  @return Disciplina recursada. */
	private static String getDisciplina(final Row row, final Integer[] indexes) {
		return getCellContent(row.getCell( indexes[Fields.DISCIPLINA.getIndex()] ));
	}
	
	/** Extrai o número da questão recursada.
	 *  @param row - linha da planilha
	 *  @param indexes - índices das colunas
	 *  @return Número da questão recursada. */
	private static Integer getQuestao(final Row row, final Integer[] indexes) {
		
		try { return Integer.parseInt( StringUtils.extractNumbers(getCellContent(row.getCell( indexes[Fields.QUESTAO.getIndex()] )) ) );	}
		catch (Exception exception) { return 0; }
		
	}
	
	/** Extrai o questionamento do candidato.
	 *  @param row - linha da planilha
	 *  @param indexes - índices das colunas
	 *  @return Questionamento do candidato. */
	private static String getQuestionamento(final Row row, final Integer[] indexes) {
		return getCellContent(row.getCell( indexes[Fields.QUESTIONAMENTO.getIndex()] ));
	}
	
	/** Extrai o link de anexo de recurso do candidato.
	 *  @param row - linha da planilha
	 *  @param indexes - índices das colunas
	 *  @return Link de anexo de recurso do candidato. */
	private static String getAnexoCandidato(final Row row, final Integer[] indexes) {
		return getCellContent(row.getCell( indexes[Fields.ANEXOS.getIndex()] ));
	}
	
	/** Extrai o recurso do candidato (solicitação de alteração de gabarito).
	 *  @param row - linha da planilha
	 *  @param indexes - índices das colunas
	 *  @return Recurso do candidato. */
	private static String getRecurso(final Row row, final Integer[] indexes) {
		return getCellContent(row.getCell( indexes[Fields.RECURSO.getIndex()] ));
	}
	
	/** Extrai o parecer da banca examinadora.
	 *  @param row - linha da planilha
	 *  @param indexes - índices das colunas
	 *  @return Parecer da banca examinadora. */
	private static String getParecerBanca(final Row row, final Integer[] indexes) {
		return getCellContent(row.getCell( indexes[Fields.PARECER.getIndex()] ));
	}
	
	/** Extrai a decisão da banca examinadora.
	 *  @param row - linha da planilha
	 *  @param indexes - índices das colunas
	 *  @return Decisão da banca examinadora. */
	private static String getDecisaoBanca(final Row row, final Integer[] indexes) {
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