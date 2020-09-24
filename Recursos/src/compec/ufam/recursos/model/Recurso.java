package compec.ufam.recursos.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.phill.libs.StringUtils;

/** Implementa a modelagem de um recurso
 *  @author Felipe André
 *  @version 2.0, 10/12/2019 */
public class Recurso {
	
	/** Atributo útil para debug de erros de processamento de planilha */
	private final int linha_excel;
	private final String planilha;

	private String disciplina;
	private int num_questao;
	
	private String nome_interessado;
	
	private String questionamento, alteracao;
	private String parecer, resposta;
	
	private String cargo;
	
	/** Construtor apenas seta a linha da planilha de onde estão vindo os dados */
	public Recurso(int linha_excel, String planilha) {
		this.linha_excel = linha_excel;
		this.planilha = planilha;
	}

	/************************ Bloco de Setters **************************/
	
	/** Seta a disciplina já fazendo algumas formatações para impressão.
	 *  @param disciplina - nome da disciplina */
	public void setDisciplina(String disciplina) {
		
		if (isEmpty(disciplina))
			log("Falha ao obter nome da disciplina.");
		else
			this.disciplina = StringUtils.BR.normaliza(disciplina).replace("questões","Questões").replace("A","a");
		
	}
	
	/** Validando e setando o número de questão.
	 *  @param num_questao - número da questão, armazenado aqui como 'int'. Caso o número informado via parâmetro seja nulo ou inválido, será atribuído o valor de erro '-1' nesta classe */
	public void setNumQuestao(String num_questao) {
		
		try {
			this.num_questao = Integer.parseInt(num_questao);
		}
		catch (Exception exception) {
			log("Número de questão inválido");
			this.num_questao = -1;
		}
		
	}
	
	/** Validando e setando o nome do interessado.
	 *  @param nome_interessado - nome do candidato que solicitou este recurso */
	public void setNomeInteressado(String nome_interessado) {
		
		if (isEmpty(nome_interessado))
			log("Nome do interessado é vazio");
		else
			this.nome_interessado = StringUtils.BR.normaliza(nome_interessado);
		
	}
	
	/** Validando e setando o questionamento do candidato.
	 *  @param questionamento - texto do questionamento do candidato */
	public void setQuestionamento(String questionamento) {
		
		if (isEmpty(questionamento))
			log("Questionamento vazio");
		else
			this.questionamento = questionamento.trim();
		
	}
	
	/** Validando e setando a solicitação de alteração de gabarito.
	 *  @param alteracao - representa a solicitação de alteração de gabarito preliminar por parte do candidato */
	public void setAlteracao(String alteracao) {
		
		if (isEmpty(alteracao))
			log("Solicitação de alteração de gabarito vazia");
		else
			this.alteracao = alteracao.trim();
		
	}
	
	/** Validando e setando o parecer da banca.
	 *  @param parecer - texto que representa o parecer de resposta da banca examinadora */
	public void setParecer(String parecer) {
		
		if (isEmpty(parecer))
			log("Parecer da banca vazio");
		else
			this.parecer = parecer.trim();
		
	}
	
	/** Validando e setando a resposta da banca.
	 *  @param resposta - resposta da banca sobre a solicitação de alteração do candidato */
	public void setResposta(String resposta) {
		
		if (isEmpty(resposta))
			log("Resposta da banca vazia");
		else
			this.resposta = resposta.trim();
		
	}
	
	/** Validando e setando o nome do cargo - exclusivo para os concursos públicos!
	 *  @param cargo - nome do cargo (com código embutido) */
	public void setCargo(String cargo) {
		
		if (isEmpty(cargo))
			log("Nome do cargo vazio");
		else
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
	
	public String getAlteracao() {
		return alteracao;
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
	
	/************************ Bloco de Helpers **************************/
	
	/** Implementa um simples log de processamento no seguinte formato:
	 *  [WRN: num_linha_planilha] mensagem
	 *  @param msg - mensagem de log */
	private void log(String msg) {
		System.err.printf("[WRN] '%s'@%d %s\n",this.planilha,this.linha_excel,msg);
	}
	
	/** Verifica se uma string é nula ou vazia
	 *  @param string - string */
	private boolean isEmpty(String string) {
		return ((string == null) || string.trim().isEmpty());
	}
	
	/** Verifica se o número de questão está fora do intervalo de questões deste recurso */
	public boolean foraIntervalo() {
		
		final int[] interval = getInterval();
		final boolean result = !(this.num_questao >= interval[0] && this.num_questao <= interval[1]);
		
		if (result)
			log("Número de questão fora do intervalo");
		
		return result;
		
	}
	
	/** Retorna um array com o intervalo de questões (extraído da string de disciplina) */
	private int[] getInterval() {
		
		Pattern pattern = Pattern.compile("\\d+");
		
		Matcher matcher = pattern.matcher(this.disciplina);
		matcher.find();
		
		String first_num_str = matcher.group();
		
		String original_without_first_num = this.disciplina.substring(this.disciplina.indexOf(first_num_str)).replace(first_num_str,"");
		
		matcher = pattern.matcher(original_without_first_num);
		matcher.find();
		
		String second_num_str = matcher.group();
		
		int first_num = Integer.valueOf(first_num_str);
		int second_num = Integer.valueOf(second_num_str);
		
		return new int[] {first_num,second_num};
		
	}
	
}
