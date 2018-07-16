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
import javax.json.stream.JsonParsingException;

public class API {
	
	
	
	
	public static Socket socket_ = null;
	public static JsonObject objJson_ = null;
	public static PrintWriter out = null;
	public static BufferedReader in = null;
	public static String s[] = {"gps", "gyroscope","arduno","rasbery"};//arduno, ...
	public static String mesInfos = "{\"name\":\"arduno\", \"state\":\"INIT\", \"poster\":false}";
	
	
	
	/**
	 * This function establishes the connection between the client and the server.
	 */
	public static void connexion() {
		Connexion1 maConnexion = new Connexion1(objJson_);
		maConnexion.Connexion();
		socket_ = maConnexion.getSocketConnexion1();
	}
	
	/**
	 * This function is used to receive data from a device.
	 * @param name is the desired device, in String.
	 * @return a string received from the server, this string will be an table of object.
	 */
	public static String getter(String name) {
		StringBuffer str = new StringBuffer();
		if(!socket_.isClosed() && !objJson_.getString("state").equals("DISCONNECT")){
			try{
				String n = "{\"getinfo\":\""+name+"\"}";
				JsonReader in_json = Json.createReader(new StringReader(n));
				JsonObject objGet = in_json.readObject(); // On cree un objet depuis le Json recu
				in_json.close();
				str.append(put(objGet));
			}catch(Exception e){
				System.err.println("Une erreur dans la lecture getter()");
			}
		}
		return str.toString();
	}
	
	/**
	 * This function makes it possible to correctly make changes to our data saved in ObjJson 
	 * by sending a request to the server.
	 * @param json is the element of the request that will be sent to the server.
	 * @return a string received from the server.
	 */
	public static String put(JsonObject json) {
		StringBuffer str = new StringBuffer();
		if(!socket_.isClosed() && !objJson_.getString("state").equals("DISCONNECT")){
			try {
				out = new PrintWriter(socket_.getOutputStream());
				in = new BufferedReader(new InputStreamReader(socket_.getInputStream()));
				SetEmission mesEnvois = new SetEmission(socket_,out, objJson_, json);
				GetReception mesLectures = new GetReception(in, socket_);
				mesEnvois.setterEmission();
				objJson_ = mesEnvois.getObjJsonEmission();
				mesLectures.getterReception();
				str.append(mesLectures.getMsg()); // CONTIENT LES INFORMATIONS RENVOYEES PAR LE SERVEUR
			} catch (IOException e) {
				System.out.println("Cette socket est surement fermé!!"+e.getMessage());
			}
		}
		return str.toString();
	}
	
	/**
	 * This function allows you to disconnect correctly by sending a request to the server.
	 */
	public static void deconnexion() {
		try{
			String n = "{\"state\":\"DISCONNECT\"}";
			JsonReader in_json = Json.createReader(new StringReader(n));
			JsonObject objGet = in_json.readObject(); 
			in_json.close();
			System.out.println(put(objGet));
		}catch(Exception e){
			System.err.println("Une erreur dans la lecture deconnexion()");
		}
	}
	
	
	
	
	public static void main(String args[]) {
		
		// FORMAT ACCEPTABLE :
		// {"name":"gyroscope", "state":"INIT", "poster":false}     	// Quand vous voulez ETABLIR une connexion
		// {"data":[1, 2, 3]}											// Quand vous voulez MODIFIER vos donnée
		// {"getinfo":"gyroscope"}										// Quand vous voulez AVOIR des infos sur un peripherique
		// {"state":"DISCONNECT"}										// Quand vous voulez vous DECONNECTER
		
		
		try{
			JsonReader in_json = Json.createReader(new StringReader(mesInfos));
			objJson_ = in_json.readObject(); // On cree un objet depuis le Json recu
			in_json.close();
			
			
			/* I -  ETAPE CONNEXION
			 * 1 - APPEL A LA FONCTION CONNEXION
			 * 2 - AFFICHAGE DES INFOS VENANT DU SERVEUR
			 */
			connexion();
			try { // Une pause le temps de l'initialisation...
				Thread.sleep(800);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			objJson_ = Connexion2.objJson_;
			while(!socket_.isClosed()){
					System.out.println("Bienvenue \""+objJson_.getString("name")+"\" voila vos informations:");
				System.out.println("\tName   : "+objJson_.getString("name"));
				System.out.println("\tIdent  : "+objJson_.getInt("id"));
				System.out.println("\tState  : "+objJson_.getString("state"));
				System.out.println("\tPoster : "+objJson_.getBoolean("poster"));
				
				
				/* II - ETAPE PUT()
				 * 1- Quand on veut modifier nos valeurs X Y Z 
				 * 2- On simule plusieurs requette au serveur 
				 * */
				System.out.println("\n*************  PARTIE SETTER ******************\n");
				int x = 44, y=77, z=66;
				while(z<100){
					String send = "{\"data\":["+x+","+y+","+z+"]}";
					JsonReader in_jsonSend = Json.createReader(new StringReader(send));
					JsonObject objJsonSEnd = in_jsonSend.readObject(); // On cree un objet depuis le Json recu
					in_jsonSend.close();
					put(objJsonSEnd);
					z +=10;
				}
				
				
				
				/* III - ETAPE GETTER()
				 * 1- Quand on veut récuperer les données du peripherique s[i] ainsi: 
				 * */

				System.out.println("\n*************  PARTIE GETTER ******************\n");
				for(int i=0; i<s.length; i++){
					getter(s[i]);
				}
				
				
				
				/* IV - ETAPE DECONNEXION().
				 * En attendant la deconnexion on tourne a l'infinie tant que la connexion n'est pas close.*/
				try { // Une pause avant de quitter A EFFACER PAR LES CLIENTS QUI FERONT EUX MEME LEURS MAIN()
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println("\n*************  PARTIE DECONNEXION ******************\n");
				deconnexion();
				try { // Une pause qui permet d'initialiser la deconnexion PEUT ETRE A RETIRER PAR LES CLIENTS
					Thread.sleep(400);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			}
			System.out.println("Vous êtes deconnecté. A bientot sur notre serveur!");
		}catch(JsonParsingException e) {
			System.err.println("Erreur: {\"state\":\"INIT_ERROR\"} \t\t Mauvaise valeur du champs dans le JSON");
		}
			
	}
}
