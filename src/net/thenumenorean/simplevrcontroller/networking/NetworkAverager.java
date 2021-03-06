package net.thenumenorean.simplevrcontroller.networking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.net.wifi.ScanResult;

public class NetworkAverager {
	
	public static final int AVERAGE_COUNT = 5;
	
	private ArrayList<AveragedNetworkInfo> networks;
	
	public NetworkAverager(){
		networks = new ArrayList<AveragedNetworkInfo>();
	}
	
	/**
	 * Calculates the averagaes again based on the new data which is given. (See source for algorithm)
	 * @param scans A list of scans to add
	 */
	public void calculateAverages(List<ScanResult> scans){
		for(ScanResult s : scans){
			
			boolean added = false;
			for(AveragedNetworkInfo inf : networks){
				if(inf.bssid.equals(s.BSSID)){
					inf.addNumber(s.level);
					added = true;
					inf.just_updated = true;
					break;
				}
			}
			
			if(!added){
				AveragedNetworkInfo av = new AveragedNetworkInfo(s.SSID, s.BSSID, AVERAGE_COUNT);
				av.addNumber(s.level);
				networks.add(av);
			}
			
		}
		
		for(AveragedNetworkInfo inf : networks){
			if(!inf.just_updated)
				inf.addNumber(Integer.MIN_VALUE);
			inf.just_updated = false;
		}
		
		Collections.sort(networks, new Comparator<AveragedNetworkInfo>(){

			@Override
			public int compare(AveragedNetworkInfo arg0, AveragedNetworkInfo arg1) {
				int comp = arg0.compareTo(arg1);
				return  -comp;
			}
			
		});
		
		ArrayList<AveragedNetworkInfo> tmp = new ArrayList<AveragedNetworkInfo>();
		for(AveragedNetworkInfo inf : networks){
			if((inf.missing_streak + inf.missing_streak) > AVERAGE_COUNT)
				tmp.add(inf);
		}
		
		networks.removeAll(tmp);
	}
	
	/**
	 * Helper class that contains data about a single Network and averages it over time.
	 * @author fmacagno
	 *
	 */
	public class AveragedNetworkInfo implements Comparable<AveragedNetworkInfo> {
		
		public final String bssid;
		public final String ssid;
		private List<Integer> nums;
		
		
	//System.out.println();
		private int store;
		public boolean just_updated;
		public int missing_streak;

		/**
		 * Creates a new instance with the given identifiers. It will then keep track of the last <storage> 
		 * values and average them
		 * @param ssid
		 * @param bssid
		 * @param storage
		 */
		public AveragedNetworkInfo(String ssid, String bssid, int storage) {
			this.ssid = ssid;
			this.bssid = bssid;
			store = storage;
			
			nums = Collections.synchronizedList(new ArrayList<Integer>());
			
			just_updated = false;
			
			missing_streak = 0;
		}
		
		
	//System.out.println();

		/**
		 * Adds a signal strength to the averaging process
		 * @param num
		 */
		public void addNumber(int num){
			
			if(num == Integer.MIN_VALUE){

				
				
				//System.out.println();
				missing_streak++;
				
			} else {
			
				if(nums.size() >= store)
					nums.remove(0);
			
				nums.add(num);
				
				missing_streak = 0;
			}
			
			just_updated = true;
		}

		/**
		 * Gets the averaged signal strength value
		 * @return An integer
		 */
		public int getAveragedLevel() {
			
			int total = 0;
			
			synchronized (nums) {
				for(Integer i : nums){
					total += i;
				}
				
			
			//System.out.println();
			}
			
			return total / nums.size();
		}

		@Override
		public int compareTo(AveragedNetworkInfo another) {
			return getAveragedLevel() - another.getAveragedLevel();
		}

	}

	/**
	 * Returns a clone of the current list of Averages for the networks
	 * @return A list, potentially empty
	 */
	public List<AveragedNetworkInfo> getAverages() {
		return (List<AveragedNetworkInfo>) networks.clone();
	}

}
