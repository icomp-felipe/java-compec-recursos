package compec.ufam.recursos.view;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/** Implementa a interface gráfica do sistema.
 *  @author Felipe André - felipeandre.eng@gmail.com
 *  @version 3.0, 27/OUT/2023 */
public class RecursosGUI extends JFrame {

	// Serial
	private static final long serialVersionUID = 6968825769983359575L;

	// Declaração de atributos gráficos
	private final JTextField textEdital, textDestino, textOrigem;
	private final DatePicker datePicker;
	private final JComboBox<String> comboTipo;
	private final JTable tablePlanilha;
    private final DefaultTableModel modelPlanilha;
    private final JLabel labelInfo;
    private final JTextArea textConsole;
	private final JButton buttonEditalLimpa, buttonOrigem, buttonDestino, buttonProcessar;
	
	// Declaração de atributos dinâmicos
	private File sourceDir, targetDir;
	
	
	
	private int dir_proc, fil_proc;
	
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
		Icon searchIcon = ResourceManager.getIcon("icon/search.png"   , 20, 20);
		Icon clearIcon  = ResourceManager.getIcon("icon/trash.png"    , 20, 20);
		Icon reportIcon = ResourceManager.getIcon("icon/doc_empty.png", 20, 20);
		Icon saveIcon   = ResourceManager.getIcon("icon/save.png"     , 20, 20);
		Icon loading = new ImageIcon(ResourceManager.getResource("icon/loading.gif"));
		
		// Recuperando fontes e cores
		Font  fonte  = instance.getFont ();
		Font  ubuntu = instance.getUbuntuFont();
		Color color  = instance.getColor();
		
		// Painel 'Concurso'
		JPanel panelConcurso = new JPanel();
		panelConcurso.setBorder(instance.getTitledBorder("Concurso"));
		panelConcurso.setBounds(10, 10, 780, 105);
		panelConcurso.setLayout(null);
		getContentPane().add(panelConcurso);
		
		JLabel labelEdital = new JLabel("Edital:");
		labelEdital.setHorizontalAlignment(JLabel.RIGHT);
		labelEdital.setFont(fonte);
		labelEdital.setBounds(10, 30, 50, 20);
		panelConcurso.add(labelEdital);
		
		textEdital = new JTextField();
		textEdital.setToolTipText(bundle.getString("hint-text-edital"));
		textEdital.setFont(fonte);
		textEdital.setForeground(color);
		textEdital.setBounds(65, 30, 660, 25);
		panelConcurso.add(textEdital);
		
		buttonEditalLimpa = new JButton(clearIcon);
		buttonEditalLimpa.setToolTipText(bundle.getString("hint-button-edlimpa"));
		buttonEditalLimpa.addActionListener((event) -> { textEdital.setText(null); textEdital.requestFocus(); } );
		buttonEditalLimpa.setBounds(735, 30, 30, 25);
		panelConcurso.add(buttonEditalLimpa);
		
		JLabel labelTipo = new JLabel("Tipo:");
		labelTipo.setHorizontalAlignment(JLabel.RIGHT);
		labelTipo.setFont(fonte);
		labelTipo.setBounds(10, 65, 50, 20);
		panelConcurso.add(labelTipo);
		
		comboTipo = new JComboBox<String>();
		comboTipo.addActionListener((event) -> action_update_table());
		comboTipo.setToolTipText(bundle.getString("hint-combo-tipo"));
		comboTipo.setForeground(color);
		comboTipo.setFont(fonte);
		comboTipo.setBounds(65, 65, 125, 24);
		panelConcurso.add(comboTipo);
		
		JLabel labelData = new JLabel("Data de Publicação:");
		labelData.setHorizontalAlignment(JLabel.RIGHT);
		labelData.setFont(fonte);
		labelData.setBounds(205, 68, 140, 20);
		panelConcurso.add(labelData);
		
		datePicker = LGoodDatePickerUtils.getDatePicker();
		datePicker.getComponentDateTextField().setToolTipText(bundle.getString("hint-datepicker"));
		datePicker.getComponentDateTextField().setHorizontalAlignment(JTextField.CENTER);
		datePicker.setBounds(350, 65, 145, 30);
		panelConcurso.add(datePicker);
		
		// Painel 'Planilhas de Entrada'
		JPanel panelPlanilha = new JPanel();
		panelPlanilha.setBorder(instance.getTitledBorder("Planilhas de Entrada"));
		panelPlanilha.setBounds(10, 115, 780, 220);
		panelPlanilha.setLayout(null);
		getContentPane().add(panelPlanilha);
		
		JScrollPane scrollPlanilha = new JScrollPane();
		scrollPlanilha.setBounds(10, 25, 760, 186);
		panelPlanilha.add(scrollPlanilha);
		
		modelPlanilha = new MyTableModel(new String [] {"#","Item","Coluna"});
		
		tablePlanilha = new JTable(modelPlanilha);
		tablePlanilha.setRowHeight(20);
		tablePlanilha.setFont(ubuntu);
		tablePlanilha.getTableHeader().setFont(fonte);
		tablePlanilha.addMouseListener(new JTableMouseListener(tablePlanilha));
		tablePlanilha.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPlanilha.setViewportView(tablePlanilha);
		
		final DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		
		TableColumnModel columnModel = tablePlanilha.getColumnModel();
		
		columnModel.getColumn(0).setCellRenderer(centerRenderer);
		columnModel.getColumn(2).setCellRenderer(centerRenderer);
		
		columnModel.getColumn(0).setPreferredWidth( 15);
		columnModel.getColumn(1).setPreferredWidth(597);
		columnModel.getColumn(2).setPreferredWidth( 36);
		
		// Painel 'Pastas'		
		JPanel panelPastas = new JPanel();
		panelPastas.setBorder(instance.getTitledBorder("Pastas"));
		panelPastas.setBounds(10, 335, 780, 105);
		panelPastas.setLayout(null);
		getContentPane().add(panelPastas);
		
		JLabel labelOrigem = new JLabel("Origem (Planilhas):");
		labelOrigem.setHorizontalAlignment(JLabel.RIGHT);
		labelOrigem.setFont(fonte);
		labelOrigem.setBounds(10, 30, 140, 20);
		panelPastas.add(labelOrigem);
		
		textOrigem = new JTextField();
		textOrigem.setFont(fonte);
		textOrigem.setForeground(color);
		textOrigem.setEditable(false);
		textOrigem.setToolTipText(bundle.getString("hint-text-origem"));
		textOrigem.setBounds(155, 30, 570, 25);
		panelPastas.add(textOrigem);
		
		buttonOrigem = new JButton(searchIcon);
		buttonOrigem.setToolTipText(bundle.getString("hint-button-origem"));
		buttonOrigem.addActionListener((event) -> actionSelectOrigem());
		buttonOrigem.setBounds(735, 30, 30, 25);
		panelPastas.add(buttonOrigem);
		
		JLabel labelDestino = new JLabel("Destino (PDF):");
		labelDestino.setHorizontalAlignment(JLabel.RIGHT);
		labelDestino.setFont(fonte);
		labelDestino.setBounds(10, 65, 140, 20);
		panelPastas.add(labelDestino);
		
		textDestino = new JTextField();
		textDestino.setForeground(color);
		textDestino.setFont(fonte);
		textDestino.setEditable(false);
		textDestino.setToolTipText(bundle.getString("hint-text-destino"));
		textDestino.setBounds(155, 65, 570, 25);
		panelPastas.add(textDestino);
		
		buttonDestino = new JButton(searchIcon);
		buttonDestino.setToolTipText(bundle.getString("hint-button-destino"));
		buttonDestino.addActionListener((event) -> actionSelectDestino());
		buttonDestino.setBounds(735, 65, 30, 25);
		panelPastas.add(buttonDestino);
		
		// Painel 'Console'
		JPanel panelConsole = new JPanel();
		panelConsole.setBorder(instance.getTitledBorder("Console"));
		panelConsole.setBounds(10, 440, 780, 231);
		panelConsole.setLayout(null);
		getContentPane().add(panelConsole);
		
		JScrollPane scrollConsole = new JScrollPane();
		scrollConsole.setBounds(10, 25, 760, 194);
		panelConsole.add(scrollConsole);
		
		textConsole = new JTextArea();
		textConsole.setFont(fonte);
		textConsole.setEditable(false);
		textConsole.setToolTipText(bundle.getString("hint-text-console"));
		scrollConsole.setViewportView(textConsole);
		
		// Fundo da janela
		labelInfo = new JLabel(loading);
		labelInfo.setFont(fonte);
		labelInfo.setVisible(false);
		labelInfo.setText("Em processamento...");
		labelInfo.setBounds(10, 685, 170, 20);
		getContentPane().add(labelInfo);
		
		buttonProcessar = new JButton(reportIcon);
		buttonProcessar.setToolTipText("Gerar os recursos em PDF");
		buttonProcessar.addActionListener((event) -> action_proccess());
		buttonProcessar.setBounds(760, 683, 30, 25);
		getContentPane().add(buttonProcessar);
		
		
		init_load_combo();
		
		setSize(800, 720);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		setVisible(true);
		
	}
	
	/******************** Bloco de Tratamento de Eventos de Botões *************************/
	
	/** Seleciona o diretório de destino das respostas aos recursos. */
	private void actionSelectDestino() {
		
		File destino = PhillFileUtils.loadDir(this, "Recursys v.3.0 - Selecione a pasta de destino (PDF)", PhillFileUtils.SAVE_DIALOG, null);
		
		if (destino != null) {
			
			textDestino.setText(destino.getAbsolutePath()); this.targetDir = destino;
			
			if (!destino.canWrite())
				warning("Não é possível criar arquivos na pasta de destino '%s'", destino.getAbsolutePath());
			
		}
		
	}
	
	/** Seleciona o diretório de origem e busca por planilhas em seus subdiretórios. */
	private void actionSelectOrigem() {
		
		File origem = PhillFileUtils.loadDir(this, "Recursys v.3.0 - Selecione a pasta de origem (planilhas)", PhillFileUtils.OPEN_DIALOG, null);
		
		if (origem != null) {
			
			textOrigem.setText(origem.getAbsolutePath()); this.sourceDir = origem;
			
			Thread explorer = new Thread(() -> threadExplorer());
			explorer.setName("Thread exploradora de diretório de origem");
			explorer.start();
			
		}
		
	}
	
	/*************************** Bloco de Métodos em Thread ********************************/
	
	/** Analisa o diretório de origem de planilhas */
	private void threadExplorer() {
		
		try {
			
			labelInfo.setVisible(true);
			
			// Contando quantas planilhas existem no diretório (e subs)
			final int planilhas = (int) Files.walk(sourceDir.toPath())
					                         .parallel()
					                         .filter(p -> p.toFile().isFile() && p.toFile().getName().endsWith("xlsx") )
					                         .count();
			
			// Atualizando a UI
			switch (planilhas) {
			
				case 0:
					log("Nenhuma planilha encontrada em '%s'", sourceDir.getAbsolutePath());
					break;
					
				case 1:
					log("Encontrada 1 planilha em '%s'", sourceDir.getAbsolutePath());
					break;
				
				default:
					log("Encontradas %d planilhas em '%s'", planilhas, sourceDir.getAbsolutePath());
					break;
			
			}
			
			
		}
		catch (Exception exception) {
			
			error("Falha ao varrer os arquivos em '%s'. Verifique o console para mais informações.", sourceDir.getAbsolutePath());
			exception.printStackTrace();
			
		}
		finally {
			
			labelInfo.setVisible(false);
			
		}
		
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
		final int rows = modelPlanilha.getRowCount();
		
		try {
		
			final String[] columns = new String[rows];
			
			// Varre todas as linhas da tabela recuperando as colunas configuradas
			for (int i=0; i<rows; i++)
				columns[i] = modelPlanilha.getValueAt(i,2).toString();
			
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
	

	
	/** Atualiza a tabela com as configurações do arquivo de propriedades */
	private void action_update_table() {

		// Recuperando o concurso selecionado
		this.concursoAtual = TipoConcurso.valueOf(comboTipo.getSelectedItem().toString());
		
		try {
			
			// Recuperando as colunas e dados
			String[] columnNames = this.concursoAtual.getColumnNames();
			String[] columns     = PropertiesManager.getStringArray(this.concursoAtual.getColumns(),null);
			
			// Simples tratamento de erros
			if (columnNames.length != columns.length) {
				
				AlertDialog.error(this, "Configuração de Planilhas","Configuração inválida, favor conferir arquivo de propriedades");
				return;
				
			}
			
			// Limpa os dados da tabela
			TableUtils.clear(modelPlanilha);
			
			// Preenche a tabela com os dados carregados do arquivo de propriedades
			for (int i=0; i<columns.length; i++)
				modelPlanilha.addRow(new Object[]{ i+1, columnNames[i], columns[i] });		// | # | Item | Coluna |
			
		}
		catch (Exception exception) {
			exception.printStackTrace();
		}
		finally {
			
			// Atualizando a quantidade de itens carregados
			
		}
		
	}
	
	/** Inicializa o combo de tipos de concurso */
	private void init_load_combo() {
		
		for (TipoConcurso concurso: TipoConcurso.values())
			comboTipo.addItem(concurso.name());
		
	}

	
	private void log(final String format, final Object... args) {
		
		SwingUtilities.invokeLater(() -> textConsole.append(String.format("[INFO] " + format + "\n", args)));
		
	}
	
	private void warning(final String format, final Object... args) {
		
		SwingUtilities.invokeLater(() -> textConsole.append(String.format("[ATENÇÃO] " + format + "\n", args)));
		
	}
	
	private void error(final String format, final Object... args) {
		
		SwingUtilities.invokeLater(() -> textConsole.append(String.format("[ERRO] " + format + "\n", args)));
		
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
		
		if (sourceDir == null) {
			AlertDialog.error(this, "Selecione a pasta de origem");
			return false;
		}
		
		if (targetDir == null) {
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
			//SwingUtilities.invokeLater(() -> labelLoading.setVisible(true));
			
			// Se o diretório de origem for legível...
			if (sourceDir.canRead()) {
				
				// ...vou varrendo-o...
				for (File sub_dir: sourceDir.listFiles()) {
					
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
												//labelLoading.setVisible(false);
												//textOBS.setText(message);
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
			//SwingUtilities.invokeLater(() -> textOBS.setText("Ordenando recursos"));
			
			ArrayList<Recurso> listaOrdenada = ListSorter.sort(listaRecursos);
			
			
			/*********************** Verificação de Dados ***********************/
			//ListParser.parseIntervalo(listaOrdenada);
			ListParser.parseDouble   (listaOrdenada);
			
			/************************* Geração de PDF ***************************/
			
			// Obtendo o nome do arquivo PDF
			File pdf = util_pdf_filename(listaOrdenada);
			
			// Atualizando a GUI
			//SwingUtilities.invokeLater(() -> textOBS.setText("Gerando PDF '" + pdf.getName() + "'"));
			
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
			
			//String message = String.format("<html>Processando pasta %d de %d (%s)...<br>Lendo planilha %d de %d (%s)</html>", dir_proc, dir_tot, dir.getName(), fil_proc, fil_tot, sheet.getName());
			//SwingUtilities.invokeLater(() -> textOBS.setText(message));
			
		}
		
		/** Bloqueia ou desbloqueia alguns campos da tela contra edição */
		private void util_lock_fields(boolean lock) {
			
			final boolean editable = !lock;
			
			SwingUtilities.invokeLater(() -> {
				
				textEdital.setEditable(editable);
				
				buttonOrigem.setEnabled(editable);
				buttonDestino.setEnabled(editable);
				
				buttonProcessar.setEnabled(editable);
				
			});
			
		}
		
		/** Recupera o nome do arquivo PDF de saída */
		private File util_pdf_filename(ArrayList<Recurso> listaRecursos) {
			
			
				
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

				return new File(targetDir.getAbsolutePath() + "/" + filename);
				
			
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

		@Override
		public void setValueAt(Object aValue, int row, int column) {
			super.setValueAt(aValue, row, column);
			action_salva_config();
		}
		
	}
}
