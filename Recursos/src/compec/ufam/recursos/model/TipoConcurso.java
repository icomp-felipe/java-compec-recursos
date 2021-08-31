package compec.ufam.recursos.model;

import java.io.File;
import com.phill.libs.ResourceManager;

/** Contém a configuração específica de cada concurso suportado pelo sistema. */
public enum TipoConcurso {
	
	PSTEC("reports/RecursosPSTEC.jasper"),
	PSC  ("reports/RecursosPSC.jasper"  ),
	PSE  ("reports/RecursosPSE.jasper"  ),
	EAD  ("reports/RecursosEAD.jasper"  );
	
	private File report;
	private String columns, columnNames;
	
	TipoConcurso(String report) {
		
		this.columns     = String.format("sheet.%s.columns"     ,name().toLowerCase());
		this.columnNames = String.format("sheet.%s.columns.name",name().toLowerCase());
		
		this.report = ResourceManager.getResourceAsFile(report);
		
	}
	
	/** Recupera o arquivo de relatório Jasper */
	public File getReport() {
		return this.report;
	}
	
	/** Recupera o campo de recurso */
	public String getColumns() {
		return this.columns;
	}
	
	public String getColumnNames() {
		return this.columnNames;
	}

}
