package serveur;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.ListIterator;

import javax.json.JsonObject;


public class Chat_ClientServeur implements Runnable {

	private ArrayList<Socket> socketArray = null;
	private ArrayList<JsonObject> jsonArray = null;
	
	public Chat_ClientServeur(ArrayList<Socket> sockArray, String log, int id, ArrayList<JsonObject> jsonArr){
		socketArray = sockArray;
		jsonArray = jsonArr;
	}
	
	public void run() {
		
		/* Mettre un iterator qui parcourira tout l'arraylist Socket
		 * on creer un "in" et "out" pour chacune d'elle
		 * Thread Reception filtre et si c'est le cas, Thread Emission envoi les données
		 * */
		ListIterator<Socket> itr = socketArray.listIterator();
		int i=-1;
		while(itr.hasNext()){
			Socket so = itr.next();

			++i;
			System.out.println("SOCKET ["+i+"]:"+so.isClosed()); // A RETIRER SI ON REGELE LE PROBLEME DE CROISEMENT
			if(! (jsonArray.get(i)).getString("state").equals("DISCONNECT")){
				try {
					BufferedReader in = new BufferedReader(new InputStreamReader(so.getInputStream()));
					PrintWriter out = new PrintWriter(so.getOutputStream());
					
					Thread t3 = new Thread(new Reception(in, out, jsonArray, socketArray, i));//++i));
					t3.start();
				}catch (IOException e) {
					System.err.println("Cette socket est déconnectée "+e.getMessage());//e.printStackTrace());
				}
			}
		}
	}
}	
