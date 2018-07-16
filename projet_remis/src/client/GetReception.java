package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.Socket;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

public class GetReception {
	

	private static Socket socket_;
	private static BufferedReader in_;
	private static String message_ = null;
	
	
	public GetReception(BufferedReader in,Socket s){
		socket_ = s;
		in_ = in;
	}
	public String getMsg(){
		return message_;
	}
	public void getterReception() {
		try { // Une pause sinon (selon les pc ) le msg d'erreur arrive plus tard que la saisie...
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if(! socket_.isClosed()){
		try {
			message_ = in_.readLine();
			if(!message_.isEmpty()){
				System.out.print("\nServeur dit > " +message_);
				
				try{
					JsonReader in_json = Json.createReader(new StringReader(message_));
					JsonObject objGet = in_json.readObject();
					in_json.close();
					if(objGet.getString("state").equals("DISCONNECT")) {
						System.out.print("\n> etat de connexion: ");
						System.out.println(socket_.isClosed()?"fermé":"ouverte");
						socket_.close();
						System.out.print("> etat de connexion: ");
						System.out.println(socket_.isClosed()?"fermé":"ouverte");
					}
				}catch(NullPointerException e){
					;
				}
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
		}
	}
}
