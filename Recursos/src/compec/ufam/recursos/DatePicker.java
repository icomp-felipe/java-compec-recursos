package compec.ufam.recursos;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.SwingConstants;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import com.phill.libs.ui.GraphicsHelper;


public class DatePicker {

	private static Properties getProperties() {
		Properties props = new Properties();
		
		props.put("text.today", "Hoje");
		props.put("text.month", "Mês");
		props.put("text.year", "Ano");
		
		return props;
	}
	
	public static JDatePickerImpl getDatePicker() {
		
		Properties properties = getProperties();
		UtilDateModel model   = new UtilDateModel();
		
		JDatePanelImpl      datePanel = new JDatePanelImpl(model, properties);
		JDatePickerImpl    datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());
		JFormattedTextField textField = datePicker.getJFormattedTextField();
		
		textField.setHorizontalAlignment(SwingConstants.CENTER);
		textField.setFont(GraphicsHelper.getInstance().getFont());
		textField.setForeground(GraphicsHelper.getInstance().getColor());
		
		return datePicker;
	}
	
	private static class DateLabelFormatter extends AbstractFormatter {

		private static final long serialVersionUID = 1L;
		private String datePattern = "dd/MM/yyyy";
	    private SimpleDateFormat dateFormatter = new SimpleDateFormat(datePattern);

	    @Override
	    public Object stringToValue(String text) throws ParseException {
	        return dateFormatter.parseObject(text);
	    }

	    @Override
	    public String valueToString(Object value) throws ParseException {
	        if (value != null) {
	            Calendar cal = (Calendar) value;
	            return dateFormatter.format(cal.getTime());
	        }

	        return "";
	    }

	}
	
}
