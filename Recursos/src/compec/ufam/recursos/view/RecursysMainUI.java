package compec.ufam.recursos.view;

import java.awt.*;
import java.awt.event.*;

import java.io.*;
import java.nio.file.*;

import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.table.*;

import com.phill.libs.*;
import com.phill.libs.ui.*;
import com.phill.libs.i18n.*;
import com.phill.libs.files.*;
import com.phill.libs.table.*;
import com.phill.libs.mfvapi.*;

import compec.ufam.recursos.pdf.*;
import compec.ufam.recursos.util.*;
import compec.ufam.recursos.model.*;
import compec.ufam.recursos.parser.*;

import com.github.lgooddatepicker.components.*;

/** Implementa a interface gráfica do sistema.
 *  @author Felipe André - felipeandre.eng@gmail.com
 *  @version 3.5, 24/FEV/2024 */
public class RecursysMainUI extends JFrame {

	// Serial
	private static final long serialVersionUID = 6968825769983359575L;

	// Declaração de atributos gráficos
	private final JTextField textEdital, textDestino, textOrigem;
	private final DatePicker pickerPublicacao, pickerRetificacao;
	private final JTable tablePlanilha;
    private final RecursoTableModel modelPlanilha;
    private final JLabel labelInfo;
    private final JTextPane textConsole;
	private final JButton buttonEditalLimpa, buttonOrigem, buttonDestino, buttonParse, buttonGabarito, buttonExport;
	private final StyledDocument styledDocument;
	private final Style greenFontStyle, orangeFontStyle, redFontStyle;
	
	// Declaração de atributos estáticos
	private static final int excelColumnID = 2;
	
	// Declaração de atributos dinâmicos
	private File sourceDir, targetDir;
	private Map<File, List<Recurso>> mapaRecursos;
	
	// MFV API
	private final MandatoryFieldsManager parserValidator, gabaritoValidator, respostasValidator;
	private final MandatoryFieldsLogger  parserLogger, gabaritoLogger, respostasLogger;
	
	// Carregando bundle de idiomas
	private final static PropertyBundle bundle = new PropertyBundle("i18n/portuguese", null);
	private JCheckBox checkHeader;
	
    /** Função principal instanciando a interface gráfica.
     *  @param args - argumentos do S.O. */
	public static void main(String[] args) { new RecursysMainUI(); }

	/** Construtor inicializando a interface gráfica. */
	public RecursysMainUI() {
		super("Recursys v.3.5");
		
		// Inicializando atributos gráficos
		GraphicsHelper instance = GraphicsHelper.getInstance();
		GraphicsHelper.setFrameIcon(this,"icon/recursys-icon.png");
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
		panelConcurso.setBounds(10, 10, 780, 100);
		panelConcurso.setLayout(null);
		getContentPane().add(panelConcurso);
		
		JLabel labelEdital = new JLabel("Cabeçalho:");
		labelEdital.setHorizontalAlignment(JLabel.RIGHT);
		labelEdital.setFont(fonte);
		labelEdital.setBounds(10, 30, 85, 20);
		panelConcurso.add(labelEdital);
		
		textEdital = new JTextField();
		textEdital.setToolTipText(bundle.getString("hint-text-edital"));
		textEdital.setFont(fonte);
		textEdital.setForeground(color);
		textEdital.setBounds(100, 30, 625, 25);
		panelConcurso.add(textEdital);
		
		buttonEditalLimpa = new JButton(clearIcon);
		buttonEditalLimpa.setToolTipText(bundle.getString("hint-button-edlimpa"));
		buttonEditalLimpa.addActionListener((_) -> { textEdital.setText(null); textEdital.requestFocus(); } );
		buttonEditalLimpa.setBounds(735, 30, 30, 25);
		panelConcurso.add(buttonEditalLimpa);
		
		JLabel labelPublicacao = new JLabel("Publicação:");
		labelPublicacao.setHorizontalAlignment(JLabel.RIGHT);
		labelPublicacao.setFont(fonte);
		labelPublicacao.setBounds(10, 63, 85, 20);
		panelConcurso.add(labelPublicacao);
		
		pickerPublicacao = LGoodDatePickerUtils.getDatePicker();
		pickerPublicacao.getComponentDateTextField().setToolTipText(bundle.getString("hint-date-publicacao"));
		pickerPublicacao.getComponentDateTextField().setHorizontalAlignment(JTextField.CENTER);
		pickerPublicacao.setBounds(100, 60, 145, 30);
		panelConcurso.add(pickerPublicacao);
		
		JLabel labelRetificacao = new JLabel("Retificação:");
		labelRetificacao.setHorizontalAlignment(JLabel.RIGHT);
		labelRetificacao.setFont(fonte);
		labelRetificacao.setBounds(255, 63, 85, 20);
		panelConcurso.add(labelRetificacao);
		
		pickerRetificacao = LGoodDatePickerUtils.getDatePicker();
		pickerRetificacao.getComponentDateTextField().setToolTipText(bundle.getString("hint-date-retificacao"));
		pickerRetificacao.getComponentDateTextField().setHorizontalAlignment(JTextField.CENTER);
		pickerRetificacao.setBounds(345, 60, 145, 30);
		panelConcurso.add(pickerRetificacao);
		
		// Painel 'Planilhas de Entrada'
		JPanel panelPlanilha = new JPanel();
		panelPlanilha.setBorder(instance.getTitledBorder("Planilhas de Entrada"));
		panelPlanilha.setBounds(10, 110, 780, 255);
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
		panelPastas.setBounds(10, 365, 780, 125);
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
		buttonOrigem.addActionListener((_) -> actionSelectOrigem());
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
		buttonDestino.addActionListener((_) -> actionSelectDestino());
		buttonDestino.setBounds(735, 65, 30, 25);
		panelPastas.add(buttonDestino);
		
		checkHeader = new JCheckBox("Ignorar verificação de cabeçalho");
		checkHeader.setToolTipText(bundle.getString("hint-check-header"));
		checkHeader.setHorizontalAlignment(JCheckBox.CENTER);
		checkHeader.setFont(fonte);
		checkHeader.setBounds(10, 95, 760, 20);
		panelPastas.add(checkHeader);
		
		// Painel 'Console'
		JPanel panelConsole = new JPanel();
		panelConsole.setBorder(instance.getTitledBorder("Console"));
		panelConsole.setBounds(10, 490, 780, 215);
		panelConsole.setLayout(null);
		getContentPane().add(panelConsole);
		
		JScrollPane scrollConsole = new JScrollPane();
		scrollConsole.setBounds(10, 25, 760, 180);
		panelConsole.add(scrollConsole);
		
		textConsole = new JTextPane();
		textConsole.setFont(fonte);
		textConsole.setEditable(false);
		textConsole.setToolTipText(bundle.getString("hint-text-console"));
		scrollConsole.setViewportView(textConsole);
		
		this.styledDocument = textConsole.getStyledDocument();
		
		this.orangeFontStyle = textConsole.addStyle("yellowFont", null);
		this.greenFontStyle  = textConsole.addStyle("blackFont" , null);
		this.redFontStyle    = textConsole.addStyle("redFont"   , null);

		StyleConstants.setForeground(redFontStyle   , Color.RED);
		StyleConstants.setForeground(orangeFontStyle, new Color(0xED921C));
		StyleConstants.setForeground(greenFontStyle , new Color(0x228F1D));
		
		// Fundo da janela
		labelInfo = new JLabel(loading);
		labelInfo.setHorizontalAlignment(JLabel.LEFT);
		labelInfo.setFont(fonte);
		labelInfo.setVisible(false);
		labelInfo.setBounds(10, 715, 650, 20);
		getContentPane().add(labelInfo);
		
		buttonParse = new JButton(parseIcon);
		buttonParse.setToolTipText(bundle.getString("hint-button-parse"));
		buttonParse.addActionListener((_) -> actionParse());
		buttonParse.setBounds(680, 713, 30, 25);
		getContentPane().add(buttonParse);
		
		buttonGabarito = new JButton(reportIcon);
		buttonGabarito.setToolTipText(bundle.getString("hint-button-gabarito"));
		buttonGabarito.addActionListener((_) -> actionGabarito());
		buttonGabarito.setBounds(720, 713, 30, 25);
		getContentPane().add(buttonGabarito);
		
		buttonExport = new JButton(reportIcon);
		buttonExport.setToolTipText(bundle.getString("hint-button-export"));
		buttonExport.addActionListener((_) -> actionRespostas());
		buttonExport.setBounds(760, 713, 30, 25);
		getContentPane().add(buttonExport);
		
		createPopupMenu();
		
		utilLoadProperty();
		
		// Cadastrando validação de campos
		this.parserValidator    = new MandatoryFieldsManager();
		this.gabaritoValidator  = new MandatoryFieldsManager();
		this.respostasValidator = new MandatoryFieldsManager();
		
		this.parserLogger    = new MandatoryFieldsLogger();
		this.gabaritoLogger  = new MandatoryFieldsLogger();
		this.respostasLogger = new MandatoryFieldsLogger();
		
		parserValidator.addPermanent(labelOrigem , () -> sourceDir != null, bundle.getString("rui-mfv-sourcedir"), false);
		parserValidator.addPermanent(new JLabel(), () -> validateColumns(), bundle.getString("rui-mfv-columnsOk"), false);
		
		gabaritoValidator.addPermanent(labelOrigem, () -> this.mapaRecursos != null      , bundle.getString("rui-mfv-mapa"  ), false);
		gabaritoValidator.addPermanent(labelEdital, () -> !textEdital.getText().isBlank(), bundle.getString("rui-mfv-edital"), false);
		
		respostasValidator.addPermanent(labelEdital , () -> !textEdital.getText().isBlank(), bundle.getString("rui-mfv-edital"   ), false);
		respostasValidator.addPermanent(labelPublicacao   , () -> pickerPublicacao.getDate() != null   , bundle.getString("rui-mfv-data"     ), false);
		respostasValidator.addPermanent(labelOrigem , () -> this.mapaRecursos != null      , bundle.getString("rui-mfv-mapa"     ), false);
		respostasValidator.addPermanent(labelDestino, () -> targetDir != null              , bundle.getString("rui-mfv-targetdir"), false);
		
		setSize(800, 780);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		setVisible(true);
		
	}
	
	private void createPopupMenu() {

		JPopupMenu popupMenu = new JPopupMenu();
		
		// Definindo aceleradores
		KeyStroke limpar = KeyStroke.getKeyStroke(KeyEvent.VK_L, 0);
		
		// Definindo ações dos itens de menu
		Action actionLimpar = new ShortcutAction("Limpar", KeyEvent.VK_L, limpar,(_) -> textConsole.setText(null));
		
		// Declarando os itens de menu
		JMenuItem itemLimpar = new JMenuItem(actionLimpar);
		popupMenu.add(itemLimpar);
		
		// Definindo atalhos de teclado
		final InputMap  imap = textConsole.getInputMap (JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		final ActionMap amap = textConsole.getActionMap();
					
		imap.put(limpar, "actionLimpar");
		
		amap.put("actionLimpar", actionLimpar);
		
		// Atribuindo menu à tabela
		textConsole.setComponentPopupMenu(popupMenu);
		
	}
	
	/******************** Bloco de Tratamento de Eventos de Botões *************************/
	
	/** Calcula e exibe um resumo dos gabaritos. */
	private void actionGabarito() {
		
		// Realizando validação dos campos antes de prosseguir
		gabaritoValidator.validate(gabaritoLogger);
				
		if (gabaritoLogger.hasErrors()) {
					
			final String errors = bundle.getFormattedString("rui-gabarito-errors", gabaritoLogger.getErrorString());
							
			AlertDialog.error(this, getTitle(), errors);
			gabaritoLogger.clear(); return;
									
		}
		
		// Iniciando o processo de geração de relatório
		Thread gabarito = new Thread(() -> threadGabarito());
		gabarito.setName("Thread de geração do relatório 'Resumo de Gabaritos'");
		gabarito.start();
			
	}
	
	/** Carrega e analisa todas as planilhas do diretório informado. */
	private void actionParse() {
		
		// Realizando validação dos campos antes de prosseguir
		parserValidator.validate(parserLogger);
		
		if (parserLogger.hasErrors()) {
			
			final String errors = bundle.getFormattedString("rui-parse-errors", parserLogger.getErrorString());
					
			AlertDialog.error(this, getTitle(), errors);
			parserLogger.clear(); return;
									
		}
		
		// Iniciando o processo de análise das planilhas
		Thread parser = new Thread(() -> threadParser());
		parser.setName("Thread analista de planilhas");
		parser.start();
		
	}
	
	/** Gera e exporta as respostas aos recursos no diretório de saída. */
	private void actionRespostas() {
	
		// Realizando validação dos campos antes de prosseguir
		respostasValidator.validate(respostasLogger);
		
		if (respostasLogger.hasErrors()) {
			
			final String errors = bundle.getFormattedString("rui-respostas-errors", respostasLogger.getErrorString());
					
			AlertDialog.error(this, getTitle(), errors);
			respostasLogger.clear(); return;
									
		}
		
		// Iniciando a exportação das respostas
		Thread respostas = new Thread(() -> threadRespostas());
		respostas.setName("Thread de exportação de respostas");
		respostas.start();
		
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
	
	public PropertyBundle getBundle() {
		return bundle;
	}
	
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
	
	/** Ativa ou desativa os campos de entrada de dados necessários para a geração do relatório 'Resumo de Gabaritos'.
	 *  @param lock - estado da ativação dos campos */
	private void utilLockGabaritoUI(final boolean lock) {
		
		final boolean enabled = !lock;
		
		SwingUtilities.invokeLater(() -> {
			
			labelInfo.setText("Gerando visualização...");
			labelInfo.setVisible(lock);

			textEdital       .setEditable(enabled);
			buttonEditalLimpa.setEnabled (enabled);
			
			buttonParse   .setEnabled(enabled);
			buttonGabarito.setEnabled(enabled);
			buttonExport  .setEnabled(enabled);
			
		});
		
	}
	
	/** Ativa ou desativa os campos de entrada de dados necessários para a análise das planilhas.
	 *  @param lock - estado da ativação dos campos */
	private void utilLockParseUI(final boolean lock) {
		
		final boolean enabled = !lock;
		
		SwingUtilities.invokeLater(() -> {
			
			labelInfo.setText("Analisando planilhas...");
			labelInfo.setVisible(lock);
			tablePlanilha.setEnabled(enabled);
			
			buttonOrigem.setEnabled (enabled);
			
			buttonParse   .setEnabled(enabled);
			buttonGabarito.setEnabled(enabled);
			buttonExport  .setEnabled(enabled);
			
		});
		
	}
	
	/** Ativa ou desativa os campos de entrada de dados necessários para a exportação das respostas aos recursos.
	 *  @param lock - estado da ativação dos campos */
	private void utilLockRecursoUI(final boolean lock) {
		
		final boolean enabled = !lock;
		
		SwingUtilities.invokeLater(() -> {
			
			labelInfo.setText("Exportando respostas aos recursos...");
			labelInfo.setVisible(lock);
			
			textEdital       .setEditable(enabled);
			buttonEditalLimpa.setEnabled (enabled);
			
			pickerPublicacao.setEnabled(enabled);
			
			buttonOrigem .setEnabled(enabled);
			buttonDestino.setEnabled(enabled);
			
			buttonParse   .setEnabled(enabled);
			buttonGabarito.setEnabled(enabled);
			buttonExport  .setEnabled(enabled);
			
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
	
	/** Constrói e exibe o relatório 'Resumo de Gabaritos'. */
	private void threadGabarito() {
		
		try {
			
			utilLockGabaritoUI(true);
			Gabarito.show(textEdital.getText(), mapaRecursos); utilLockGabaritoUI(false);
				
		}
		catch (Exception exception) {
				
			exception.printStackTrace(); utilLockGabaritoUI(false);
			AlertDialog.error(this, getTitle(), bundle.getString("rui-thread-gabarito-error"));
				
		}
		
	}

	/** Analisa todos as planilhas contidas na pasta informada. */
	private void threadParser() {
		
		try {
			
			utilLockParseUI(true);
			this.mapaRecursos = DirectoryParser.parse(sourceDir, utilGetColumnsFromTable(), this.checkHeader.isSelected(), this); utilLockParseUI(false);
			
		} catch (Exception exception) {
			
			exception.printStackTrace(); utilLockParseUI(false);
			AlertDialog.error(this, getTitle(), bundle.getString("rui-thread-parser-error"));
			
		}
		
	}
	
	/** Exporta as respostas aos recursos pra PDF. */
	private void threadRespostas() {
		
		try {
			
			utilLockRecursoUI(true);
			
			// Iterando por todas as entradas do mapa
			for (Map.Entry<File, List<Recurso>> entries: mapaRecursos.entrySet()) {
				
				// Recuperando os objetos do mapeamento atual
				File planilha = entries.getKey();
				List<Recurso> listaRecursos = entries.getValue();
			
				// Construindo o relatório e exportando pra PDF
				Resposta.exportPDF(textEdital.getText(), pickerPublicacao.getDate(), pickerRetificacao.getDate(), planilha, listaRecursos, targetDir);
				
			}
			
			utilLockRecursoUI(false);
			AlertDialog.info(this, getTitle(), bundle.getString("rui-thread-respostas-success"));
			
		} catch (Exception exception) {
			
			exception.printStackTrace(); utilLockRecursoUI(false);
			AlertDialog.error(this, getTitle(), bundle.getString("rui-thread-respostas-error"));
			
		}
		
	}
	
	/******************************** Bloco dos Loggers ************************************/
	
	/** Imprime uma mensagem comum no console.
	 *  @param format - formato do texto
	 *  @param args - argumentos referenciados no formato */
	public synchronized void log(final String format, final Object... args) {
		
		SwingUtilities.invokeLater(() -> {
			try { styledDocument.insertString(styledDocument.getLength(), String.format("[INFO] " + format + "\n", args), greenFontStyle); }
			catch (BadLocationException exception) { }
		});
		
	}
	
	/** Imprime uma mensagem de aviso no console.
	 *  @param format - formato do texto
	 *  @param args - argumentos referenciados no formato */
	public synchronized void warning(final String format, final Object... args) {
		
		SwingUtilities.invokeLater(() -> {
			try { styledDocument.insertString(styledDocument.getLength(), String.format("[ALERT] " + format + "\n", args), orangeFontStyle); }
			catch (BadLocationException exception) { }
		});
		
	}
	
	/** Imprime uma mensagem de erro no console.
	 *  @param format - formato do texto
	 *  @param args - argumentos referenciados no formato */
	public synchronized void error(final String format, final Object... args) {
		
		SwingUtilities.invokeLater(() -> {
			try { styledDocument.insertString(styledDocument.getLength(), String.format("[ERRO] " + format + "\n", args), redFontStyle); }
			catch (BadLocationException exception) { }
		});
		
	}
	
	/************************** Bloco de Classes Auxiliares ********************************/
	
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