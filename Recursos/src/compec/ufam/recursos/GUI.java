package compec.ufam.recursos;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.*;

import org.jdatepicker.impl.JDatePickerImpl;

import com.phill.libs.AlertDialog;
import com.phill.libs.FileChooserHelper;
import com.phill.libs.GraphicsHelper;
import com.phill.libs.ResourceManager;

public class GUI extends JFrame {

	private static final long serialVersionUID = 1L;
	private static final GraphicsHelper helper = GraphicsHelper.getInstance();
	
	private final JDatePickerImpl data_realizacao;
	private JTextField textEdital;
	private JLabel textDestino;
	private JLabel textOrigem;
	private JLabel textOBS;
	
	
	private int dir_proc, fil_proc;
	private int dir_tot, fil_tot;
	
	private File dir_origem, dir_destino;
	private JLabel labelLoading;
	private JButton buttonOrigem;
	private JButton buttonDestino;
	private JButton buttonSair;
	private JButton buttonLimpar;
	private JButton buttonProcessar;

	public static void main(String[] args) {
		new GUI();
	}

	public GUI() {
		super("Processador de Recursos");
		
		Font  fonte = helper.getFont ();
		Color color = helper.getColor();
		
		setSize(720,300);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		getContentPane().setLayout(null);
		
		JPanel painelID = new JPanel();
		painelID.setBorder(helper.getTitledBorder("Identificação do Concurso"));
		painelID.setBounds(12, 12, 696, 105);
		getContentPane().add(painelID);
		painelID.setLayout(null);
		
		JLabel labelEdital = new JLabel("Edital:");
		labelEdital.setFont(fonte);
		labelEdital.setBounds(12, 35, 60, 20);
		painelID.add(labelEdital);
		
		textEdital = new JTextField();
		textEdital.setFont(fonte);
		textEdital.setForeground(color);
		textEdital.setBounds(69, 33, 615, 25);
		painelID.add(textEdital);
		textEdital.setColumns(10);
		
		JLabel labelData = new JLabel("Data de Publicação dos Recursos:");
		labelData.setFont(fonte);
		labelData.setBounds(12, 70, 262, 20);
		painelID.add(labelData);
		
		data_realizacao = DatePicker.getDatePicker();
		data_realizacao.setLocation(270, 70);
		painelID.add(data_realizacao);
		
		JPanel painelPastas = new JPanel();
		painelPastas.setBorder(helper.getTitledBorder("Pastas"));
		painelPastas.setBounds(12, 120, 696, 105);
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
		
		Icon searchIcon = ResourceManager.getResizedIcon("icon/search-black.png",20,20);
		Icon clearIcon  = ResourceManager.getResizedIcon("icon/clear.png",20,20);
		Icon exitIcon   = ResourceManager.getResizedIcon("icon/exit-black.png",20,20);
		Icon reportIcon = ResourceManager.getResizedIcon("icon/report.png",20,20);
		
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
		textOBS.setBounds(44, 232, 528, 35);
		textOBS.setVisible(false);
		getContentPane().add(textOBS);
		
		labelLoading = new JLabel(loading,SwingConstants.LEFT);
		labelLoading.setBounds(12, 232, 20, 35);
		labelLoading.setVisible(false);
		getContentPane().add(labelLoading);
		
		buttonSair = new JButton(exitIcon);
		buttonSair.setToolTipText("Sair do programa");
		buttonSair.addActionListener((event) -> dispose());
		buttonSair.setBounds(590, 237, 30, 25);
		getContentPane().add(buttonSair);
		
		buttonLimpar = new JButton(clearIcon);
		buttonLimpar.setToolTipText("Limpar os dados da tela");
		buttonLimpar.addActionListener((event) -> action_clear());
		buttonLimpar.setBounds(630, 237, 30, 25);
		getContentPane().add(buttonLimpar);
		
		buttonProcessar = new JButton(reportIcon);
		buttonProcessar.setToolTipText("Gerar os recursos em PDF");
		buttonProcessar.addActionListener((event) -> action_proccess());
		buttonProcessar.setBounds(670, 237, 30, 25);
		getContentPane().add(buttonProcessar);
		
		setVisible(true);
		
	}

	private void action_seleciona_origem() {
		
		try {
			
			// Abrindo a GUI de seleção de arquivo
			dir_origem = FileChooserHelper.loadDir(this, "Selecione a pasta de origem (planilhas)", false, FileChooserHelper.HOME_DIRECTORY);
			
			// Atualizando o nome do arquivo no label de seleção
			textOrigem.setText(dir_origem.getAbsolutePath());
			
			// Carrega o diretório informado e exibe algumas estatísticas
			new Thread(() -> util_parse_dir()).start();
			
		}
		catch (NullPointerException exception) { }
		catch (Exception exception) { AlertDialog.erro("Não foi possível carregar o diretório"); }
		
	}
	
	/** Analisa o diretório de origem de planilhas */
	private void util_parse_dir() {
		
		// Zerando os contadores
		int dir_count  = 0;
		int file_count = 0;
		
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
					
					dir_count++;
					
					for (File sub_file: sub_dir.listFiles()) {
						
						if (sub_file.isFile() && sub_file.getName().endsWith(".xlsx"))
							file_count++;
						
					}
					
				}
				
			}
			
		}
		
		// Por fim, atualizo a view com as informações encontradas
		String obs = String.format("Encontrada(s) %d planilha(s) em %d pasta(s)",file_count,dir_count);
		SwingUtilities.invokeLater(() -> {
											textOBS.setText(obs);
											labelLoading.setVisible(false);
									     });
		
	}

	private void action_seleciona_destino() {
		
		try {
			
			// Abrindo a GUI de seleção de arquivo
			dir_destino = FileChooserHelper.loadDir(this, "Selecione a pasta de destino (PDF)", true, FileChooserHelper.HOME_DIRECTORY);
			
			// Atualizando o nome do arquivo no label de seleção
			textDestino.setText(dir_destino.getAbsolutePath());
			
		}
		catch (NullPointerException exception) { }
		catch (Exception exception) { AlertDialog.erro("Não foi possível carregar o diretório"); }
		
	}
	
	private void action_clear() {
		
		int res = AlertDialog.dialog("Você tem certeza que deseja limpar os dados da tela?");
		if (res != AlertDialog.OK_OPTION)
			return;
		
		textEdital.setText(null);
		data_realizacao.getJFormattedTextField().setText(null);
		
		textOrigem.setText("<Selecionar Arquivo>");
		textDestino.setText("<Selecionar Arquivo>");
		dir_origem = dir_destino = null;
		
		textOBS.setVisible(false);
		
	}
	
	private void action_proccess() {
		
		String edital = textEdital.getText().trim();
		String data   = data_realizacao.getJFormattedTextField().getText();
		
		if (util_parse_view(edital,data)) {
			util_lock_fields(true);
			new Thread(() -> util_process(edital,data)).start();
		}
		
	}
	
	private void util_process(String edital, String data_publicacao) {
		
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
					status = core_parse_dir(edital, data_publicacao, sub_dir);
					
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
	
	private boolean util_parse_view(String edital, String data) {
		
		if (edital.equals("")) {
			AlertDialog.erro("Preencha o nome do edital");
			return false;
		}
		
		if (data.equals("")) {
			AlertDialog.erro("Selecione uma data");
			return false;
		}
		
		if (dir_origem == null) {
			AlertDialog.erro("Selecione a pasta de origem");
			return false;
		}
		
		if (dir_destino == null) {
			AlertDialog.erro("Selecione a pasta de destino");
			return false;
		}
		
		if (AlertDialog.dialog("Deseja mesmo continuar com o processamento?") != AlertDialog.OK_OPTION)
			return false;
		
		return true;
		
	}
	
	
	
	
	
	
	public boolean core_parse_dir(String edital, String data_publicacao, File dir_planilhas) {

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
					
					ExcelReader.read(arquivo,listaRecursos);
					
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
		
		
		/************************* Geração de PDF ***************************/
		
		// Obtendo o nome do arquivo PDF
		File pdf = util_pdf_filename(listaOrdenada);
		
		// Atualizando a GUI
		SwingUtilities.invokeLater(() -> textOBS.setText("Gerando PDF '" + pdf.getName() + "'"));
		
		// Exportando o PDF
		try {
			PDFWriter.export(edital, data_publicacao, listaOrdenada, pdf);
		} catch (Exception exception) {
			System.err.println("x Falha ao gerar o PDF '" + pdf.getName() + "': " + exception.getMessage());
			return false;
		}
		
		return true;
	}
	
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
		String filename = String.format("%s%s (Todos).pdf",builder.toString(),listaRecursos.get(0).getCargo().substring(5).replace("/"," - ").replace(":"," "));

		return new File(dir_destino.getAbsolutePath() + "/" + filename);
	}
	
	private void util_obs_reading(File dir, File sheet) {
		
		String message = String.format("<html>Processando pasta %d de %d (%s)...<br>Lendo planilha %d de %d (%s)</html>", dir_proc, dir_tot, dir.getName(), fil_proc, fil_tot, sheet.getName());
		SwingUtilities.invokeLater(() -> textOBS.setText(message));
		
	}
	
	private void util_lock_fields(boolean lock) {
		
		lock = !lock;
		
		textEdital.setEditable(lock);
		
		buttonOrigem.setEnabled(lock);
		buttonDestino.setEnabled(lock);
		
		buttonSair.setEnabled(lock);
		buttonLimpar.setEnabled(lock);
		buttonProcessar.setEnabled(lock);
		
	}
	
}
