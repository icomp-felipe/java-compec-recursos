package compec.ufam.recursos.model;

import java.time.*;

/** Modelagem de um recurso.
 *  @author Felipe André - felipeandre.eng@gmail.com
 *  @version 3.0, 28/OUT/2023 */
public class Recurso2 {

	private LocalDateTime dataRecurso;
	private String nomeCandidato, cpfCandidato;
	private Integer inscricao, questao;
	private String objeto, disciplina;
	private String questionamentoCandidato, anexoCandidato, recursoCandidato;
	private String parecerBanca, decisaoBanca;
	
	/************************ Bloco de Getters **************************/
	
	public String getCpfCandidato() {
		return cpfCandidato;
	}
	
	public LocalDateTime getDataRecurso() {
		return dataRecurso;
	}
	
	public String getNomeCandidato() {
		return nomeCandidato;
	}
	
	public Integer getInscricao() {
		return inscricao;
	}
	
	public Integer getQuestao() {
		return questao;
	}
	
	public String getObjeto() {
		return objeto;
	}
	
	public String getDisciplina() {
		return disciplina;
	}
	public String getQuestionamento() {
		return questionamentoCandidato;
	}
	
	public String getAnexoCandidato() {
		return anexoCandidato;
	}

	public String getAlteracaoCandidato() {
		return recursoCandidato;
	}
	
	public String getParecerBanca() {
		return parecerBanca;
	}
	
	public String getRespostaBanca() {
		return decisaoBanca;
	}
	
	/************************ Bloco de Setters **************************/
	
	/** Setter da data de envio do recurso.
	 *  @param dataRecurso - data de envio do recurso */
	public void setDataRecurso(final LocalDateTime dataRecurso) {
		this.dataRecurso = dataRecurso;
	}
	
	/** Setter do nome do candidato.
	 *  @param nomeCandidato - nome do candidato */
	public void setNomeCandidato(final String nomeCandidato) {
		this.nomeCandidato = nomeCandidato;
	}
	
	/** Setter do número de CPF do candidato.
	 *  @param cpfCandidato - número de CPF do candidato */
	public void setCPFCandidato(final String cpfCandidato) {
		this.cpfCandidato = cpfCandidato;
	}
	
	/** Setter do número de inscrição do candidato.
	 *  @param inscricao - número de inscrição do candidato */
	public void setInscricao(final Integer inscricao) {
		this.inscricao = inscricao;
	}
	
	/** Setter do número da questão recursada.
	 *  @param questao - número da questão recursada */
	public void setQuestao(final Integer questao) {
		this.questao = questao;
	}
	
	/** Setter do objeto de execução do concurso (cargo ou 'null', no caso dos processos seletivos para graduação).
	 *  @param objeto - objeto de execução do concurso */
	public void setObjeto(final String objeto) {
		this.objeto = objeto;
	}
	
	/** Setter da disciplina recursada.
	 *  @param disciplina - disciplina recursada */
	public void setDisciplina(final String disciplina) {
		this.disciplina = disciplina;
	}
	
	/** Setter do questionamento do candidato.
	 *  @param questionamentoCandidato - questionamento do candidato */
	public void setQuestionamento(final String questionamentoCandidato) {
		this.questionamentoCandidato = questionamentoCandidato;
	}
	
	/** Setter do link de anexo de recurso do candidato.
	 *  @param anexoCandidato - link de anexo de recurso do candidato */
	public void setAnexoCandidato(final String anexoCandidato) {
		this.anexoCandidato = anexoCandidato;
	}
	
	/** Setter do recurso do candidato (solicitação de alteração de gabarito).
	 *  @param recursoCandidato - recurso do candidato */
	public void setRecurso(final String recursoCandidato) {
		this.recursoCandidato = recursoCandidato;
	}
	
	/** Setter do parecer da banca examinadora.
	 *  @param parecerBanca - parecer da banca examinadora */
	public void setParecerBanca(final String parecerBanca) {
		this.parecerBanca = parecerBanca;
	}
	
	/** Setter da decisão da banca examinadora.
	 *  @param decisaoBanca - decisão da banca examinadora */
	public void setRespostaBanca(String decisaoBanca) {
		this.decisaoBanca = decisaoBanca;
	}
	
}