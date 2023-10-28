package compec.ufam.recursos.model;

public enum Fields {

	TIMESTAMP     ("Data e hora do recurso"),
	NOME          ("Nome do candidato"),
	CPF           ("CPF do candidato"),
	INSCRICAO     ("Número de inscrição"),
	CARGO         ("Cargo (quando couber)"),
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
	
	public Object[] getRowData() {
		return new Object[] { this.ordinal() + 1, this.fieldName };
	}

	public int getIndex() {
		return this.ordinal();
	}
	
}