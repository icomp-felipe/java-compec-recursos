package compec.ufam.recursos.model;

import java.time.*;
import java.time.format.*;

import com.phill.libs.*;
import com.phill.libs.br.*;

/** Modelagem de um recurso.
 *  @author Felipe André - felipeandre.eng@gmail.com
 *  @version 3.0, 30/OUT/2023 */
public class Recurso {

	private LocalDateTime dataRecurso;
	private String nomeCandidato, cpfCandidato;
	private Integer inscricao, questao;
	private String objeto, disciplina;
	private String questionamento, anexoCandidato, recurso;
	private String parecerBanca, decisaoBanca;
	
	/************************ Bloco de Getters **************************/
	
	/** @return Data de envio do recurso. */
	public LocalDateTime getDataRecurso() {
		return this.dataRecurso;
	}
	
	public String getDataRecursoString() {
		return this.dataRecurso == null ? null : this.dataRecurso.format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm:ss"));
	}
	
	/** @return Nome do candidato. */
	public String getNomeCandidato() {
		return this.nomeCandidato;
	}
	
	/** @return Nome do candidato (normalizado). */
	public String getNomeNormalizado() {
		return StringUtils.BR.normaliza(this.nomeCandidato);
	}
	
	/** @return Número de CPF do candidato. */
	public String getCPFCandidato() {
		return this.cpfCandidato;
	}
	
	/** @return Número de CPF do candidato, seguindo as regras da LGPD-BR. */
	public String getCPFOculto() {
		return CPFParser.oculta(this.cpfCandidato);
	}
	
	/** @return Número de inscrição do candidato. */
	public Integer getInscricao() {
		return this.inscricao;
	}
	
	/** @return Número da questão recursada. */
	public Integer getQuestao() {
		return this.questao;
	}
	
	/** @return Objeto de execução do concurso (cargo ou 'null', no caso dos processos seletivos para graduação). */
	public String getObjeto() {
		return this.objeto;
	}
	
	/** @return Disciplina recursada. */
	public String getDisciplina() {
		return this.disciplina == null ? null : this.disciplina.replace(".", "");
	}
	
	/** @return Questionamento do candidato. */
	public String getQuestionamento() {
		return this.questionamento;
	}
	
	/** @return Link de anexo de recurso do candidato. */
	public String getAnexoCandidato() {
		return this.anexoCandidato;
	}

	/** @return Recurso do candidato (solicitação de alteração de gabarito). */
	public String getRecurso() {
		return this.recurso;
	}
	
	/** @return Recurso do candidato (solicitação de alteração de gabarito). */
	public String getRecursoCompleto() {
		return this.recurso == null ? null
				                    : (this.recurso.trim().length() == 1 && StringUtils.isAlphaStringOnly(this.recurso))
				                    ? String.format("Alterar o gabarito para a letra \"%s\"", this.recurso.trim())
				                    : this.recurso;
	}
	
	/** @return Parecer da banca examinadora. */
	public String getParecerBanca() {
		return this.parecerBanca;
	}
	
	/** @return Decisão da banca examinadora. */
	public String getDecisaoBanca() {
		return this.decisaoBanca;
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
	 *  @param questionamento - questionamento do candidato */
	public void setQuestionamento(final String questionamento) {
		this.questionamento = questionamento;
	}
	
	/** Setter do link de anexo de recurso do candidato.
	 *  @param anexoCandidato - link de anexo de recurso do candidato */
	public void setAnexoCandidato(final String anexoCandidato) {
		this.anexoCandidato = anexoCandidato;
	}
	
	/** Setter do recurso do candidato (solicitação de alteração de gabarito).
	 *  @param recursoCandidato - recurso do candidato */
	public void setRecurso(final String recurso) {
		this.recurso = recurso;
	}
	
	/** Setter do parecer da banca examinadora.
	 *  @param parecerBanca - parecer da banca examinadora */
	public void setParecerBanca(final String parecerBanca) {
		this.parecerBanca = parecerBanca;
	}
	
	/** Setter da decisão da banca examinadora.
	 *  @param decisaoBanca - decisão da banca examinadora */
	public void setDecisaoBanca(String decisaoBanca) {
		this.decisaoBanca = decisaoBanca;
	}
	
}