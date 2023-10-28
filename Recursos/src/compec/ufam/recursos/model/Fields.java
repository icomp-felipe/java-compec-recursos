package compec.ufam.recursos.model;

public enum Fields {

	TIMESTAMP     ("Data e hora do recurso", "Carimbo de data/hora"),
	NOME          ("Nome do candidato", "NOME COMPLETO"),
	CPF           ("CPF do candidato", "CPF"),
	INSCRICAO     ("Número de inscrição", "Nº INSCRIÇÃO"),
	CARGO         ("Cargo (quando couber)", "CARGO QUE ESTÁ CONCORRENDO"),
	DISCIPLINA    ("Disciplina", "Tópico:"),
	QUESTAO       ("Questão", "Número da questão"),
	QUESTIONAMENTO("Questionamento (Candidato)", "Fundamentação do questionamento:"),
	ANEXOS        ("Anexos (Candidato)", "Caso sinta a necessidade, anexe documentos (em PDF)  que fortaleçam o seu questionamento."),
	RECURSO       ("Recurso (Candidato)", "Alteração do Gabarito para:"),
	PARECER       ("Parecer (Banca)", "PARECER BANCA"),
	DECISAO       ("Decisão (Banca)", "RESPOSTA DA BANCA");
	
	private String fieldName, header;
	
	Fields(final String fieldName, final String header) {
		this.fieldName = fieldName;
		this.header = header;
	}
	
	public Object[] getRowData() {
		return new Object[] { this.ordinal() + 1, this.fieldName };
	}

	public int getIndex() {
		return this.ordinal();
	}

	public String getHeader() {
		return this.header;
	}
	
}