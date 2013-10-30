import java.util.ArrayList;

import com.maxmind.geoip.Location;
import com.maxmind.geoip.LookupService;

public class IPEntry{
	private ArrayList<String> usernames;
	private String ip;
	private LookupService lookup;
	
	public IPEntry(LookupService l, String i){
		ip = i; 
		usernames = new ArrayList<String>();
		lookup = l;
	}
	
	public IPEntry(LookupService l, String i, String username){
		ip =i;
		usernames = new ArrayList<String>();
		usernames.add(username);
		lookup = l;
	}
	
	public void add(String username){
		if(!usernames.contains(username))
			usernames.add(username);
	}
	
	public String getCSVList(){
		Location loc = lookup.getLocation(ip);
		
		//====Terrible, yet necessary====
		String locationString;
		
		if(loc != null){
			if(loc.city == null){
				loc.city = "???";
			}
			if(loc.countryName == null){
				loc.countryName = "???";
			}
			
			locationString = "\""+loc.city + ","+loc.countryName+"\",";
		}
		else{
			locationString = "???,";
		}
		
		//=====End terribleness==========
		
		
		
		String output = ip+","+usernames.size()+","+locationString;
		
		for(int i=0; i< usernames.size()-1; i++){
			output+= usernames.get(i) + ",";
		}
		output+= usernames.get(usernames.size()-1);
		
		return output;
	}
	
	public int size(){
		return usernames.size();
	
	}
	

}