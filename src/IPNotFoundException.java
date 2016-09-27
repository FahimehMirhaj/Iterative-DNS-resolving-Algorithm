
public class IPNotFoundException extends Exception {
	
	
	public IPNotFoundException(String domainName) {
		super("IP not found for the " + domainName);
	}	
}
