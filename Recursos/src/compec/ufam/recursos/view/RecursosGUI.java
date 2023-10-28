package compec.ufam.recursos.view;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import com.phill.libs.mfvapi.MandatoryFieldsLogger;
import com.phill.libs.mfvapi.MandatoryFieldsManager;
import com.phill.libs.table.JTableMouseListener;

import compec.ufam.recursos.ListParser;
import compec.ufam.recursos.ListSorter;
import compec.ufam.recursos.io.DirectoryParser;
import compec.ufam.recursos.io.ExcelReader;
import compec.ufam.recursos.io.PDFWriter;
import compec.ufam.recursos.model.Constants;
import compec.ufam.recursos.model.Fields;
import compec.ufam.recursos.model.Recurso;
import compec.ufam.recursos.model.Recurso2;
import compec.ufam.recursos.model.TipoConcurso;
import compec.ufam.recursos.util.LGoodDatePickerUtils;

/** Implementa a interface gráfica do sistema.
 *  @author Felipe André - felipeandre.eng@gmail.com
 *  @version 3.0, 27/OUT/2023 */
public class RecursosGUI extends JFrame {

	// Serial
	private static final long serialVersionUID = 6968825769983359575L;

	// Declaração de atributos gráficos
	private final JTextField textEdital, textDestino, textOrigem;
	private final DatePicker datePicker;
	private final JTable tablePlanilha;
    private final RecursoTableModel modelPlanilha;
    private final JLabel labelInfo;
    private final JTextArea textConsole;
	private final JButton buttonEditalLimpa, buttonOrigem, buttonDestino, buttonParse, buttonExport;
	
	// Declaração de atributos dinâmicos
	private File sourceDir, targetDir;
	
	// MFV API
	private final MandatoryFieldsManager fieldValidator;
	private final MandatoryFieldsLogger  fieldLogger;
	
	private static final int excelColumnID = 2;
	
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
		Icon parseIcon  = ResourceManager.getIcon("icon/cog.png"      , 20, 20);
		Icon loading = new ImageIcon(ResourceManager.getResource("icon/loading.gif"));
		
		// Recuperando fontes e cores
		Font  fonte  = instance.getFont ();
		Font  ubuntu = instance.getUbuntuFont();
		Color color  = instance.getColor();
		
		// Painel 'Concurso'
		JPanel panelConcurso = new JPanel();
		panelConcurso.setBorder(instance.getTitledBorder("Concurso"));
		panelConcurso.setBounds(10, 10, 780, 70);
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
		textEdital.setBounds(65, 30, 415, 25);
		panelConcurso.add(textEdital);
		
		buttonEditalLimpa = new JButton(clearIcon);
		buttonEditalLimpa.setToolTipText(bundle.getString("hint-button-edlimpa"));
		buttonEditalLimpa.addActionListener((event) -> { textEdital.setText(null); textEdital.requestFocus(); } );
		buttonEditalLimpa.setBounds(490, 30, 30, 25);
		panelConcurso.add(buttonEditalLimpa);
		
		JLabel labelData = new JLabel("Publicação:");
		labelData.setHorizontalAlignment(JLabel.RIGHT);
		labelData.setFont(fonte);
		labelData.setBounds(530, 30, 85, 20);
		panelConcurso.add(labelData);
		
		datePicker = LGoodDatePickerUtils.getDatePicker();
		datePicker.getComponentDateTextField().setToolTipText(bundle.getString("hint-datepicker"));
		datePicker.getComponentDateTextField().setHorizontalAlignment(JTextField.CENTER);
		datePicker.setBounds(620, 27, 145, 30);
		panelConcurso.add(datePicker);
		
		// Painel 'Planilhas de Entrada'
		JPanel panelPlanilha = new JPanel();
		panelPlanilha.setBorder(instance.getTitledBorder("Planilhas de Entrada"));
		panelPlanilha.setBounds(10, 80, 780, 255);
		panelPlanilha.setLayout(null);
		getContentPane().add(panelPlanilha);
		
		JScrollPane scrollPlanilha = new JScrollPane();
		scrollPlanilha.setBounds(10, 25, 760, 221);
		panelPlanilha.add(scrollPlanilha);
		
		modelPlanilha = new RecursoTableModel();
		
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
		
		buttonParse = new JButton(parseIcon);
		buttonParse.setToolTipText(bundle.getString("hint-button-parse"));
		buttonParse.addActionListener((event) -> actionParse());
		buttonParse.setBounds(720, 683, 30, 25);
		getContentPane().add(buttonParse);
		
		buttonExport = new JButton(reportIcon);
		buttonExport.setToolTipText(bundle.getString("hint-button-export"));
		buttonExport.addActionListener((event) -> action_proccess());
		buttonExport.setBounds(760, 683, 30, 25);
		getContentPane().add(buttonExport);
		
		utilLoadProperty();
		
		// Cadastrando validação de campos
		this.fieldValidator = new MandatoryFieldsManager();
		this.fieldLogger    = new MandatoryFieldsLogger ();
		
		fieldValidator.addPermanent(labelOrigem , () -> sourceDir != null, bundle.getString("rui-mfv-sourcedir"), false);
		fieldValidator.addPermanent(new JLabel(), () -> validateColumns(), bundle.getString("rui-mfv-columnsOk"), false);
		
		setSize(800, 720);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		setVisible(true);
		
	}
	
	/******************** Bloco de Tratamento de Eventos de Botões *************************/
	
	private Map<File, List<Recurso2>> mapaRecursos;
	
	/** Carrega e analisa todas as planilhas do diretório informado. */
	private void actionParse() {
		
		// Realizando validação dos campos antes de prosseguir
		fieldValidator.validate(fieldLogger);
		
		if (fieldLogger.hasErrors()) {
			
			final String errors = bundle.getFormattedString("rui-parse-errors", fieldLogger.getErrorString());
					
			AlertDialog.error(this, getTitle(), errors);
			fieldLogger.clear(); return;
									
		}
		
		// Iniciando o processo de análise das planilhas
		Thread parser = new Thread(() -> threadParser());
		parser.setName("Thread analista de planilhas");
		parser.start();
		
	}
	
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
	
	/************************* Bloco de Métodos Utilitários *******************************/
	
	/** Recupera as colunas configuradas na tabela para um array de strings.
	 *  @return Array contendo as colunas configuradas na tabela. */
	private String[] utilGetColumnsFromTable() {
		
		final String[] columns = new String[Constants.fieldCount];
		
		for (int row=0; row<Constants.fieldCount; row++) {
			
			Object data = modelPlanilha.getValueAt(row, excelColumnID);
			
			columns[row] = (data == null || data.toString().isBlank()) ? null : data.toString();
			
		}
		
		return columns;
		
	}
	
	/** Carrega a configuração de colunas na tabela, a partir do arquivo de propriedades do sistema. */
	private void utilLoadProperty() {
		
		try {
			
			// Carrega a configuração do arquivo de propriedades
			final String[] columns = PropertiesManager.getStringArray(Constants.excelColumnsProperty, null);
			
			// Atualiza a tabela
			for (int row=0; row<columns.length; row++)
				modelPlanilha.setValueAt(columns[row], row, excelColumnID, false);
			
		} catch (Exception exception) {
			
			exception.printStackTrace();
			AlertDialog.error(this, getTitle(), bundle.getString("rui-load-prop-error"));
			
		}
		
	}
	
	/** Ativa ou desativa os campos de entrada de dados necessários para a análise das planilhas.
	 *  @param lock - estado da ativação dos campos */
	private void utilLockParseUI(final boolean lock) {
		
		final boolean enabled = !lock;
		
		SwingUtilities.invokeLater(() -> {
			
			labelInfo.setVisible(lock);
			tablePlanilha.setEnabled(enabled);
			buttonOrigem.setEnabled (enabled);
			
		});
		
	}
	
	/** Salva a configuração de colunas na tabela no arquivo de propriedades do sistema. */
	private void utilSaveProperty() {

		try {
		
			PropertiesManager.setStringArray(Constants.excelColumnsProperty,
					                         utilGetColumnsFromTable(),
					                         null
					                        );
			
		}
		catch (Exception exception) {
			
			exception.printStackTrace();
			AlertDialog.error(this, getTitle(), bundle.getString("rui-save-prop-error"));
			
		}
		
	}
	
	/************************* Bloco de Validadores de Dados *******************************/
	
	/** Verifica se todos os campos obrigatórios da tabela foram preenchidos.
	 *  O único que pode ser nulo é o cargo!
	 *  @return 'true' se, e somente se, todas as identificações obrigatórias de coluna foram devidamente preenchidas */
	private boolean validateColumns() {
		
		final String[] columns = utilGetColumnsFromTable();

		for (int i=0; i<Constants.fieldCount; i++)
			
			if (i != 4 && columns[i] == null)
				return false;
		
		return true;
		
	}
	
	/*************************** Bloco de Métodos em Thread ********************************/
	
	/** Analisa o diretório de origem de planilhas. */
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

	/** Analisa todos as planilhas contidas na pasta informada. */
	private void threadParser() {
		
		try {
			
			utilLockParseUI(true);
			this.mapaRecursos = DirectoryParser.parse(sourceDir, utilGetColumnsFromTable(), this);
			
		} catch (Exception exception) {
			
			exception.printStackTrace();
			AlertDialog.error(this, getTitle(), bundle.getString("rui-thread-parser-error"));
			
		}
		finally {
			
			utilLockParseUI(false);
			
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
	

	

	
	
	
	public synchronized void log(final String format, final Object... args) {
		
		SwingUtilities.invokeLater(() -> textConsole.append(String.format("[INFO] " + format + "\n", args)));
		
	}
	
	public synchronized void warning(final String format, final Object... args) {
		
		SwingUtilities.invokeLater(() -> textConsole.append(String.format("[ATENÇÃO] " + format + "\n", args)));
		
	}
	
	public synchronized void error(final String format, final Object... args) {
		
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
						
						//ExcelReader.read(arquivo,this.colunas,listaRecursos);
						
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
				
				buttonExport.setEnabled(editable);
				
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
	
	/** Implementa o modelo de dados da tabela de configuração de colunas das planilhas de entrada.
	 *  @author Felipe André - felipeandre.eng@gmail.com
	 *  @version 3.0, 28/OUT/2023 */
	private class RecursoTableModel extends DefaultTableModel {

		private static final long serialVersionUID = 8528128752920533163L;

		private static final String[]  colunas = { "#","Item","Coluna" };
		private static final Object[][] linhas = { Fields.TIMESTAMP.getRowData(),
												   Fields.NOME.getRowData(),
												   Fields.CPF.getRowData(),
												   Fields.INSCRICAO.getRowData(),
												   Fields.CARGO.getRowData(),
												   Fields.DISCIPLINA.getRowData(),
												   Fields.QUESTAO.getRowData(),
												   Fields.QUESTIONAMENTO.getRowData(),
												   Fields.ANEXOS.getRowData(),
												   Fields.RECURSO.getRowData(),
												   Fields.PARECER.getRowData(),
												   Fields.DECISAO.getRowData(),
												 };
		
		/** Construtor inicializando o modelo com as colunas e linhas pré-definidas. */
		public RecursoTableModel() {
			super(linhas, colunas);
			
			//Object[] a = Stream.of(Fields.values()).map(Fields::getRowData).toArray();
			
		}

		/** Define apenas a coluna 'Coluna' como editável. */
		@Override
		public boolean isCellEditable(int row, int column) {
			return (column == 2);
		}

		/** Atualiza a tabela e salva a configuração de colunas no arquivo de propriedades.
		 *  @param aValue - valor a ser aplicado
		 *  @param row - número da linha (começa em 0)
		 *  @param column - número da coluna (começa em 0) */
		@Override
		public void setValueAt(Object aValue, int row, int column) {
			super.setValueAt(aValue, row, column);
			utilSaveProperty();
		}
		
		/** Este método apenas atualiza a tabela, ignorando o salvamento no arquivo de propriedades.
		 *  @param aValue - valor a ser aplicado
		 *  @param row - número da linha (começa em 0)
		 *  @param column - número da coluna (começa em 0)
		 *  @param update - parâmetro não é utilizado na função, existe apenas para diferenciar do método {@link RecursoTableModel#setValueAt(Object, int, int)} */
		public void setValueAt(Object aValue, int row, int column, boolean update) {
			super.setValueAt(aValue, row, column);
		}
		
	}
	
}