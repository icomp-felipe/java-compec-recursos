package compec.ufam.recursos.pdf;

import java.io.*;
import java.time.*;
import java.util.*;
import java.util.stream.*;
import java.awt.image.*;

import javax.imageio.*;

import com.phill.libs.*;

import org.apache.commons.io.*;

import net.sf.jasperreports.view.*;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.*;

import compec.ufam.recursos.model.*;

public class Resposta {

	public static void show(final String edital, final LocalDate date, final File planilha, final List<Recurso> listaRecursos) throws IOException, JRException {
		
		// Leitura dos arquivos
		File     reportPath = ResourceManager.getResourceAsFile("reports/Resposta.jasper");
		BufferedImage  logo = ImageIO.read(ResourceManager.getResourceAsFile("img/header-portrait.png"));
		JasperReport report = (JasperReport) JRLoader.loadObject(reportPath);
				
		// Preparação dos parâmetros
		Map<String, Object> parameters = new HashMap<String, Object>();
			
		parameters.put("PAR_LOGO"     , logo  );
		parameters.put("PAR_CABECALHO", edital);
		parameters.put("PAR_DATA"     ,	date  );
		
		// Preparando data source
		JRBeanCollectionDataSource beansRecursos = new JRBeanCollectionDataSource(listaRecursos, false);
		
		// Preenchendo o relatório
		JasperPrint  prints = JasperFillManager.fillReport(report, parameters, beansRecursos);
				
		// Preparando e exibindo o relatório
		JasperViewer viewer = new JasperViewer(prints, false);
		viewer.setTitle("Recursys v.3.0 - Resumo de Gabaritos");
		viewer.setVisible(true);
		
	}
	
}