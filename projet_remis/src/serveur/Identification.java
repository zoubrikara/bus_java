package serveur;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.Socket;
import java.util.ArrayList;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.stream.JsonParsingException;

public class Identification  implements Runnable {

	private ArrayList<Socket> socketArray_ = null;
	private ArrayList<JsonObject> jsonArray_ = null;
	private PrintWriter out_ = null;
	private BufferedReader in_ = null; 
	private String login_ = null;
	public boolean authentifier = false;
	static int cpt = 1,nb_connexion=1, nb_data=0;
	public Thread t2;
	

	public Identification(ArrayList<Socket> sockArray, ArrayList<JsonObject> jsonL, int pos){
		socketArray_ = sockArray;
		jsonArray_ = jsonL;
		login_ = new String();
	}
	

	public void run() {
		try {
			/* Pour communiquer sur (la socket (canal) client-serveur) on utilise 
			 * - "out" pour (ecrire des données vers le Client) 
			 * - "in"  pour (lire des données depuis le Client)
			 */
			in_ = new BufferedReader(new InputStreamReader(socketArray_.get(socketArray_.size()-1).getInputStream())); 
			out_ = new PrintWriter(socketArray_.get(socketArray_.size()-1).getOutputStream());
			JsonObject modif_objJson = null;
			
			System.out.println("PHASE 3 - \tENREGISTREMENT CLIENT.");
			out_.println("...Presentez vous svp.");
			out_.flush();
			
			while(!authentifier){
				String tmp_json_lue = in_.readLine();
				JsonReader in_json = null;
				JsonObject objJson = null;
				
				// On teste les valeur des champs: poster = bien un BOOLEAN
				try {
					in_json = Json.createReader(new StringReader(tmp_json_lue));
					objJson = in_json.readObject();
					in_json.close();
				}catch(JsonParsingException e) {
					out_.println("{\"state\":\"INIT_ERROR\"}");
					out_.flush();
					System.err.println("Mauvaise valeur du champs dans le JSON");
				}
				if(objJson != null) {		
				/* On mémorise les champs de l'objet JSON RECU du client[X],
				 * une fois que le protocol de chaque objet soit respecté
				 */
				if(objValid(objJson)){
					login_ = objJson.getString("name");
					String state = new String(objJson.getString("state")+"_OK");
					boolean poster = objJson.getBoolean("poster");
					/* ID doit être en 3 parties:
					 *  - Une place de connexion [10 000; infinie]
					 *  - Une place connue par le serveur [100; 9 900]
					 *  - Une place connue des données Json [0; 99]
					 * */
					int id = (cpt++)*10000 + (nb_connexion++)*100+ nb_data++;
					
					tmp_json_lue = "{\"id\":"+id+", \"name\":\""+login_+"\", \"data\":[0,0,0], \"state\":\""+state+"\", \"poster\":"+poster+"}";
					JsonReader modif_json = Json.createReader(new StringReader(tmp_json_lue));
					modif_objJson = modif_json.readObject();
					modif_json.close();
					
					out_.println(tmp_json_lue);
					System.out.println("> \""+login_+" "+id+"\" vient de se connecter ");
					authentifier = true;
					jsonArray_.add(modif_objJson);	
				}
				else
					out_.println("{\"state\":\"INIT_ERROR\"}");
					//out_.println("Echec d'authentification, vous ne respectez pas la convention du FRAMAPAD!!!!!");
				out_.flush();
				}
			}
			
			t2 = new Thread(new Chat_ClientServeur(socketArray_, login_, modif_objJson.getInt("id"), jsonArray_));
			t2.start();
		} catch (IOException e) {
			System.err.println("Ce client ne répond pas !");
		}
	}
	
	
	static boolean objValid(JsonObject obj) {
		if(obj.keySet().size() == 3){
			try{
				obj.getString("name");
				String state = obj.getString("state");
				if(state.equals("INIT")){
					obj.getBoolean("poster");
					return true;
				}
			}catch(Exception e){
				return false;
			}
		}
		return false;
		
	}
	
}
