package compec.ufam.recursos.model;

import java.io.File;
import com.phill.libs.ResourceManager;

/** Contém a configuração específica de cada concurso suportado pelo sistema. */
public enum TipoConcurso {
	
	PSTEC("reports/RecursosPSTEC.jasper", new Fields[] { Fields.TIMESTAMP,
			                                             Fields.NOME,
			                                             Fields.CPF,
			                                             Fields.INSCRICAO,
			                                             Fields.CARGO,
			                                             Fields.TOPICO,
			                                             Fields.QUESTAO,
			                                             Fields.QUESTIONAMENTO,
			                                             Fields.ANEXOS,
			                                             Fields.RECURSO,
			                                             Fields.PARECER,
			                                             Fields.DECISAO
			                                           });
	
	/*PSC  ("reports/RecursosPSC.jasper"  , null),
	PSE  ("reports/RecursosPSE.jasper"  , null),
	EAD  ("reports/RecursosEAD.jasper"  , null);*/
	
	private File report;
	private String columns, columnNames;
	private Fields[] fields;
	
	TipoConcurso(final String report, final Fields[] fields) {
		
		this.columns     = String.format("sheet.%s.columns"     ,name().toLowerCase());
		this.columnNames = String.format("sheet.%s.columns.name",name().toLowerCase());
		
		this.fields = fields;
		
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
	
	public String[] getColumnNames() {
		
		StringBuilder builder = new StringBuilder();
		
		for (Fields field: this.fields)
			builder.append(field.getName() + ";");
		
		return builder.toString().split(";");
	}

	public enum Fields {
		
		TIMESTAMP     ("Data e hora do recurso"),
		NOME          ("Nome do candidato"),
		CPF           ("CPF do candidato"),
		INSCRICAO     ("Número de inscrição"),
		CARGO         ("Cargo"),
		TOPICO        ("Tópico"),
		DISCIPLINA    ("Disciplina"),
		QUESTAO       ("Questão"),
		QUESTIONAMENTO("Questionamento (Candidato)"),
		ANEXOS        ("Anexos (Candidato)"),
		RECURSO       ("Recurso (Candidato)"),
		PARECER       ("Parecer (Banca)"),
		DECISAO       ("Decisão (Banca)");
		
		private String fieldName;
		
		Fields(final String fieldName) {
			this.fieldName = fieldName;
		}
		
		public String getName() {
			return this.fieldName;
		}
		
	}
	
}