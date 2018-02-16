import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

public class DonationAnalytics {

	public static void main(String[] args) {
		// String percentileFileName = "./input/percentile.txt";
		String percentileFileName = "./../input/percentile.txt";
		// String inputFileName = "./input/itcont.txt";
		String inputFileName = "./../input/itcont.txt";
		// String outputFileName = "./output/repeat_donors.txt";
		String outputFileName = "./../output/repeat_donors.txt";
		int bufferSize = 8*1024;
		int percentile = 0;
		try (BufferedReader bReader = new BufferedReader(new FileReader(percentileFileName), bufferSize)) {
			percentile = Integer.parseInt(bReader.readLine());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Set<String> pastYearsDonors = new HashSet<String>();
		Set<String> newDonors = new HashSet<String>();
		Map<String, DonationsCollection> repeatDonations = new HashMap<String, DonationsCollection>();
		int currentYear = 0;
		
		try (BufferedReader reader = new BufferedReader(new FileReader(inputFileName), bufferSize);
				BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName), bufferSize)) {
			String inputLine;
			while ((inputLine = reader.readLine()) != null) {
				String[] fields;
				fields = parseInputLine(inputLine);
				if (fields == null) {
					// Invalid entry -> ignore
					continue;
				}
				// Valid entry, let's do the work!
				// Current Donor's unique ID = NAME-ZIPCODE
				String currentDonor = fields[1] + "-" + fields[2];
				int year = Integer.parseInt(fields[3]);
				if (year < currentYear) {
					// Add this donor to prior years donors
					pastYearsDonors.add(currentDonor);
					continue;
				}
				if (year > currentYear) {
					// A new year has come!
					// Add donors to past years donors set
					pastYearsDonors.addAll(newDonors);
					newDonors = new HashSet<String>();
					currentYear = year;
				} 
				// Check if a repeat donor
				if (pastYearsDonors.contains(currentDonor)) {
					String key = fields[0] + "-" + fields[2];
					DonationsCollection donations = (repeatDonations.containsKey(key)) ? 
							repeatDonations.get(key): new DonationsCollection(percentile);
					double amount = Double.parseDouble(fields[4]);
					donations.addDonation(amount);
					double totalAMT = donations.getTotalRepeatDonations();
					
					String output = fields[0] + "|" + fields[2] + "|" + fields[3] + "|" 
							+ donations.getRunningPercentile() + "|";
					// Remove decimals if the total amount is actually an integer (00 after the decimal point)
					double tempAMT = totalAMT * 100;
					if ((tempAMT % 100 == 0)) {
						long amt = (long) totalAMT;
						output += amt + "|" + donations.getNumberOfRepeatDonations();
					} else {
						output += totalAMT + "|" + donations.getNumberOfRepeatDonations();
					}
					repeatDonations.put(key, donations);
					writer.write(output);
					writer.newLine();
				} else {
					newDonors.add(currentDonor);
				}
				
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static String[] parseInputLine(String inputLine) {
		String[] fields = new String[5];
		String[] allFields = inputLine.split("\\|", 16);
		if (allFields.length < 16 || !allFields[15].startsWith("|") || 
				allFields[0].isEmpty() || allFields[7].length() < 2 || 
				allFields[10].length() < 5 || allFields[13].length() < 8 || 
				allFields[14].isEmpty()) {
			return null;
		}
		
		fields[0] = allFields[0]; // CMTE_ID
		fields[1] = allFields[7]; // NAME
		fields[2] = allFields[10].substring(0, 5); // 5-digits ZIP_CODE
		fields[3] = allFields[13].substring(4); // YEAR in the format YYYY
		fields[4] = allFields[14]; // TRANSACTION_AMT
		
		return fields;
	}

}


class DonationsCollection {
	
	private int percentile;
	private double totalRepeatDonations;
	private Queue<Double> lowHeap; // Max Heap - Holds values less than or equal to the running percentile
	private Queue<Double> highHeap; // Min Heap - Holds all values greater than the running percentile
	
	public DonationsCollection(int percentile) {
		this.percentile = percentile;
		totalRepeatDonations = 0;
		lowHeap = new PriorityQueue<Double>(new Comparator<Double>() {
			@Override
			public int compare(Double d1, Double d2) {
				return Double.compare(d2, d1);
			}
		});
		highHeap = new PriorityQueue<Double>();
	}
	
	public double getTotalRepeatDonations() {
		return totalRepeatDonations;
	}
	
	public int getNumberOfRepeatDonations() {
		return lowHeap.size() + highHeap.size();
	}
	
	public long getRunningPercentile() {
		double runningPercentile = lowHeap.peek();
		return (long) Math.round(runningPercentile);
	}
	
	public void addDonation(double amount) {
		if (lowHeap.isEmpty() || amount < lowHeap.peek()) {
			lowHeap.add(amount);
		} else {
			highHeap.add(amount);
		}
		totalRepeatDonations += amount;
		balanceHeaps();
	}

	// Make sure P% of the contributions are stored in the lowHeap
	private void balanceHeaps() {
		int N = getNumberOfRepeatDonations();
		double rank = N * percentile / 100.0;
		int ordinalRank = (int) Math.ceil(rank);
		while (lowHeap.size() != ordinalRank) {
			if (lowHeap.size() < ordinalRank) {
				lowHeap.add(highHeap.poll());
			} else {
				highHeap.add(lowHeap.poll());
			}
		}
	}
	
}
