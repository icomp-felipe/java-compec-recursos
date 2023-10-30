package compec.ufam.recursos.pdf;

import java.io.*;
import java.util.*;
import java.util.stream.*;
import java.awt.image.*;

import javax.imageio.*;

import com.phill.libs.*;

import org.apache.commons.io.*;

import net.sf.jasperreports.view.*;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.util.*;

import compec.ufam.recursos.model.*;

public class Gabarito {

	public static void show(final String edital, final Map<File, List<Recurso>> mapaRecursos) throws IOException, JRException {
		
		// Leitura dos arquivos
		File     reportPath = ResourceManager.getResourceAsFile("reports/Gabarito.jasper");
		BufferedImage  logo = ImageIO.read(ResourceManager.getResourceAsFile("img/header-portrait.png"));
		JasperReport report = (JasperReport) JRLoader.loadObject(reportPath);
		
		// Preparação dos parâmetros
		Map<String, Object> parameters = new HashMap<String, Object>();
		
		parameters.put("PAR_LOGO"     , logo  );
		parameters.put("PAR_CABECALHO", edital);
		parameters.put("PAR_GABARITO" ,	compute(mapaRecursos));
		
		// Preenchendo o relatório
		JasperPrint  prints = JasperFillManager.fillReport(report, parameters, new JREmptyDataSource());
		
		// Preparando e exibindo o relatório
		JasperViewer viewer = new JasperViewer(prints, false);
		viewer.setTitle("Recursys v.3.0 - Resumo de Gabaritos");
		viewer.setVisible(true);
		
	}
	
	/** Computa o resumo dos gabaritos, de acordo com os recursos do <code>mapaRecursos</code>.
	 *  @param mapaRecursos - mapeamento de arquivos-recursos previamente processados
	 *  @return String contendo o resumo dos gabaritos computados. */
	public static String compute(final Map<File, List<Recurso>> mapaRecursos) {

		final StringBuilder builder = new StringBuilder();
		
		// Iterando por todas as entradas do mapa
		for (Map.Entry<File, List<Recurso>> entries: mapaRecursos.entrySet()) {
			
			// Recuperando os objetos do mapeamento atual
			File planilha = entries.getKey();
			List<Recurso> listaRecursos = entries.getValue();
			
			// Calculando o título do objeto
			builder.append("==> " + FilenameUtils.removeExtension(planilha.getName()) + "\n\n");
			
			// Filtra a lista por questão e decisão da banca
			Map<Integer, Map<String, Recurso>> filtroQuestaoDecisao = listaRecursos.stream().filter(recurso -> recurso.getQuestao() != null).collect(Collectors.groupingBy(Recurso::getQuestao, LinkedHashMap::new, Collectors.toMap(Recurso::getDecisaoBanca, recurso -> recurso, (recurso1, recurso2) -> recurso1)));

			// Imprime o gabarito para cada questão
			for (Map.Entry<Integer, Map<String, Recurso>> mapaQuestaoDecisoes: filtroQuestaoDecisao.entrySet()) {
				
				Map<String, Recurso> mapaDecisoes = mapaQuestaoDecisoes.getValue();

				builder.append(String.format("Questão %02d: %s\n", mapaQuestaoDecisoes.getKey(), mapaDecisoes.keySet()));
				
			}
			
			builder.append("\n");
			
		}
		
		return builder.toString().trim();
		
	}
	
}