import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.Type;

public class Experiments {

	public static void main(String[] args) {
		String[] websites = {"Google.com", "Facebook.com", "Youtube.com", "Baidu.com", "Yahoo.com",
				 "Amazon.com", "Wikipedia.org", "Qq.com", "Google.co.in", "Twitter.com", 
				 "Live.com", "Taobao.com", "Sina.com.cn", "Msn.com", "Yahoo.co.jp", "Linkedin.com",
				 "Weibo.com", "Google.co.jp", "Vk.com", "Yandex.ru", "Bing.com", "Hao123.com",
				 "Ebay.com", "Instagram.com", "Google.de"};
		
		doExperiment(1, websites);
		doExperiment(2, websites);
		doExperiment(3, websites);
	}
	
	// this method does the experiment for the input websites
	public static void doExperiment(int experimentNumber, String[] websites) {
		DNSResolver dnsResolver = new DNSResolver();
		Map<String, ExperimentInformation> resolveTimeMapping_experiment1 = 
				new HashMap<String, ExperimentInformation>();
		for (String website: websites) {
			dnsResolver.setDomainName(website);
			double totalResolveTime = 0.0;
			List<Long> resolveTime_experiment1 = new ArrayList<Long>();
			for (int i = 0; i < 10; i++) {
				long startTime = System.currentTimeMillis();
				try {
					switch (experimentNumber) {
					case 1:
						String IPS = dnsResolver.getIP();
						break;
					case 2:
						try {
							InetAddress address = InetAddress.getByName(website);
						} catch (UnknownHostException e) {
							e.printStackTrace();
						}
						break;
					case 3:
						try {
							Resolver resolver = new SimpleResolver("8.8.8.8");
							Lookup lookup = new Lookup(website, Type.A);
							lookup.setResolver(resolver);
							Record[] records = lookup.run();
							
						}
						catch (IOException e) {
							e.printStackTrace();
						}
						break;
					}
					
				} catch (IPNotFoundException e) {
					e.printStackTrace();
				}
				long endTime = System.currentTimeMillis();
				long totalTime = endTime - startTime;
				resolveTime_experiment1.add(totalTime);
				totalResolveTime += totalTime;				
			}
			ExperimentInformation experiment = new ExperimentInformation(resolveTime_experiment1, totalResolveTime/10.0);
			resolveTimeMapping_experiment1.put(website, experiment);
		}
		
		makeExcelFile(resolveTimeMapping_experiment1, "experiment" + experimentNumber);
	}
	
	// this method makes an excel file out of the experiment results
	private static void makeExcelFile(Map<String, ExperimentInformation> resolveTimeMapping_experimentInput, String fileName) {
		Map<String, ExperimentInformation> resolveTimeMapping_experiment = sortTheExperiment(resolveTimeMapping_experimentInput);
		String[] firstLine = {"", "", "exp1", "exp2", "exp3", "exp4", "exp5", "exp6", "exp7", "exp8", "exp9", "exp10", "AVG DNS resolve Time", "Actual Frequency"};
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet spreadsheet = workbook.createSheet(fileName);
		XSSFRow row;
		int rowId = 0;
		row = spreadsheet.createRow(rowId++);
		int cellId = 0;
		for (String cellValue: firstLine) {
			Cell cell = row.createCell(cellId++);
			cell.setCellValue(cellValue);
		}
		double doubleValue = 0.04;
		for (Map.Entry<String, ExperimentInformation> entry: resolveTimeMapping_experiment.entrySet()) {
			String webSite = entry.getKey();
			ExperimentInformation exp = entry.getValue();
			row = spreadsheet.createRow(rowId);
			cellId = 0;
			
			Cell cell = row.createCell(cellId);
			cell.setCellValue("(" + rowId + ")");
			cellId++;
			
			Cell cell2 = row.createCell(cellId);
			cell2.setCellValue(webSite);
			cellId++;
			
			for(Long value: exp.getResolveTimes()) {
				Cell cell3 = row.createCell(cellId++);
				cell3.setCellValue(value);
			}
			
			Cell cell4 = row.createCell(cellId++);
			cell4.setCellValue(exp.getAverageResolveTime());
			
			Cell cell5 = row.createCell(cellId);
			cell5.setCellValue(doubleValue);
			
			doubleValue += 0.04;
			rowId++;
		}
		
		FileOutputStream out;
		try {
			out = new FileOutputStream( new File(fileName + ".xlsx"));
			workbook.write(out);
			out.close();
			workbook.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	// sorting the experiment results for the purpose of CDF
	private static Map<String, ExperimentInformation> sortTheExperiment(Map<String, ExperimentInformation> inputMap) {
		Map<String, ExperimentInformation> output = new LinkedHashMap<String, ExperimentInformation>();
		
		while (!inputMap.isEmpty()) {
			// finding the minimum
			Iterator<Map.Entry<String, ExperimentInformation>> iter = inputMap.entrySet().iterator();
			Map.Entry<String, ExperimentInformation> minEntry = iter.next();
			String minimumWebsite = minEntry.getKey();
			double minimumExperimentAverageResolveTime = minEntry.getValue().getAverageResolveTime();
			while (iter.hasNext()) {
				Map.Entry<String, ExperimentInformation> entry = iter.next();
				if (entry.getValue().getAverageResolveTime() < minimumExperimentAverageResolveTime) {
					minimumWebsite = entry.getKey();
					minimumExperimentAverageResolveTime = entry.getValue().getAverageResolveTime();
					minEntry = entry;
				}
			}
			
			// adding the minimum to the end of output
			output.put(minimumWebsite, inputMap.get(minimumWebsite));
			// removing the minimum from inputMap
			inputMap.remove(minimumWebsite);
		}
		
		
		return output;
	}
}
