package mc.alk.shops.objects;


/**
 * 
 * @author alkarin
 *
 */
public class SignFormatException extends Exception {
	private static final long serialVersionUID = 1L;
	public SignFormatException(String msg) {
		super(msg);
	}	
	public SignFormatException(String msg, int line) {
		super(msg + " : Line " + line);
	}	
}
