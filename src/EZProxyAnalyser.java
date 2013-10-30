import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.maxmind.geoip.LookupService;

public class EZProxyAnalyser {
 
	private final UserInterface window;
	Hashtable<String, IPEntry> ip_list; //Global variable to be used later in the CSV creation. Needs some tidying here.

	public EZProxyAnalyser(UserInterface f){
		window = f;
	}
	
	public ArrayList<User> parseLog(String filename){
		File file = new File(filename);
		BufferedReader reader = null;
		Hashtable<String, User> t = new Hashtable<String, User>(); //Hastable of all Users (tracks IPs, hits, bytes, city)
		ip_list = new Hashtable<String, IPEntry>(); //Hashtable of all IPs (shows all users of that IP, city)
		
		
		try {
			reader = new BufferedReader(new FileReader(file));
			String text = null;
			
			String dbfile = "." + System.getProperty("file.separator") + "GeoLiteCity.dat"; //Look in same directory for the GeoIP file
			LookupService lookup = new LookupService(dbfile,LookupService.GEOIP_MEMORY_CACHE);
			
			
			//Prepare the regex to parse the log files
			String regex = "(\\s\\\".*?[^\\\\]\"\\s|\\[.*?\\]|[a-zA-Z0-9\\.-]*)"; // Contained in quotes OR contained in square brackets OR is a segment with no spaces
			Pattern p = Pattern.compile(regex); 
			Matcher m;
			
			while ((text = reader.readLine()) != null) {
				m = p.matcher(text);
				List<String> matches = new ArrayList<String>();
				
				//Place all regex matches into a List
				while(m.find()){
				    if(!m.group().equals("")) //Not sure why, but it matches all delimiting spaces. So manually ignore them
				    	matches.add(m.group().trim());
				}
				
				//matches.get(0) = IP ADDRESS
				//matches.get(1) = SESSION HASH
				//matches.get(2) = Unknown
				//matches.get(3) = MATRIX ID
				//matches.get(4) = [Date] (Note: encased in square brackers & contains spaces)
				//matches.get(5) = "HTTP Request" (Note: encased in quotes & contains spaces)
				//matches.get(6) = HTTP Response Code
				//matches.get(7) = Bytes transfered
				
				try{
					Integer.parseInt(matches.get(7));
				
				}
				catch(NumberFormatException e){
					for(int i=0; i<matches.size(); i++){
						System.err.println("Match at "+i+": "+ matches.get(i));
						
						
					}
					System.err.println("\n\n");
					e.printStackTrace();
					System.exit(1);
				}
				
				catch(IndexOutOfBoundsException e){
					window.setMessage("IndexOutOfBounds happened: Ignoring line\n");
					for(int i=0; i<matches.size(); i++){
						window.setMessage("Match at "+i+": "+ matches.get(i)+"\n");
					}
					break;
					
				}
				
				//Populate the ArrayList linking IP address to usernames
				if(!matches.get(3).equals("-")){
					if(ip_list.containsKey(matches.get(0))){
						IPEntry temp = ip_list.get(matches.get(0)); 
						temp.add(matches.get(3));
						ip_list.put(matches.get(0),temp);
					}
					else{
						ip_list.put(matches.get(0), new IPEntry(lookup, matches.get(0), matches.get(3)));
					}
					
				}
				
				
				//Populate the main ArrayList with User activity data (hits, origin IP, etc...)
				//User is logged in
				if(!matches.get(3).equals("-")){
					if(t.containsKey(matches.get(3))){
						User u;
						
						u = t.get(matches.get(3));
						u.addIP(matches.get(0), true);
						u.addBytes(Integer.parseInt(matches.get(7)));
						t.put(matches.get(3), u);
					}
					else{
						User u = new User(lookup,matches.get(3));
						u.addIP(matches.get(0), false);
						u.addBytes(Integer.parseInt(matches.get(7)));
						t.put(matches.get(3), u);
					}
				}
				
				//User is not logged in
				else{
					if(t.containsKey(matches.get(0))){
						User u = t.get(matches.get(0));
						u.addIP(matches.get(0), false);
						u.addBytes(Integer.parseInt(matches.get(7)));
						t.put(matches.get(0), u);
					}
					else{
						User u = new User(lookup,matches.get(0));
						u.addIP(matches.get(0), true);
						u.addBytes(Integer.parseInt(matches.get(7)));
						t.put(matches.get(0), u);
					}
				}
				
			}
			lookup.close();
		}
		catch (Exception e) {
			//TODO: Send an email alerting of this failure!
			window.setMessage("Error Parsing File. Make sure it is a valid EZProxy Log file!");
			e.printStackTrace();
			return null;
		} 
		finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} 
			catch (IOException e) {
				window.setMessage(e.toString());
			}
		}
		
		//Return the list in order
		ArrayList<User> finaldata = new ArrayList<User>(t.values());
		Collections.sort(finaldata, new UserComparator());
		
		return finaldata;
	}
	
	public void writeFile(ArrayList<User> d,String filename){
		try{
			if(!filename.endsWith(".csv")) filename = filename+".csv";
			
			//Create file 
			FileWriter fstream = new FileWriter(filename);
			BufferedWriter out = new BufferedWriter(fstream);
			
			//Write the header
			out.write("Username,Hits,Data Used (MB),Originating IPs,Region/Country,City\n");
			
			for(int i=0;i<d.size(); i++){
				//Username, Hits, Bytes
				out.write(d.get(i).getName());
				out.write(", " + d.get(i).getHits());
				
				//Convert Bytes to Megabytes
				double bytes = ((double)d.get(i).getBytes()) / 1024 / 1024;
				out.write(", " + bytes);
				
				//Origin IP Address
				out.write(",\"");
				for(int j=0;j<(d.get(i).getIP().size()-1); j++){
					out.write(d.get(i).getIP().get(j) + " , ");
				}
				
				//Last item in array for IP
				if(d.get(i).getIP().size() > 0){
					out.write(d.get(i).getIP().get(d.get(i).getIP().size()-1));
				}
				
				out.write("\",\"");
				
				//Origin Cities/Country
				if(d.get(i).getCountries().size() > 0){
					for(int j=0;j<(d.get(i).getCountries().size()-1); j++){
						out.write(d.get(i).getCountries().get(j).region + "," + d.get(i).getCountries().get(j).countryName + "|");
					}
					//Last item in array for Country/City
					out.write(d.get(i).getCountries().get(d.get(i).getCountries().size()-1).region + "," + d.get(i).getCountries().get(d.get(i).getCountries().size()-1).countryName);
					
				}
				
				out.write("\",\"");
	
				if(d.get(i).getCountries().size() > 0){
					for(int j=0;j<(d.get(i).getCountries().size()-1); j++){
						out.write(d.get(i).getCountries().get(j).city + "|");
					}
					
					out.write(d.get(i).getCountries().get(d.get(i).getCountries().size()-1).city);
					
				}
				
				out.write("\"\n");
			}

			//Close the output stream
			out.close();
		}
		
		
		catch (Exception e){//Catch exception if any
			window.setMessage("Error creating User Usage file : " + e.toString());
			e.printStackTrace();
		}
		
		//Create the file listing users per IP address
		try{
			FileWriter fstream = new FileWriter(filename.substring(0, filename.length()-4) + "_ip_usage.csv");
			BufferedWriter out = new BufferedWriter(fstream);
			
			
			ArrayList<IPEntry> finaldata = new ArrayList<IPEntry>(ip_list.values());
			
			//Sort by most used IP address
			Collections.sort(finaldata, new IPComparator());
			
			//Create the header
			out.write("IP,Count,City/Country,Usernames\n");
			
			for(int i=0;i<finaldata.size(); i++){
				out.write(finaldata.get(i).getCSVList()+"\n");
			}
			
			out.close();
				
		}
		catch (Exception e){//Catch exception if any
			window.setMessage("Error creating IP Usage file : "+e.toString());
			e.printStackTrace();
		}
	}

}

class UserComparator implements Comparator<User> {
    public int compare(User u1, User u2) {
        return u2.getHits() - u1.getHits();
    }
}

class IPComparator implements Comparator<IPEntry> {
    public int compare(IPEntry u1, IPEntry u2) {
        return u2.size() - u1.size();
    }
}


