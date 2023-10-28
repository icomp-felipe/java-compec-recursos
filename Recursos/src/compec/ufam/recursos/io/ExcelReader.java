package compec.ufam.recursos.io;

import java.io.*;
import java.text.*;
import java.util.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.*;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

import com.phill.libs.StringUtils;
import com.phill.libs.time.PhillsDateParser;
import com.phill.libs.time.PhillsDateUtils;

import compec.ufam.recursos.model.Fields;
import compec.ufam.recursos.model.Recurso;
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
			rowIterator.next();
			
			// Varrendo as linhas da planilha...
			while (rowIterator.hasNext()) {
								
				Row row = rowIterator.next();
				
				final Recurso2 recurso = new Recurso2();
				
				// Carregando os dados de um recurso da linha atual da planilha
				recurso.setNomeCandidato(getNome(row, indexes));
				recurso.setDataRecurso(getDataRecurso(row, indexes));
				
				listaRecursos.add(recurso);
				
			}
			
			System.out.println(listaRecursos.size());
			
		}
		catch (Exception exception) {
			
			exception.printStackTrace();
			return null;
			
		}
		
		return listaRecursos;
	}
	
	/****************** Bloco de Extração de Dados da Planilha *****************/
	
	private static LocalDateTime getDataRecurso(final Row row, final Integer[] indexes) {
		
		final Cell cell = row.getCell( indexes[Fields.TIMESTAMP.getIndex()] );
		String rawData  = getCellContent(cell);
		
		DateTime dt = PhillsDateParser.createDate(rawData, "dd/MM/yyyy HH:mm:ss");
		
		if (dt != null)
			System.out.println(dt.toString("dd/MM/yyyy HH:mm:ss"));
		
		return dt == null ? null : dt.toLocalDateTime();
	}
	
	/** @return Nome do candidato.
	 *  @param row - linha da planilha */
	private static String getNome(final Row row, final Integer[] indexes) {
		
		final Cell cell = row.getCell( indexes[Fields.NOME.getIndex()] );
		String rawData  = getCellContent(cell);
		
		return StringUtils.trim(rawData);
	}
	
	
	/*
	 * Fields.TIMESTAMP.getRowData(),
												   Fields.NOME.getRowData(),
												   Fields.CPF.getRowData(),
												   Fields.INSCRICAO.getRowData(),
												   Fields.CARGO.getRowData(),
												   Fields.DISCIPLINA.getRowData(),
												   Fields.QUESTAO.getRowData(),
												   Fields.QUESTIONAMENTO.getRowData(),
												   Fields.ANEXOS.getRowData(),
												   Fields.RECURSO.getRowData(),
												   Fields.PARECER.getRowData(),
												   Fields.DECISAO.getRowData(),
	 * */
	
	
	
	public static void read(File planilha, String[] colunas, ArrayList<Recurso> listaRecursos) throws Exception {
		
		// Preparando o ambiente...
		FileInputStream stream           = new FileInputStream(planilha);
		XSSFWorkbook workbook            = new XSSFWorkbook(stream);
		XSSFSheet sheet                  = workbook.getSheetAt(0);
		Iterator<Row> rowIterator        = sheet.iterator();
		
		// Pulando a primeira linha da planilha (cabeçalho)
		rowIterator.next();
		
		// Varrendo as linhas da planilha...
		while (rowIterator.hasNext()) {
			
			// Carregando um Recurso da planilha
			Row row = rowIterator.next();
			Recurso recurso = extractRecurso(row,getIndices(colunas),planilha.getName());
			
			// Só é pra acontecer quando eu chegar numa linha vazia
			if (recurso == null)
				break;
			
			listaRecursos.add(recurso);
			
		}
		
		// Limpando a casa
		workbook.close();
		
	}
	
	/** Carrega um Recurso a partir de uma 'row' da planilha. Os campos
	 *  de dados são configurados de acordo com o parâmetro 'sheetIndexes'. */
	private static Recurso extractRecurso(Row row, int[] INDICES, String planilha) {
		
		// Aqui verifico se a primeira célula de uma linha é vazia
		Cell first_cell = row.getCell(INDICES[0]);
		
		if (first_cell == null)
			return null;
		
		// Se for vazia, significa que não tenho mais dados pra ler, então vou embora
		if (first_cell.getCellType().toString().equals("BLANK"))
			return null;
		
		// Extração de dados das células do Excel
		//String cargo            = getCellContent(first_cell);
		String nome_interessado = getCellContent(first_cell);
		String disciplina       = getCellContent(row.getCell(INDICES[1]));
		String num_questao      = getCellContent(row.getCell(INDICES[2]));
		String questionamento   = getCellContent(row.getCell(INDICES[3]));
		String solic_alteracao  = getCellContent(row.getCell(INDICES[4]));
		String parecer          = getCellContent(row.getCell(INDICES[5]));
		String resposta         = getCellContent(row.getCell(INDICES[6]));
		
		// Alimentando uma nova classe 'Recurso'
		Recurso recurso = new Recurso(row.getRowNum(),planilha);
		
		//recurso.setCargo(cargo);
		recurso.setNomeInteressado(nome_interessado);
		recurso.setDisciplina(disciplina);
		recurso.setNumQuestao(num_questao);
		recurso.setQuestionamento(questionamento);
		recurso.setAlteracao(solic_alteracao);
		recurso.setParecer(parecer);
		recurso.setResposta(resposta);
		
		return recurso;
	}

	/** 
	 *  1. Nome do Interessado
	 *  2. Disciplina
	 *  3. Número da Questão
	 *  4. Questionamento (Candidato)
	 *  5. Alteração (Candidato)
	 *  6. Parecer (Banca)
	 *  7. Resposta (Banca)  */
	private static int[] getIndices(String[] colunas) {
		
		final int size = colunas.length;
		int[] indices = new int[size];
		
		for (int i=0; i<size; i++)
			indices[i] = CellReference.convertColStringToIndex(colunas[i]);
		
		return indices;
	}
	
	/** Extrai o conteúdo de uma célula do Excel */
	private static String getCellContent(Cell cell) {
		
		// Se deu algum erro, pego minha bike e vou embora!
		if (cell == null)
			return null;
		
		// Especifica um tratamento diferente para os diversos tipos de dados
		switch (cell.getCellType()) {
		
			// Nem precisa eu dizer nada né ¯\_(ツ)_/¯
			case STRING:
				return cell.getStringCellValue();
			
			// Tratamento especial para datas (que são armazenadas em double pelo excel)
			case NUMERIC:
				if (DateUtil.isCellDateFormatted(cell)) {
					return DATE_FORMATTER.format(cell.getDateCellValue());
				}
				else {
					return DATA_FORMATTER.formatCellValue(cell);
				}
			
			/* Agora uma aulinha básica de Excel:
			 * For formula cells, excel stores two things. One is the Formula itself, the other is
			 * the "cached" value (the last value that the formula was evaluated as). If you want to
			 * get the last cached value (which may no longer be correct, but as long as Excel saved
			 * the file and you haven't changed it it should be), you'll want something like:     */
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
