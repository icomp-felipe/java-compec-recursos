package compec.ufam.recursos.model;

import org.joda.time.*;

public class Recurso2 {

	private LocalDateTime dataRecurso;
	private String nomeCandidato, cpfCandidato;
	private Integer inscricao, questao;
	private String objeto, disciplina;
	private String questionamentoCandidato, anexoCandidato, alteracaoCandidato;
	private String parecerBanca, respostaBanca;
	public LocalDateTime getDataRecurso() {
		return dataRecurso;
	}
	public void setDataRecurso(LocalDateTime dataRecurso) {
		this.dataRecurso = dataRecurso;
	}
	public String getNomeCandidato() {
		return nomeCandidato;
	}
	public void setNomeCandidato(String nomeCandidato) {
		this.nomeCandidato = nomeCandidato;
	}
	public String getCpfCandidato() {
		return cpfCandidato;
	}
	public void setCpfCandidato(String cpfCandidato) {
		this.cpfCandidato = cpfCandidato;
	}
	public int getInscricao() {
		return inscricao;
	}
	public void setInscricao(Integer inscricao) {
		this.inscricao = inscricao;
	}
	public int getQuestao() {
		return questao;
	}
	public void setQuestao(Integer questao) {
		this.questao = questao;
	}
	public String getObjeto() {
		return objeto;
	}
	public void setObjeto(String objeto) {
		this.objeto = objeto;
	}
	public String getDisciplina() {
		return disciplina;
	}
	public void setDisciplina(String disciplina) {
		this.disciplina = disciplina;
	}
	public String getQuestionamentoCandidato() {
		return questionamentoCandidato;
	}
	public void setQuestionamentoCandidato(String questionamentoCandidato) {
		this.questionamentoCandidato = questionamentoCandidato;
	}
	public String getAnexoCandidato() {
		return anexoCandidato;
	}
	public void setAnexoCandidato(String anexoCandidato) {
		this.anexoCandidato = anexoCandidato;
	}
	public String getAlteracaoCandidato() {
		return alteracaoCandidato;
	}
	public void setAlteracaoCandidato(String alteracaoCandidato) {
		this.alteracaoCandidato = alteracaoCandidato;
	}
	public String getParecerBanca() {
		return parecerBanca;
	}
	public void setParecerBanca(String parecerBanca) {
		this.parecerBanca = parecerBanca;
	}
	public String getRespostaBanca() {
		return respostaBanca;
	}
	public void setRespostaBanca(String respostaBanca) {
		this.respostaBanca = respostaBanca;
	}
	
}