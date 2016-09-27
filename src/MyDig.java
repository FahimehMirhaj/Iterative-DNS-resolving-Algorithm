import org.xbill.DNS.Message;
import org.xbill.DNS.Type;

public class MyDig {
	public static void main(String[] args) {
		if (args.length < 2) {
			System.out.println("Error: You need to determine the domain-name and also the type of dig");
			return;
		}
		
		DNSResolver resolver = new DNSResolver(args[0]);
		try {
			Message result = resolver.dig((args[1].equals("A") ? Type.A : 
												(args[1].equals("NS") ? Type.NS :
													(args[1].equals("MX") ? Type.MX : -1))));
			System.out.println(result);
		} catch (IPNotFoundException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		
	}
}
