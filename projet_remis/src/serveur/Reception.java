package serveur;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.ListIterator;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;


public class Reception implements Runnable{

	private ArrayList<Socket> socketArray_ = null;
	private ArrayList<JsonObject> jsonArray_ = null;
	private BufferedReader in_ = null;
	private PrintWriter out_ = null;
	private String message_ = null;
	private int pos_;
	
	public Reception(BufferedReader in, PrintWriter out, ArrayList<JsonObject> json, ArrayList<Socket> sockArray, int pos) {
		in_ = in;
		out_ = out; 
		jsonArray_= json;
		socketArray_ = sockArray;
		pos_ = pos;
	}
	
	public String getMsg(){
		return message_;
	}
	public void run() {
		while(!socketArray_.isEmpty() && !socketArray_.get(pos_).isClosed()){
			try {
				message_ = in_.readLine(); 
				if(!message_.isEmpty()){
					if(message_.contains("{")){
						JsonReader in_json = Json.createReader(new StringReader(message_));
						JsonObject objJson = in_json.readObject();
						in_json.close();
						
						if(validFormat(objJson)){
							traiter(jsonArray_, socketArray_, out_, in_, objJson); 
							FichierLog.writeLog(message_);
						}
						else {
							out_.println("INIT_ERROR");
							out_.flush();
						}
					}
					else{
						out_.println("WRONG_ENTRY");
						out_.flush();
					}
				}
				else
					continue;
			} catch (IOException e) {
				;
			}catch(NullPointerException e){
				;
			}
		}
	}
	
	public boolean validFormat(JsonObject obj){
		/* Verification que les données transmises sont correct
		 * - Y a t'il 5 arguments dans chaque msg recu
		 * - Les données envoyés provient t'ils du bon client (grace a l'ID)
		 * */
		int number_json = (obj.getInt("id")%100) ;
		int number_socket = ((obj.getInt("id")%10000)/100)-1;
		if(obj.keySet().size() == 5){
			try{
				if(socketArray_.get(number_socket).isConnected()){
					String str1 = jsonArray_.get(number_json).getString("name");
					String str2 = obj.getString("name");
					if(str1.equals(str2)){
						try{
							return (obj.getJsonArray("data").size() == 3);
						}catch(Exception e){
							System.err.println("data: "+e.getMessage());
						}
						try{
							String s = obj.getString("getinfo");
							return !s.isEmpty();
						}catch(Exception e){
							System.err.println("getInfo: "+e.getMessage());
						}
					}
				}
			}catch(Exception e){
				System.err.println("Erreur lecture arguments: "+e.getMessage());
				return false;
			}
		}
		return false;
	}
	
	public void traiter(ArrayList<JsonObject> jsonArr, ArrayList<Socket> socketArray, PrintWriter out, BufferedReader in, JsonObject objJson){
		int number_json = (objJson.getInt("id")%100) ;
		int number_socket = ((objJson.getInt("id")%10000)/100)-1;
		
		// Difference entre DATA et GETINFO
		int val = -1;
		String information=null;
		try{
			information = objJson.getString("getinfo");
			val = 1;
		}catch(Exception e){
			val=0;
		}
		
		/* PROCEDURE DE DECONNEXION,
		 * ON FERME LA SOCKET ET ON MET A JOUR LES DONNEES. 
		 * */
		if(objJson.getString("state").equals("DISCONNECT")) { 
			jsonArr.set(number_json, objJson);
			out.println(jsonArr.get(number_json));
			out.flush();
			System.out.println("> \""+objJson.getString("name")+"\"Demande sa deconnexion !!");
			System.out.print("> \""+objJson.getString("name")+"\" etat de connexion: ");
			System.out.println(socketArray.get(number_socket).isClosed()?"fermé":"ouverte");
			try {
				socketArray.get(number_socket).close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.print("> \""+objJson.getString("name")+"\" etat de connexion: ");
			System.out.println(socketArray.get(number_socket).isClosed()?"fermé":"ouverte");
			System.out.println("> \""+objJson.getString("name")+"\" est déconnecté!! \n\n");
		}
		else{
			switch(val){
			case 0:
				// On met a jour la données du TABLEAU JSON
				jsonArr.set(number_json, objJson);
				if(!jsonArray_.get(number_json).getBoolean("poster")){
					Thread t333 = new Thread(new Emission(out, jsonArray_.get(number_json)));
					t333.start();
				}
				System.out.println("\""+objJson.getString("name")+"\" ["+ objJson.getInt("id") +"] dit: \t"+message_);
				break;
			case 1:
				// Si on veut renvoyé les information d'un peripherique
				System.out.println("\""+objJson.getString("name")+"\" veut les infos de: \""+information+"\"");
				ListIterator<JsonObject> itr2 = jsonArray_.listIterator();
				boolean v = false;
				StringBuffer str = new StringBuffer();
				str.append("{\""+information+"\":[");
				while(itr2.hasNext()){
					JsonObject objRead = itr2.next();
					if(objRead.getString("name").equals(information)){
						/*   FONCTIONNEL Si on veut trier les infos a envoyer genre le nom et data seulement...
						int tab[] = new int[3], i=0;
						JsonArray ref = objRead.getJsonArray("data");
						for(JsonValue v: ref)
							tab[i++]= Integer.parseInt(v.toString());
						String r = "{\"data\":["+tab[0]+", "+tab[1]+", "+tab[2]+"], \"name\":\""+objRead.getString("name")+"\"}";
						
						
						JsonReader in_json = Json.createReader(new StringReader(r));
						JsonObject obj = in_json.readObject();
						in_json.close();
						*/
						v=true;
						str.append(objRead).append(",");
					}
				}
				if(v){
					str.replace(str.length()-1, str.length(), "]");// on retire ','
					str.append("}");
					JsonReader in_json = Json.createReader(new StringReader(str.toString()));
					JsonObject objsend = in_json.readObject();
					in_json.close();
					
					Thread t3 = new Thread(new Emission(out, objsend));
						t3.start();
				}
				else{
					str.append("{\"").append(information).append("\":\"-inexistant-\"}]}");
					JsonReader in_json = Json.createReader(new StringReader(str.toString()));
					JsonObject objJsonNon = in_json.readObject();
					in_json.close();
					Thread t3 = new Thread(new Emission(out, objJsonNon));
					t3.start();
				}
				break;
			default:
				System.err.println("Une erreur est detecté dans le traitement d'information\n");
				break;
			}
		}
	}
}
