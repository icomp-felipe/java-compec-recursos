package compec.ufam.recursos;

import com.phill.libs.StringUtils;

public class Recurso {

	private String disciplina;
	private int num_questao;
	private String nome_interessado;
	private String questionamento;
	private String parecer;
	private String resposta;
	private String cargo;

	/************************ Bloco de Setters **************************/
	
	public void setDisciplina(String disciplina) {
		this.disciplina = (disciplina != null) ? StringUtils.firstLetterLowerCase(disciplina).replace("questões","Questões").replace("A","a") : null;
	}
	
	public void setNumQuestao(String num_questao) {
		try {
			this.num_questao = Integer.parseInt(num_questao);
		}
		catch (Exception exception) {
			System.out.println("x Número de questão inválido!");
			this.num_questao = -1;
		}
	}
	
	public void setNomeInteressado(String nome_interessado) {
		this.nome_interessado = (nome_interessado != null) ? StringUtils.firstLetterLowerCase(nome_interessado) : null;
	}
	
	public void setQuestionamento(String questionamento) {
		this.questionamento = questionamento.trim();
	}
	
	public void setParecer(String parecer) {
		this.parecer = parecer.trim();
	}
	
	public void setResposta(String resposta) {
		this.resposta = resposta.trim();
	}
	
	public void setCargo(String cargo) {
		this.cargo = cargo.trim();
	}

	/************************ Bloco de Getters **************************/	
	
	public String getDisciplina() {
		return disciplina;
	}

	public int getQuestao() {
		return num_questao;
	}

	public String getInteressado() {
		return nome_interessado;
	}

	public String getQuestionamento() {
		return questionamento;
	}

	public String getParecer() {
		return parecer;
	}

	public String getResposta() {
		return resposta;
	}
	
	public String getCargo() {
		return cargo;
	}
	
}
