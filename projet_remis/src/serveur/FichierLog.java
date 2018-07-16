package serveur;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

public class FichierLog {
	
	public static File log =  new File("fichier.log");
		
	public static void writeLog(String msg){
	 
	        Calendar c = Calendar.getInstance();
	        int heure = c.get(Calendar.HOUR_OF_DAY);
	        int minutes = c.get(Calendar.MINUTE);
	        int secondes = c.get(Calendar.SECOND);
	 
	        String h = ThereNeedsToZero(heure);
	        String m = ThereNeedsToZero(minutes);
	        String s = ThereNeedsToZero(secondes);
	        String prefix = "[" + h + ":" + m + ":" + s + "] ";
	        FileWriter fw = null;
	        try {
	            fw = new FileWriter(log, true);
	        } catch (IOException e1) {
	            e1.printStackTrace();
	            //dialogErrorLog();
	        }
	        BufferedWriter output = new BufferedWriter(fw);
	         
	        try {
	            output.write(prefix + msg + "\n");
	            output.flush();
	            output.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	            //dialogErrorLog();
	        }
	    }
	public static String ThereNeedsToZero(int i){
	        String a = Integer.toString(i);
	        if(i < 10){
	            a = 0 + a;
	        }
	        return a;
	    }
}
