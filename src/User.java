import java.util.ArrayList;
import java.util.Hashtable;

import com.maxmind.geoip.*;


public class User {
	private String username;
	private ArrayList<String> ip_address;
	private Hashtable<String,Location> countries;
	private int hits;
	private LookupService lookup;
	private long bytesTransfered;
	
	public User(LookupService l){
		username = "";
		countries = new Hashtable<String,Location>();
		ip_address = new ArrayList<String>();
		hits = 1;
		lookup=l;
		bytesTransfered = 0;
	}
	
	public User(LookupService l, String uname){
		
		username = uname;
		countries = new Hashtable<String,Location>();
		ip_address = new ArrayList<String>();
		hits = 1;
		lookup=l;
	}
	
	public void addBytes(int bytes){
		bytesTransfered += bytes;
	}
	
	public void addHit(){
		hits++;
	}
	
	public void addIP(String ip, boolean count){
		if(!ip_address.contains(ip)){
			ip_address.add(ip);
			
			//Add country of origin
			addCountry(ip);
		}
		
		if(count)
			addHit();
	}
	
	public ArrayList<Location> getCountries(){
		return new ArrayList<Location>(countries.values());
	}
	
	private void addCountry(String ip){
			
			Location loc = lookup.getLocation(ip);
			
			//Check to see if country is already added
			
			if(loc != null){
				if(loc.city == null){
					loc.city = "";
				}
				if(loc.countryName == null){
					loc.countryName = "";
				}
				
				if(!countries.contains(loc.city+"," + loc.countryName)){
					countries.put(loc.city+","+loc.countryName, loc);
				}
			}
		
		
		//System.out.println("Country is : "+ countries);
		
	}
	
	public void setName(String name){
		username = name;
	}
	
	public int getHits(){
		return hits;
	}
	
	public long getBytes(){
		return bytesTransfered;
	}
	
	public ArrayList<String> getIP(){
		return ip_address;
	}
	
	public String getName(){
		return username;
	}
}
