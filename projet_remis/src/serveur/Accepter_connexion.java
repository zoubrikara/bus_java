package serveur;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

import javax.json.JsonObject;


public class Accepter_connexion implements Runnable{

	private ServerSocket socketserver_ = null;
	private static ArrayList<Socket> socketArray_ = null;
	private static ArrayList<JsonObject> jsonArray_ = null;
	public Thread t1;
	
	public Accepter_connexion(ServerSocket ss){
	 socketserver_ = ss;
	 socketArray_ = new ArrayList<Socket>();
	 jsonArray_ = new ArrayList<JsonObject>();
	}
	
	public void run() {
		try {
			while(true){
				if(socketArray_.size()<= 10){
					socketArray_.add(socketserver_.accept());
					System.out.println("PHASE 2 - \tDETECTION CLIENT.");
					
					t1 = new Thread(new Identification(socketArray_, jsonArray_, socketArray_.size()-1) );
					t1.start();
				}
				else
					System.err.println("Echec car la limite de clients connectés est déjà atteinte!");
			}
		} catch (IOException e) {
			System.err.println("Erreur serveur");
		}
	}
}
