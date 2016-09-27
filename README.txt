Fahimeh Mirhaj
Fundamentals of Computer Networks Course

The zip file, contains the following items:
	- myDNSResolver.jar:
		- This jar file is used to resolve the DNS query (for part A).
		- To run the program: java -jar myDNSResolver.jar <domain name>
			- Example: java -jar myDNSResolver.jar www.google.com
				- Output: 
					63.117.14.25
					63.117.14.20
					63.117.14.26
					63.117.14.22
					63.117.14.23
					63.117.14.21
					63.117.14.27
					63.117.14.24

	- myDig.jar: 
		- This jar file is used to run the dig queries (for part B)
		- To run the program: java -jar myDig.jar <domain name> <type>
			- Example: java -jar myDig.jar www.cs.stonybrook.edu A
				- Output:
					;; ->>HEADER<<- opcode: QUERY, status: NOERROR, id: 36094
					;; flags: qr aa rd ; qd: 1 an: 1 au: 2 ad: 2 
					;; QUESTIONS:
					;;	www.cs.stonybrook.edu., type = A, class = IN

					;; ANSWERS:
					www.cs.stonybrook.edu.	28800	IN	A	130.245.27.2

					;; AUTHORITY RECORDS:
					cs.stonybrook.edu.	28800	IN	NS	dns01.cs.stonybrook.edu.
					cs.stonybrook.edu.	28800	IN	NS	dns02.cs.stonybrook.edu.

					;; ADDITIONAL RECORDS:
					dns01.cs.stonybrook.edu.	28800	IN	A	130.245.1.15
					dns02.cs.stonybrook.edu.	28800	IN	A	130.245.1.48

					;; Message size: 127 bytes

	- FCN-HW1.zip:
		- This zip file contains all the source codes (the .java files) which are:
			1- DNSResolver.java:
				- This class contains the basic DNS resolver. It uses the dnsjava library. It implements the iterative DNS resolving algorithm. To keep the round-robin selection for TLDs, in the first execution, it generates a binary file, called "TDL.ser"  which contains the list of TLDs and everytime, after accessing it, it updates the file (i.e., it puts the currently used TLD to the end of the file to have fairness). 
				  It has the following public methods:
					- public Message dig(int type) throws IPNotFoundException, IllegalArgumentException
						This method does the dns resolving algorithm (for the private data member domain name of type String) and based on the input parameter "type" (which can be NS, A or MX), in the last query, it considers the type and creates the corresponding query and returns the message received. For example, if the domainName is www.stanford.edu and the type is "X", the approach is:
							(1) The program, frist, asks a "NS"-type query from one of the TLDs (based on the round-robin selection) for "edu."
							(2) Then, the program asks a "NS"-type query from one of the dns-servers returned from the previous step (1) for "stanford.edu."
							(3) Then, the program asks a "NS"-type query from one of the dns-servers returned from the previous step (2) for "www.stanford.edu."
							(4) Then, as the last step, the program asks an "X"-type query from one of the dsn-servers returned from the previous step (3) for "www.stanford.edu."

						This method throws IPNotFoundException if it can't resolve the IP address (in any of the steps mentioned in the example).
						This method throws IllegalArgumentException if the type parameter is other than NS, A, or MX.
					
					- public String getIP() throws IPNotFoundException:
						This method uses the method dig(...) with input parameter type = "A" as it asks for the IP-address of the requested domain.
						This method throws the same exception in case dig method (which is invoked inside this method) can't resolve the dns query in any of the steps mentioned in the example.

					- public String getDomainName:
						This method returns the domain to be resolved.

					- public void setDomainName(String domainName):
						This method sets the domain to be resolved.

			2- MyDNSResolver.java:
				- This class contains only one static main method which takes the argument, the domain-name to be resolved, instantiates a resolver of type DNSResolver (the previous class) and calls its getIP() method to get the list of asssociated IPs and then, it prints them.

			3- MyDig.java:
				- This class contains only one static main method which takes two arguments, the domain-name to be resolved and the type of the dig-query (which can be either A, or NS or MX). It instantiates a resolver of type DNSResolver (the first class explained) and calls its dig(...) method and prints the received message.

			4- Experiments.java:
				- This class is used for part C of the assignment. It contains the following methods:
					- public static void doExperiment(int experimentNumber, String[] websites):
						- This method takes the experimentNumber (which can be either 1, 2 or 3) and also the list of the 25 top websites and run the required experiment.
						  This method instantiates an object of type DNSResolver (called dnsResolver) and
							- If experimentNumber is 1, it uses the getIP() method of the dnsResolver object to get the associated IPs.
							- If experimentNumber is 2, it calls the static method getByName() of the class InetAddress which indeed uses the local dns-server to get the associated IPs.
							- If experimentNumber is 3, it instantiates a SimpleResolver object (with parameter "8.8.8.8" to use google's public DNS resolver), called resolver, a Lookup object, called lookup. It then sets the resolver of lookup (to be google's public DNS resolver) and calls the run() method to get the IP address.

						  Finally, after executing the experiment and collecting the results, it calls the method makeExcelFile (to be explained later) to generate the associated excel file.

					- private static void makeExcelFile(Map<String, ExperimentInformation> resolveTimeMapping_experimentInput, String fileName):
						- This method gets the information about the experiment and the name of the experiment (which can be experiment1, experiment2 and experiment3) and generates the corresponding excel file. This method uses the java Apache POI library. It is worth mentioning that since, we want to plot the CDF, this method also calls the method sortTheExperiment (to be explained later) to sort the result of the experiments based on the average DSN resolve time.

					- private static Map<String, ExperimentInformation> sortTheExperiment(Map<String, ExperimentInformation> inputMap):
						- This method gets the information about the experiment and returns the sorted order of the same information (sorted based on the average DNS resolve time).

					- public static void main(String[] args):
						- This method calls the method doExperiment() three times with different arguments to run the three required experiments.

			5- IPNotFoundException:
				- This class is a subclass of class Exception which is used for the purpose of exception handling.