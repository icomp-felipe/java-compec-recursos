package compec.ufam.recursos.model;

/** Enum que armazena os campos de dados utilizados pelo sistema.
 *  Cada campo é composto de seu título e seu correspondente no cabeçalho da planilha do Excel.
 *  @author Felipe André - felipeandre.eng@gmail.com
 *  @version 3.0, 31/OUT/2023 */
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
	
	private String fieldName, sheetHeader;
	
	/** Construtor apenas inicializando o enum.
	 *  @param fieldName - título do campo
	 *  @param sheetHeader - título no cabeçalho da planilha */
	Fields(final String fieldName, final String sheetHeader) {
		this.fieldName = fieldName;
		this.sheetHeader = sheetHeader;
	}
	
	/** @return Um array de objetos contendo a posição de declaração deste enum, seguido de seu título de campo.
	 *  Útil para exibição na UI. */
	public Object[] getRowData() {
		return new Object[] { this.ordinal() + 1, this.fieldName };
	}

	/** @return O índice deste enum (começando em zero). */
	public int getIndex() {
		return this.ordinal();
	}

	/** @return O título do cabeçalho da planilha. */
	public String getHeader() {
		return this.sheetHeader;
	}
	
}