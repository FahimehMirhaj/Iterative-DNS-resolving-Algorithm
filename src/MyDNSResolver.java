
public class MyDNSResolver {
	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("Error: You need to determine the domain-name");
			return;
		}
		
		DNSResolver resolver = new DNSResolver(args[0]);
		try {
			String ipAddresses = resolver.getIP();
			if (ipAddresses.equals("")) {
				System.out.println("IP not found for the " + args[0]);
			}
			else 
				System.out.println(ipAddresses);
		} catch (IPNotFoundException e) {
			e.printStackTrace();
		}
		
	}
}
