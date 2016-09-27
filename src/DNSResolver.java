import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.xbill.DNS.DClass;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.SOARecord;
import org.xbill.DNS.Section;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.Type;

public class DNSResolver {
	// data member
	private String domainName;
	/* NOTE: this hard-coded list of TLDs have been used only for the first time of running the program.
	   after the first time, the file is generated and then, from the next time, the list of TLDs are read 
	   from the file (due to the purpose of round-robin)
	 */ 
	private static List<String> tlds = Collections.synchronizedList(new ArrayList<String>(Arrays.asList(new String[] {"a.root-servers.net.", "b.root-servers.net.", "c.root-servers.net.",
									"d.root-servers.net.", "e.root-servers.net.", "f.root-servers.net.",
									"g.root-servers.net.", "h.root-servers.net.", "i.root-servers.net.",
									"j.root-servers.net.", "k.root-servers.net.", "l.root-servers.net.",
									"m.root-servers.net."})));

	// constructor
	public DNSResolver(String domainName) {
		setDomainName(domainName);
		readTLDFile();
	}
	
	// NOTE: if this constructor is used, the, we need to call the method setDomainName(...) afterwards
	public DNSResolver() {
		readTLDFile();
	}
	
	private void readTLDFile() {
		String fileName = "TLD.ser";
		boolean fileExists = false;
		try{
	        //use buffering
	            InputStream file = new FileInputStream(fileName);
	            InputStream buffer = new BufferedInputStream(file);
	            ObjectInputStream input = new ObjectInputStream(buffer);
	            try{
	            	tlds = (List<String>) input.readObject();
	                fileExists = true;
	            }
	            finally{
	                input.close();
	            }
	        }
	        catch(ClassNotFoundException ex){
//	            ex.printStackTrace();
	        }
	        catch(IOException ex){
//	            ex.printStackTrace();
	        }
	        
	        if (!fileExists) {
	            // putting the default-hard coded one and then storing it on the disk
	            try{
	                //use buffering
	                OutputStream file = new FileOutputStream(fileName);
	                OutputStream buffer = new BufferedOutputStream(file);
	                ObjectOutput output = new ObjectOutputStream(buffer);
	                try{
	                  output.writeObject(tlds);
	                }
	                finally{
	                  output.close();
	                }
	            }  
	            catch(IOException ex){
	                ex.printStackTrace();
	            }
	        }
	}
	
	private Message consult(String dnsServerAddress, String domainName, int queryType) throws UnknownHostException, IOException {
		Resolver resolver = null;
		resolver = new SimpleResolver(dnsServerAddress);
		Record qr = Record.newRecord(Name.fromConstantString(domainName), queryType, DClass.IN);
		Message response = null;
		response = resolver.send(Message.newQuery(qr));
		return response;
	}
	
	public Message dig(int type) throws IPNotFoundException, IllegalArgumentException {
		if (!(type == Type.A || type == Type.NS || type == Type.MX)) {
			throw new IllegalAccessError("The type of dig query must be either A, or NS or MX!");
		}
		String[] domainNameParts = domainName.trim().split("\\.");
		Collections.reverse(Arrays.asList(domainNameParts));
		for (int i = 0; i < domainNameParts.length; ++i) {
			domainNameParts[i] += ".";
		}
		
		String domainToBeAsked = "";
		List<String> dnsServers = null;
		for (int i = 0; i < domainNameParts.length; ++i) { // querying NS query
			domainToBeAsked = domainNameParts[i] + domainToBeAsked;
			if (i == 0) {
				dnsServers = tlds;
			}
			boolean connectionSuccessful = false;
			Message result = null;
			Iterator<String> itr = dnsServers.iterator();
			String dnsServer = null;
			while (itr.hasNext()) {
				dnsServer = itr.next();
				try {
					result = consult(dnsServer, domainToBeAsked, Type.NS);
					connectionSuccessful = true;
					break;
					
				} catch (UnknownHostException ex) {
					ex.printStackTrace();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
			
			if (!connectionSuccessful) {
				throw new IPNotFoundException(domainToBeAsked);
			}
			else {
				if (i == 0) { // first updating the TLD file to have round-robin selection
					Iterator<String> iter = tlds.iterator();
					while (iter.hasNext()) {
						String dnsServerInTLD = iter.next();
						if (dnsServerInTLD.equals(dnsServer)) {
							iter.remove();
							tlds.add(dnsServer);
							break;
						}
						else {
							iter.remove();
							tlds.add(dnsServer);
						}
					}
					
					// now, writing back to the file
					try{
			            File fileToBeRemoved = new File("TLD.ser");
			            fileToBeRemoved.delete();
			            //use buffering
			            OutputStream file = new FileOutputStream("TLD.ser");
			            OutputStream buffer = new BufferedOutputStream(file);
			            ObjectOutput output = new ObjectOutputStream(buffer);
			            try{
			              output.writeObject(tlds);
			            }
			            finally{
			              output.close();
			            }
			        }  
			        catch(IOException ex){
			                ex.printStackTrace();
			        }
				}
				
				dnsServers = new ArrayList<String>();
				Record[] answers = result.getSectionArray(Section.AUTHORITY);
		        for (Record record: answers) {
		        	if (record.getType() == Type.SOA) {
		        		SOARecord soaRecord = (SOARecord) record;
		        		dnsServers.add(soaRecord.getHost().toString());
		        	}
		        	else
		        		dnsServers.add(record.getAdditionalName().toString());
		        }
		        
		        Record[] answerSectionAnswers = result.getSectionArray(Section.ANSWER);
		        for (Record record: answerSectionAnswers) {
		        	if (record.getType() == Type.NS) {
		        		dnsServers.add(record.getAdditionalName().toString());
		        	}
		        }
			}
		}
		// the final state
		boolean successful = false;
		Message response = null;
		for (String dnsServer: dnsServers) {
			try {
				response = consult(dnsServer, domainName + ".", type);
				successful = true;
				break;
			} catch (UnknownHostException ex) {
				ex.printStackTrace();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		if (!successful) {
			throw new IPNotFoundException(domainName);
		}
		else {
			return response;
		}
	}
	
	public String getIP() throws IPNotFoundException {
//		System.out.println("Method getIP() gets called for the domain-name: " + domainName);
		Message finalResponse = dig(Type.A);
		String ultimateResult = "";
		Record[] answers = finalResponse.getSectionArray(Section.ANSWER);
        for (Record record: answers) {
        	ultimateResult += (record.rdataToString()) + "\n";
        }
        
        return ultimateResult;
	}
	
	
	// setter and getters for the data members
	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}
	
	// test program
	public static void main(String[] args) {
		
		DNSResolver resolver = new DNSResolver("Google.co.in");
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
