package compec.ufam.recursos.pdf;

import java.io.*;
import java.time.*;
import java.time.format.*;

import java.util.*;
import java.awt.image.*;

import javax.imageio.*;

import com.phill.libs.*;

import org.apache.commons.io.*;

import net.sf.jasperreports.view.*;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.*;
import net.sf.jasperreports.engine.util.*;

import compec.ufam.recursos.model.*;

/** Classe responsável pela construção e exibição do relatório 'Respostas aos Recursos'.
 *  @author Felipe André - felipeandre.eng@gmail.com
 *  @version 3.0, 31/OUT/2023 */
public class Resposta {

	/** Gera a visualização do relatório 'Respostas aos Recursos'.
	 *  @param edital - edital a ser impresso no cabeçalho do relatório
	 *  @param dataPublicacao - data de publicação do documento
	 *  @param planilha - planilha de origem dos dados
	 *  @param listaRecursos - recursos extraídos da planilha
	 *  @throws IOException quando o arquivo de logo do relatório (res/img/header-portrait.png) está inacessível
	 *  @throws JRException quando há alguma falha no carregamento do relatório Jasper */
	public static void show(final String edital, final LocalDate dataPublicacao, final File planilha, final List<Recurso> listaRecursos) throws IOException, JRException {
		
		// Construindo o relatório
		JasperPrint prints = buildReport(edital, dataPublicacao, planilha, listaRecursos);
		JasperViewer viewer = new JasperViewer(prints, false);
		
		// Exibindo o relatório
		viewer.setTitle("Recursys v.3.0 - Respostas aos Recursos");
		viewer.setVisible(true);
		
	}
	
	/** Exporta o relatório 'Respostas aos Recursos' para PDF no diretório de saída.
	 *  @param edital - edital a ser impresso no cabeçalho do relatório
	 *  @param dataPublicacao - data de publicação do documento
	 *  @param planilha - planilha de origem dos dados
	 *  @param listaRecursos - recursos extraídos da planilha
	 *  @param dirSaida - diretório de saída dos relatórios
	 *  @throws IOException quando o arquivo de logo do relatório (res/img/header-portrait.png) está inacessível
	 *  @throws JRException quando há alguma falha no carregamento do relatório Jasper */
	public static void exportPDF(final String edital, final LocalDate dataPublicacao, final File planilha, final List<Recurso> listaRecursos, final File dirSaida) throws IOException, JRException {
		
		// Preparando arquivo de saída
		File pdfDestino = new File(dirSaida, FilenameUtils.removeExtension(planilha.getName()) + ".pdf");
		
		// Preenchendo o relatório
		JasperPrint prints = buildReport(edital, dataPublicacao, planilha, listaRecursos);
				
		// Exportando pra PDF
		JasperExportManager.exportReportToPdfFile(prints, pdfDestino.getAbsolutePath());
		
	}
	
	/** Prepara o relatório 'Respostas aos Recursos' para impressão ou visualização.
	 *  @param edital - edital a ser impresso no cabeçalho do relatório
	 *  @param dataPublicacao - data de publicação do documento
	 *  @param planilha - planilha de origem dos dados
	 *  @param listaRecursos - recursos extraídos da planilha
	 *  @throws IOException quando o arquivo de logo do relatório (res/img/header-portrait.png) está inacessível
	 *  @throws JRException quando há alguma falha no carregamento do relatório Jasper */
	private static JasperPrint buildReport(final String edital, final LocalDate dataPublicacao, final File planilha, final List<Recurso> listaRecursos) throws IOException, JRException {
		
		// Leitura dos arquivos
		File     reportPath = ResourceManager.getResourceAsFile("reports/Resposta.jasper");
		BufferedImage  logo = ImageIO.read(ResourceManager.getResourceAsFile("img/header-portrait.png"));
		JasperReport report = (JasperReport) JRLoader.loadObject(reportPath);
				
		// Preparação dos parâmetros
		Map<String, Object> parameters = new HashMap<String, Object>();
			
		parameters.put("PAR_LOGO"     , logo  );
		parameters.put("PAR_CABECALHO", edital);
		parameters.put("PAR_DATA"     ,	dataPublicacao.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
		
		// Preparando data source
		JRBeanCollectionDataSource beansRecursos = new JRBeanCollectionDataSource(listaRecursos, false);
		
		// Preenchendo o relatório
		JasperPrint prints = JasperFillManager.fillReport(report, parameters, beansRecursos);
				
		return prints;
		
	}
	
}