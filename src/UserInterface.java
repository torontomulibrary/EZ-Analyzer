import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;


import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;


import javax.swing.JSeparator;



public class UserInterface extends JFrame {

	private static final long serialVersionUID = 913573034499963766L;
	
	
	final JTextPane pane = new JTextPane();
	final UserInterface ui = this;
	
    public UserInterface() {
    	    
    	
		  JPanel basic = new JPanel();
		  basic.setLayout(new BoxLayout(basic, BoxLayout.Y_AXIS));
		  add(basic);
		
		  JPanel topPanel = new JPanel(new BorderLayout(0, 0));
		  topPanel.setMaximumSize(new Dimension(450, 0));
		  JLabel hint = new JLabel("EZProxy Log Analysis Tool");
		  hint.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 0));
		  topPanel.add(hint);
		
		  JSeparator separator = new JSeparator();
		  separator.setForeground(Color.gray);
		
		  topPanel.add(separator, BorderLayout.SOUTH);
		
		  basic.add(topPanel);
		  
		  pane.setEditable(false);
		  JPanel top_button = new JPanel(new FlowLayout(FlowLayout.CENTER));
		  JButton beginButton = new JButton("Analyze Log");
		  
		  beginButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				String logfile = openFile(new Frame(), "Open EZProxy Log File", "", "");
				if(logfile != null){
					EZProxyAnalyser ez = new EZProxyAnalyser(ui);
					pane.setText(pane.getText()+"\n\n" + "Opened file: " + logfile);
					ArrayList<User> d = ez.parseLog(logfile); 
					
					String outfile = null;
					
					if(d != null) {
						outfile = saveFile(new Frame(), "Save CSV Output", "", "");
					}
					
					if(outfile != null && d != null){ 
						ez.writeFile(d, outfile);
						
						if(!outfile.endsWith("csv")) outfile += ".csv";
						pane.setText(pane.getText()+"\n\n" + "Saved file: " + outfile);
					}
				}
			}
		  });
          
          top_button.add(beginButton); 
          basic.add(top_button);

          JPanel textPanel = new JPanel(new BorderLayout());
          textPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
          
          textPanel.add(pane);

          basic.add(textPanel);
          JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
          JButton close = new JButton("Close");
          
          //Give the close button some functionality
          close.addActionListener(new ActionListener(){
        	  public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
        	  }
          });
          
          bottom.add(close); 
          basic.add(bottom);

          bottom.setMaximumSize(new Dimension(450, 0));

          setTitle("EZAnalyzer");
          setSize(new Dimension(450, 350));
          setResizable(false);
          setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
          setLocationRelativeTo(null);
    }
    
    public JFrame getInterface(){
    	return this;
    }
    
    public void setMessage(String msg){
    	pane.setText(pane.getText() + "\n\n" + msg);
    }
    
    public String openFile (Frame f, String title, String defDir, String fileType) {
		FileDialog fd;

		fd = new FileDialog(f, title, FileDialog.LOAD);
		fd.setFile("*.log");
		
		
		fd.setFilenameFilter(new FilenameFilter(){
		    public boolean accept(File dir, String name){
		      return (name.endsWith(".log"));
		    }
		 });

		fd.setVisible(true);
		
		if(fd.getFile() != null){
			return fd.getDirectory()+fd.getFile();
		}
		else return null;
	}
	
	public String saveFile (Frame f, String title, String defDir, String fileType) {
		FileDialog fd;

		fd = new FileDialog(f, title, FileDialog.SAVE);
		fd.setFile("*.csv");

		fd.setVisible(true);
		
		if(fd.getFile() != null){
			return fd.getDirectory()+fd.getFile();
		}
		else return null;
	}
}
   
