package compec.ufam.recursos;

import java.io.*;
import java.util.*;
import javax.imageio.*;
import java.awt.image.*;
import com.phill.libs.*;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.*;
import net.sf.jasperreports.engine.util.*;
import net.sf.jasperreports.view.*;

public class PDFWriter {
	
	public static void export(String edital, String data_publicacao, ArrayList<Recurso> listaRecursos, File arquivo) throws Exception {
		
		/** Inflando o relatório */
		JasperPrint  prints = prepare(edital, data_publicacao, listaRecursos);
		JasperExportManager.exportReportToPdfFile(prints, arquivo.getAbsolutePath());
		
	}
	
	public static void show(String edital, String data_publicacao, ArrayList<Recurso> listaRecursos) throws Exception {
		
		/** Inflando o relatório */
		JasperPrint  prints = prepare(edital, data_publicacao, listaRecursos);
		
		/** Enfeitando a visualização do Jasper */
		JasperViewer jrv = new JasperViewer(prints,false);
		jrv.setTitle("Recursos de Questão");
		jrv.setVisible(true);
		
	}
	
	private static JasperPrint prepare(String edital, String data_publicacao, ArrayList<Recurso> listaRecursos) throws Exception {
		
		/** Leitura dos arquivos */
		File     reportPath = ResourceManager.getResourceAsFile("reports/Recursos.jasper");
		BufferedImage  logo = ImageIO.read(ResourceManager.getResourceAsFile("img/logo.jpg"));
		JasperReport report = (JasperReport) JRLoader.loadObject(reportPath);
		
		/** Preparação dos parâmetros */
		Map<String,Object> parameters = new HashMap<String,Object>();
		parameters.put("PAR_LOGO",logo);
		parameters.put("PAR_EDITAL",edital);
		parameters.put("PAR_DATA_PUBLICACAO", data_publicacao);
		
		/** Convertendo meu ArrayList para Beans */
		JRBeanCollectionDataSource beansConcursos = new JRBeanCollectionDataSource(listaRecursos,false);
		
		/** Inflando o relatório */
		JasperPrint  prints = JasperFillManager.fillReport(report, parameters, beansConcursos);
		
		return prints;
		
	}

}
