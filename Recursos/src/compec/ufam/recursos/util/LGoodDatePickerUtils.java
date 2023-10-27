package compec.ufam.recursos.util;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import com.phill.libs.ui.*;

import com.github.lgooddatepicker.components.*;
import com.github.lgooddatepicker.components.DatePickerSettings.*;
import com.github.lgooddatepicker.components.TimePickerSettings.*;

/** Helper para criação de {@link DatePicker} and {@link DateTimePicker}.
 *  Todos esses componentes contém personalização nos botões e fontes.
 *  @author Felipe André - felipeandresouza@hotmail.com
 *  @version 1.0, 22/JAN/2022 */
public class LGoodDatePickerUtils {
	
	// Localização para Português
	private static final Locale portuguese = Locale.of("pt", "BR");
	
	// TimeSettings - único que pode ser singleton
	private static final TimePickerSettings timeSettings = getTimeSettings();
	
	// Ícones
	private static final ImageIcon datePickerIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(LGoodDatePickerUtils.class.getResource("/images/datepickerbutton1.png")));
	private static final ImageIcon timePickerIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(LGoodDatePickerUtils.class.getResource("/images/timepickerbutton1.png")));
	
	/** Recupera um DatePicker personalizado. */
	public static DatePicker getDatePicker() {
		
		DatePicker datePicker = new DatePicker(getDateSettings());
		JButton datePickerButton = datePicker.getComponentToggleCalendarButton();
		
		// Modificando estilo do botão do DatePicker
		datePickerButton.setText(null);
		datePickerButton.setIcon(datePickerIcon);
		
		return datePicker;
	}
	
	/** Recupera um DateTimePicker personalizado. */
	public static DateTimePicker getDateTimePicker() {
		
		DateTimePicker dateTimePicker = new DateTimePicker(getDateSettings(), timeSettings);
		JButton calendarButton = dateTimePicker.getDatePicker().getComponentToggleCalendarButton();
		JButton timeMenuButton = dateTimePicker.getTimePicker().getComponentToggleTimeMenuButton();
		
		// Modificando estilo dos botões do DateTimePicker
		calendarButton.setText(null);
		timeMenuButton.setText(null);
	    
	    calendarButton.setIcon(datePickerIcon);
	    timeMenuButton.setIcon(timePickerIcon);
	    
		return dateTimePicker;
	}
	
	/** Prepara as configurações do DatePicker:<br>
	 *  1. Formato de data português (dd MMM yyyy);<br>
	 *  2. Fonte 'Swiss' no calendário;<br>
	 *  3. Fonte 'Swiss' no text field;<br>
	 *  4. Cor de fonte azul, para datas válidas. */
	private static DatePickerSettings getDateSettings() {
			
		DatePickerSettings dateSettings = new DatePickerSettings(portuguese);
	    dateSettings.setFormatForDatesCommonEra("dd MMM yyyy");
	    dateSettings.setFontCalendarDateLabels(GraphicsHelper.getInstance().getFont());
	    dateSettings.setFontValidDate(GraphicsHelper.getInstance().getFont());
	    dateSettings.setColor(DateArea.DatePickerTextValidDate, GraphicsHelper.getInstance().getColor());
		
		return dateSettings;
	}
	
	/** Prepara as configurações do TimePicker:<br>
	 *  1. Formato de horário português (HH:mm);<br>
	 *  2. Fonte 'Swiww' no text field;
	 *  3. Cor de fonte azul, para horários válidos. */
	private static TimePickerSettings getTimeSettings() {
		
		TimePickerSettings timeSettings = new TimePickerSettings(portuguese);
		timeSettings.setColor(TimeArea.TimePickerTextValidTime, GraphicsHelper.getInstance().getColor());
		timeSettings.fontValidTime = GraphicsHelper.getInstance().getFont();
		
		return timeSettings;
	}
	
}
