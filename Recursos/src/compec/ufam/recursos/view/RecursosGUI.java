package compec.ufam.recursos.view;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import com.phill.libs.ui.AlertDialog;
import com.phill.libs.ui.ESCDispose;
import com.phill.libs.ui.GraphicsHelper;
import com.github.lgooddatepicker.components.DatePicker;
import com.phill.libs.PropertiesManager;
import com.phill.libs.ResourceManager;
import com.phill.libs.files.PhillFileUtils;
import com.phill.libs.i18n.PropertyBundle;
import com.phill.libs.table.JTableMouseListener;
import com.phill.libs.table.TableUtils;

import compec.ufam.recursos.ExcelReader;
import compec.ufam.recursos.ListParser;
import compec.ufam.recursos.ListSorter;
import compec.ufam.recursos.io.PDFWriter;
import compec.ufam.recursos.model.Recurso;
import compec.ufam.recursos.model.TipoConcurso;
import compec.ufam.recursos.util.LGoodDatePickerUtils;

/** Implementa a interface gráfica do sistema.
 *  @author Felipe André - felipeandre.eng@gmail.com
 *  @version 3.0, 27/OUT/2023 */
public class RecursosGUI extends JFrame {

	// Serial
	private static final long serialVersionUID = 6968825769983359575L;

	// Declaração de atributos gráficos
	private final JTextField textEdital;
	private final DatePicker datePicker;
	private final JComboBox<String> comboConcursos;
	private final JTable tablePlanilha;
    private final DefaultTableModel modelo;
	private final JLabel textItens, textDestino, textOrigem, textOBS, labelLoading;
	private final JButton buttonOrigem, buttonDestino, buttonLimpar, buttonProcessar;
	
	// Declaração de atributos dinâmicos
	private int dir_proc, fil_proc;
	private int dir_tot, fil_tot;
	private File dir_origem, dir_destino;
    private String[] colunas = new String [] {"#","Item","Coluna"};
	private ReportGenerator threadReports;
    private TipoConcurso concursoAtual;
	
	// Carregando bundle de idiomas
	private final static PropertyBundle bundle = new PropertyBundle("i18n/portuguese", null);
	
    
	public static void main(String[] args) { new RecursosGUI(); }

	public RecursosGUI() {
		super("Recursys v.3.0");
		
		// Inicializando atributos gráficos
		GraphicsHelper instance = GraphicsHelper.getInstance();
		//GraphicsHelper.setFrameIcon(this,"icon/windows-icon.png");
		ESCDispose.register(this);
		getContentPane().setLayout(null);
		
		// Recuperando ícones
		Icon searchIcon = ResourceManager.getIcon("icon/search-black.png",20,20);
		Icon clearIcon  = ResourceManager.getIcon("icon/clear.png",20,20);
		Icon reportIcon = ResourceManager.getIcon("icon/report.png",20,20);
		Icon saveIcon   = ResourceManager.getIcon("icon/save.png",20,20);
		
		// Recuperando fontes e cores
		Font  fonte = instance.getFont ();
		Color color = instance.getColor();
		
		JPanel painelID = new JPanel();
		painelID.setBorder(instance.getTitledBorder("Identificação do Concurso"));
		painelID.setBounds(12, 10, 695, 105);
		getContentPane().add(painelID);
		painelID.setLayout(null);
		
		JLabel labelEdital = new JLabel("Edital:");
		labelEdital.setFont(fonte);
		labelEdital.setBounds(12, 35, 60, 20);
		painelID.add(labelEdital);
		
		textEdital = new JTextField();
		textEdital.setToolTipText(bundle.getString("hint-text-edital"));
		textEdital.setFont(fonte);
		textEdital.setForeground(color);
		textEdital.setBounds(69, 33, 615, 25);
		painelID.add(textEdital);
		textEdital.setColumns(10);
		
		JLabel labelData = new JLabel("Data de Publicação dos Recursos:");
		labelData.setFont(fonte);
		labelData.setBounds(12, 70, 262, 20);
		painelID.add(labelData);
		
		datePicker = LGoodDatePickerUtils.getDatePicker();
		datePicker.getComponentDateTextField().setHorizontalAlignment(JTextField.CENTER);
		datePicker.setBounds(265, 70, 145, 30);
		painelID.add(datePicker);
		
		/*data_realizacao = DatePicker.getDatePicker();
		data_realizacao.setBounds(265, 70, 140, 25);
		painelID.add(data_realizacao);*/
		
		JLabel labelTipoConcurso = new JLabel("Tipo de Concurso:");
		labelTipoConcurso.setFont(fonte);
		labelTipoConcurso.setBounds(420, 70, 131, 20);
		painelID.add(labelTipoConcurso);
		
		comboConcursos = new JComboBox<String>();
		comboConcursos.addActionListener((event) -> action_update_table());
		comboConcursos.setForeground(color);
		comboConcursos.setFont(fonte);
		comboConcursos.setBounds(559, 68, 125, 24);
		painelID.add(comboConcursos);
		
		modelo  = new MyTableModel(colunas);
		
		tablePlanilha = new JTable(modelo);
		tablePlanilha.setOpaque(false);
		tablePlanilha.addMouseListener(new JTableMouseListener(tablePlanilha));
		tablePlanilha.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		final DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		
		TableColumnModel columnModel = tablePlanilha.getColumnModel();
		
		columnModel.getColumn(0).setCellRenderer(centerRenderer);
		columnModel.getColumn(2).setCellRenderer(centerRenderer);
		
		columnModel.getColumn(0).setPreferredWidth(5);
		columnModel.getColumn(1).setPreferredWidth(400);
		
		JPanel painelPlanilha = new JPanel();
		painelPlanilha.setBorder(instance.getTitledBorder("Configuração da Planilha"));
		painelPlanilha.setBounds(12, 120, 695, 220);
		getContentPane().add(painelPlanilha);
		painelPlanilha.setLayout(null);
		
		JScrollPane scrollPlanilha = new JScrollPane(tablePlanilha);
		scrollPlanilha.setBounds(12, 22, 672, 150);
		painelPlanilha.add(scrollPlanilha);
		

		
		JButton botaoSalvaConfig = new JButton(saveIcon);
		botaoSalvaConfig.addActionListener((event) -> action_salva_config());
		
		JLabel labelItens = new JLabel("Quantidade de Itens:");
		labelItens.setFont(fonte);
		labelItens.setBounds(12, 190, 150, 20);
		painelPlanilha.add(labelItens);
		
		textItens = new JLabel("0");
		textItens.setForeground(color);
		textItens.setFont(fonte);
		textItens.setBounds(167, 189, 70, 20);
		painelPlanilha.add(textItens);
		botaoSalvaConfig.setToolTipText("Salvar configuração");
		botaoSalvaConfig.setBounds(653, 184, 30, 25);
		painelPlanilha.add(botaoSalvaConfig);
		
		JPanel painelPastas = new JPanel();
		painelPastas.setBorder(instance.getTitledBorder("Pastas"));
		painelPastas.setBounds(12, 352, 696, 105);
		getContentPane().add(painelPastas);
		painelPastas.setLayout(null);
		
		JLabel labelOrigem = new JLabel("Origem (Planilhas):");
		labelOrigem.setFont(fonte);
		labelOrigem.setBounds(12, 35, 140, 20);
		painelPastas.add(labelOrigem);
		
		textOrigem = new JLabel("<Selecionar Arquivo>");
		textOrigem.setFont(fonte);
		textOrigem.setForeground(color);
		textOrigem.setBounds(164, 35, 475, 20);
		painelPastas.add(textOrigem);
		
		buttonOrigem = new JButton(searchIcon);
		buttonOrigem.setToolTipText("Selecionar a pasta de origem (planilhas)");
		buttonOrigem.addActionListener((event) -> action_seleciona_origem());
		buttonOrigem.setBounds(650, 32, 30, 25);
		painelPastas.add(buttonOrigem);
		
		JLabel labelDestino = new JLabel("Destino (PDF):");
		labelDestino.setFont(fonte);
		labelDestino.setBounds(12, 65, 140, 20);
		painelPastas.add(labelDestino);
		
		textDestino = new JLabel("<Selecionar Arquivo>");
		textDestino.setForeground(color);
		textDestino.setFont(fonte);
		textDestino.setBounds(164, 65, 473, 20);
		painelPastas.add(textDestino);
		
		buttonDestino = new JButton(searchIcon);
		buttonDestino.setToolTipText("Selecionar a pasta de destino (PDF)");
		buttonDestino.addActionListener((event) -> action_seleciona_destino());
		buttonDestino.setBounds(650, 63, 30, 25);
		painelPastas.add(buttonDestino);
		
		ImageIcon loading = new ImageIcon(ResourceManager.getResource("img/ajax-loader.gif"));
		
		textOBS = new JLabel();
		textOBS.setFont(fonte);
		textOBS.setForeground(color);
		textOBS.setBounds(44, 464, 528, 44);
		textOBS.setVisible(false);
		getContentPane().add(textOBS);
		
		labelLoading = new JLabel(loading,SwingConstants.LEFT);
		labelLoading.setBounds(12, 464, 20, 35);
		labelLoading.setVisible(false);
		getContentPane().add(labelLoading);
		
		buttonLimpar = new JButton(clearIcon);
		buttonLimpar.setToolTipText("Limpar os dados da tela");
		buttonLimpar.addActionListener((event) -> action_clear());
		buttonLimpar.setBounds(678, 474, 30, 25);
		getContentPane().add(buttonLimpar);
		
		buttonProcessar = new JButton(reportIcon);
		buttonProcessar.setToolTipText("Gerar os recursos em PDF");
		buttonProcessar.addActionListener((event) -> action_proccess());
		buttonProcessar.setBounds(600, 474, 30, 25);
		getContentPane().add(buttonProcessar);
		
		init_load_combo();
		
		setSize(720,545);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		getContentPane().setLayout(null);
		setVisible(true);
		
	}
	
	/** Limpa os dados da tela */
	private void action_clear() {
		
		int res  = AlertDialog.dialog(this, "Você tem certeza que deseja limpar os dados da tela?");
		if (res != AlertDialog.OK_OPTION)
			return;
		
		textEdital.setText(null);
		//data_realizacao.getJFormattedTextField().setText(null);
		
		textOrigem.setText("<Selecionar Arquivo>");
		textDestino.setText("<Selecionar Arquivo>");
		dir_origem = dir_destino = null;
		
		textOBS.setVisible(false);
		
	}
	

	/** Valida os dados da tela e, se tudo estiver certo, inicia a geração de relatórios */
	private void action_proccess() {
		
		// Recuperando os dados da tela
		String    edital = textEdital.getText().trim();
		String      data = null; //data_realizacao.getJFormattedTextField().getText();
		
		// Se os dados da tela são válidos, inicio os trabalhos
		if (util_parse_view(edital,data)) {
			
			try {
				
				this.threadReports = new ReportGenerator(edital,data);
				this.threadReports.setName("Thread de geração de relatórios");
				this.threadReports.start();
				
			}
			catch (Exception exception) {
				exception.printStackTrace();
			}
			
		}
		
	}
	
	/** Salva a configuração de planilhas no arquivo de propriedades */
	private void action_salva_config() {

		// Configurando as variáveis
		final int rows = modelo.getRowCount();
		
		try {
		
			final String[] columns = new String[rows];
			
			// Varre todas as linhas da tabela recuperando as colunas configuradas
			for (int i=0; i<rows; i++)
				columns[i] = modelo.getValueAt(i,2).toString();
			
			// Salvando configuração no arquivo de propriedades
			PropertiesManager.setStringArray(this.concursoAtual.getColumns(),columns,null);
			
			// Alertando usuário
			AlertDialog.info(this, "Configuração de planilha salva com sucesso!");
		
		}
		catch (Exception exception) {
			
			exception.printStackTrace();
			AlertDialog.error(this, "Salvando configuração","Houve algum erro durante o salvamento de configuração.\nFavor consultar o console para mais infos.");
			
		}
		finally {
			
			// Recarregando configurações
			action_update_table();
			
		}
		
	}
	
	/** Seleciona o diretório de destino */
	private void action_seleciona_destino() {
		
		try {
			
			// Abrindo a GUI de seleção de arquivo
			dir_destino = PhillFileUtils.loadDir(this, "Selecione a pasta de destino (PDF)", PhillFileUtils.SAVE_DIALOG, null);
			
			// Atualizando o nome do arquivo no label de seleção
			textDestino.setText(dir_destino.getAbsolutePath());
			
		}
		catch (NullPointerException exception) { }
		catch (Exception exception) { AlertDialog.error(this, "Não foi possível carregar o diretório"); }
		
	}
	
	/** Seleciona o diretório de origem */
	private void action_seleciona_origem() {
		
		try {
			
			// Abrindo a GUI de seleção de arquivo
			dir_origem = PhillFileUtils.loadDir(this, "Selecione a pasta de origem (planilhas)", PhillFileUtils.OPEN_DIALOG, null);
			
			// Atualizando o nome do arquivo no label de seleção
			textOrigem.setText(dir_origem.getAbsolutePath());
			
			// Carrega o diretório informado e exibe algumas estatísticas
			new Thread(() -> util_parse_dir()).start();
			
		}
		catch (NullPointerException exception) { }
		catch (Exception exception) { AlertDialog.error(this, "Não foi possível carregar o diretório"); }
		
	}
	
	/** Atualiza a tabela com as configurações do arquivo de propriedades */
	private void action_update_table() {

		// Recuperando o concurso selecionado
		this.concursoAtual = TipoConcurso.valueOf(comboConcursos.getSelectedItem().toString());
		
		try {
			
			// Recuperando as colunas e dados
			String[] columnNames = PropertiesManager.getStringArray(this.concursoAtual.getColumnNames(),null);
			String[] columns     = PropertiesManager.getStringArray(this.concursoAtual.getColumns(),null);
			
			// Simples tratamento de erros
			if (columnNames.length != columns.length) {
				
				AlertDialog.error(this, "Configuração de Planilhas","Configuração inválida, favor conferir arquivo de propriedades");
				return;
				
			}
			
			// Limpa os dados da tabela
			TableUtils.clear(modelo);
			
			// Preenche a tabela com os dados carregados do arquivo de propriedades
			for (int i=0; i<columns.length; i++)
				modelo.addRow(new Object[]{ i+1, columnNames[i], columns[i] });		// | # | Item | Coluna |
			
		}
		catch (Exception exception) {
			exception.printStackTrace();
		}
		finally {
			
			// Atualizando a quantidade de itens carregados
			TableUtils.updateSize(modelo,textItens);
			
		}
		
	}
	
	/** Inicializa o combo de tipos de concurso */
	private void init_load_combo() {
		
		for (TipoConcurso concurso: TipoConcurso.values())
			comboConcursos.addItem(concurso.name());
		
	}

	/** Analisa o diretório de origem de planilhas */
	private void util_parse_dir() {
		
		// Zerando os contadores
		this.dir_tot = this.fil_tot = 0;
		
		// Se o diretório de origem for legível...
		if (dir_origem.canRead()) {
			
			// ...vou atualizando a view e...
			SwingUtilities.invokeLater(() -> {
												labelLoading.setVisible(true);
												textOBS.setVisible(true);
												textOBS.setText("Lendo diretório");
											 });
			
			// ...varrendo os subdiretórios em busca de planilhas
			for (File sub_dir: dir_origem.listFiles()) {
				
				if (sub_dir.isDirectory() && sub_dir.canRead()) {
					
					this.dir_tot++;
					
					for (File sub_file: sub_dir.listFiles()) {
						
						if (sub_file.isFile() && sub_file.getName().endsWith(".xlsx"))
							this.fil_tot++;
						
					}
					
				}
				
			}
			
		}
		
		// Por fim, atualizo a view com as informações encontradas
		String obs = String.format("Encontrada(s) %d planilha(s) em %d pasta(s)",this.fil_tot,this.dir_tot);
		SwingUtilities.invokeLater(() -> {
											textOBS.setText(obs);
											labelLoading.setVisible(false);
									     });
		
	}
	
	/** Valida os dados da tela */
	private boolean util_parse_view(String edital, String data) {
		
		if (edital.equals("")) {
			AlertDialog.error(this, "Preencha o nome do edital");
			return false;
		}
		
		if (data.equals("")) {
			AlertDialog.error(this, "Selecione uma data");
			return false;
		}
		
		if (dir_origem == null) {
			AlertDialog.error(this, "Selecione a pasta de origem");
			return false;
		}
		
		if (dir_destino == null) {
			AlertDialog.error(this, "Selecione a pasta de destino");
			return false;
		}
		
		if (AlertDialog.dialog(this, "Deseja mesmo continuar com o processamento?") != AlertDialog.OK_OPTION)
			return false;
		
		return true;
		
	}


	
	
	

	
	
	private class ReportGenerator extends Thread {
		
		private final String edital, data;
		private final String[] colunas;
		
		public ReportGenerator(String edital, String data) throws IOException {
			
			this.edital  = edital;
			this.data    = data;
			this.colunas = PropertiesManager.getStringArray(concursoAtual.getColumns(),null);
			
		}
		
		@Override
		public void run() {
			
			// Bloqueando os campos da tela contra edição
			util_lock_fields(true);
			
			// Variáveis de controle da view
			boolean status, success = true;
			dir_proc = fil_proc = 0;
			
			// Atualizando a view
			SwingUtilities.invokeLater(() -> labelLoading.setVisible(true));
			
			// Se o diretório de origem for legível...
			if (dir_origem.canRead()) {
				
				// ...vou varrendo-o...
				for (File sub_dir: dir_origem.listFiles()) {
					
					// ...em busca de subdiretórios acessíveis...
					if (sub_dir.isDirectory() && sub_dir.canRead()) {
						
						// ...e, processando estes subdiretórios
						dir_proc++;
						status = core_parse_dir(sub_dir);
							
						// Caso dê alguma falha no processamento do subdiretório, sinalizo nesta variável
						if (!status)
							success = false;
							
					}
				}
			}
			
			// Atualizando a view
			String message = (success) ? "Tudo completo!" : "Terminado, mas com algumas falhas (vide console).";
			SwingUtilities.invokeLater(() -> {
												labelLoading.setVisible(false);
												textOBS.setText(message);
												util_lock_fields(false);
											 });
			
		}
		
		public boolean core_parse_dir(File dir_planilhas) {

			/******************** Leitura das Planilhas *************************/
			// Preparando recursos
			ArrayList<Recurso> listaRecursos = new ArrayList<Recurso>();
			
			// Varrendo o diretório...
			for (File arquivo: dir_planilhas.listFiles()) {
				
				// ...em busca de arquivos XLSX legíveis e...
				if (arquivo.isFile() && arquivo.getName().endsWith(".xlsx")) {
					
					// ...tentando ler as planilhas (sempre atualizando a view)
					try {
						
						fil_proc++;
						util_obs_reading(dir_planilhas,arquivo);
						
						ExcelReader.read(arquivo,this.colunas,listaRecursos);
						
					}
					
					// Caso dê algum erro, informo apenas no console e sigo o baile
					catch (Exception exception) {
						System.err.println("x Falha ao ler a planilha '" + arquivo.getName() + "': " + exception.getMessage());
						exception.printStackTrace();
						return false;
					}
					
				}
				
			}
			
			/********************* Ordenação das Listas *************************/
			
			// Atualizando a GUI
			SwingUtilities.invokeLater(() -> textOBS.setText("Ordenando recursos"));
			
			ArrayList<Recurso> listaOrdenada = ListSorter.sort(listaRecursos);
			
			
			/*********************** Verificação de Dados ***********************/
			//ListParser.parseIntervalo(listaOrdenada);
			ListParser.parseDouble   (listaOrdenada);
			
			/************************* Geração de PDF ***************************/
			
			// Obtendo o nome do arquivo PDF
			File pdf = util_pdf_filename(listaOrdenada);
			
			// Atualizando a GUI
			SwingUtilities.invokeLater(() -> textOBS.setText("Gerando PDF '" + pdf.getName() + "'"));
			
			// Exportando o PDF
			try {
				PDFWriter.export(this.edital, this.data, concursoAtual, listaOrdenada, pdf);
			} catch (Exception exception) {
				System.err.println("x Falha ao gerar o PDF '" + pdf.getName() + "': " + exception.getMessage());
				exception.printStackTrace();
				return false;
			}
			
			return true;
		}
		
		/** Formatando string de log (view) */
		private void util_obs_reading(File dir, File sheet) {
			
			String message = String.format("<html>Processando pasta %d de %d (%s)...<br>Lendo planilha %d de %d (%s)</html>", dir_proc, dir_tot, dir.getName(), fil_proc, fil_tot, sheet.getName());
			SwingUtilities.invokeLater(() -> textOBS.setText(message));
			
		}
		
		/** Bloqueia ou desbloqueia alguns campos da tela contra edição */
		private void util_lock_fields(boolean lock) {
			
			final boolean editable = !lock;
			
			SwingUtilities.invokeLater(() -> {
				
				textEdital.setEditable(editable);
				
				buttonOrigem.setEnabled(editable);
				buttonDestino.setEnabled(editable);
				
				buttonLimpar.setEnabled(editable);
				buttonProcessar.setEnabled(editable);
				
			});
			
		}
		
		/** Recupera o nome do arquivo PDF de saída */
		private File util_pdf_filename(ArrayList<Recurso> listaRecursos) {
			
			// No PSC, os documentos são gerados por disciplina, logo, o nome do arquivo é uma disciplina
			if (concursoAtual == TipoConcurso.PSC) {
				
				String disciplina = listaRecursos.get(0).getDisciplina();
				disciplina = disciplina.substring(0,disciplina.indexOf("(")-1);
				
				String filename = String.format("%s/PSC (%s).pdf", dir_destino.getAbsolutePath(),disciplina);
				
				return new File(filename);
				
			}
			
			else if (concursoAtual == TipoConcurso.EAD) {
				
				String filename = String.format("%s/Recursos EAD.pdf", dir_destino.getAbsolutePath());
				
				return new File(filename);
				
			}
			
			// No sistema está implementado apenas o PSC e PSTEC, logo, esse bloco é para o PSTEC
			else {
				
				// Instanciando meu builder
				StringBuilder builder = new StringBuilder();
				
				// Aqui extraio os cargos diferentes da lista de recursos
				Map<String,List<Recurso>> map_cargos = listaRecursos.stream().collect(Collectors.groupingBy(Recurso::getCargo));
				
				// Nesta etapa faco a ordenacao do Map por ordem crescente de código de cargos
				Map<String,List<Recurso>> map_cargos_sorted = map_cargos.entrySet().stream().sorted(Map.Entry.comparingByKey()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,(e1, e2) -> e2, LinkedHashMap::new));
						
				// Aqui adiciono todos os códigos de cargos diferentes no builder
				map_cargos_sorted.forEach((key,value) -> builder.append(key.substring(0,4) + " - "));
				
				// Por fim, monto o nome do arquivo com os códigos e nome de cargo + tratamentos de caracteres especiais em nome de arquivo
				String filename = String.format("%s%s (Todos).pdf",builder.toString(),listaRecursos.get(0).getCargo().substring(7).replace("/"," - ").replace(":"," "));

				return new File(dir_destino.getAbsolutePath() + "/" + filename);
				
			}
			
		}
		
	}
	
	private class MyTableModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;
		
		public MyTableModel(String[] colunas) {
			super(null,colunas);
		}
		
		@Override
		public boolean isCellEditable(int row, int column) {
			return (column == 2);
		}

	}
}
