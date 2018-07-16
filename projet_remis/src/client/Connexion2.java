package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.Socket;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

public class Connexion2 implements Runnable{

	private Socket socket = null;
	private PrintWriter out = null;
	private BufferedReader in_ = null;
	private boolean connect = false;
	public static JsonObject objJson_ = null;
	public static Thread t2;
	
	public Connexion2(Socket s, JsonObject obj){
		socket = s;
		objJson_ = obj;
	}
	
	public void run() {
		try {
			out = new PrintWriter(socket.getOutputStream());
			in_ = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			System.out.println("\t"+in_.readLine()+"\n");
			
			while(!connect) {
				out.println(objJson_);
				out.flush();

				// Lecture de ce que dit le serveur (authentification: ok ou pas).
				String tmp2 = in_.readLine();
				JsonReader in_json = Json.createReader(new StringReader(tmp2));
				objJson_ = in_json.readObject();
				in_json.close();
				if(objJson_.getString("state").equals("INIT_OK")){
					System.out.println("PHASE 2 -\tIDENTIFICATION:\tReussie");
					connect = true;
				}
				else 
					System.err.println(tmp2+"\n");
			}
			System.out.println("PHASE 3 -\tMODE TCHAT. \t\t Actif\n");
		} catch (IOException e) {
			System.err.println("Le serveur ne répond plus ");
		}
	}
}
