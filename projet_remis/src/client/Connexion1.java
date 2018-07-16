package client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.json.JsonObject;

public class Connexion1 {
	
	public Socket socket_ = null;
	public JsonObject objJson_ = null;
	public static Thread t1;
	
	public Connexion1(JsonObject json){
		objJson_ = json;
	}
	public Socket getSocketConnexion1(){
		return socket_;
	}
	public JsonObject getObjJsonConnexion1(){
		return objJson_;
	}
	public void Connexion(){
		try {
			//InetAddress adrIPClient = InetAddress.getByName("www.goggle.fr");// Test en fonction de la table de routage.
			InetAddress adrIPClient = InetAddress.getLocalHost();// Test en local "127.0.0.1".
			System.out.println("\n\t\tCOTE CLIENT\n");
			System.out.print("PHASE 1 -\tCONNEXION AU SERVEUR:");
			socket_ = new Socket(adrIPClient,2009); 
			System.out.println("\tConnexion établie avec le serveur.");
			System.out.print("PHASE 2 -\tIDENTIFICATION:");
			
			// Utilisation de Thread() pour gerer la partie connexion.
			t1 = new Thread(new Connexion2(socket_, objJson_));
			t1.start();
		} catch (UnknownHostException e) {
			System.err.println("Impossible de se connecter à l'adresse "+socket_.getLocalAddress());
		} catch (IOException e) {
			System.err.println("Aucun serveur à l'écoute du port "+socket_.getLocalPort());
		}
	}
}
