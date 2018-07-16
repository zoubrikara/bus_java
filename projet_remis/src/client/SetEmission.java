package client;

import java.io.PrintWriter;
import java.io.StringReader;
import java.net.Socket;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

public class SetEmission {

	//private static Socket socket_;
	private static PrintWriter out;
	private static JsonObject jsonObj_ = null, jsonSend = null;
	
	public SetEmission(Socket sock, PrintWriter o, JsonObject objJson, JsonObject json ){
		out = o;
		jsonObj_ = objJson;
		jsonSend = json;
		//socket_ = sock;
	}
	public JsonObject getObjJsonEmission(){
		return jsonObj_;
	}
	
	public void setterEmission() {
		try { // Une pause sinon (selon les pc ) le msg d'erreur arrive plus tard que la saisie...
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		String name = jsonObj_.getString("name");
		String state = jsonObj_.getString("state");
		boolean poster = jsonObj_.getBoolean("poster");
		int id = jsonObj_.getInt("id");
		
		System.out.print("\n"+jsonObj_.getString("name")+"["+jsonObj_.getInt("id")+"]: ");
		String msg = jsonSend.toString();//sc.nextLine();
			
		if(msg.equals("{\"state\":\"DISCONNECT\"}")){
			state = "DISCONNECT";
			StringBuffer str = new StringBuffer();
			str.append("{ \"data\":[0,0,0], \"name\":\""+name+"\", \"id\":"+id+", \"poster\":"+poster+", \"state\":\""+state+"\"}");
			JsonReader in_json = Json.createReader(new StringReader(str.toString()));
			jsonObj_ = in_json.readObject(); // On cree un objet depuis le Json recu
			in_json.close();
			out.println(str.toString());
			out.flush();
			/*
			try {
				socket_.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/
		}
		else if(msg.length()>6 && msg.substring(0, 9).equals("{\"data\":[")){
			try{
				JsonReader modif_json = Json.createReader(new StringReader(msg));
				if(!modif_json.readObject().isEmpty()){ // On cree un objet depuis le Json recu
					modif_json.close();
					StringBuffer str = new StringBuffer(msg);
					if(str.length()>3){
						str.replace(str.length()-1, str.length(), ",");// on retire '}'
						str.append(" \"name\":\""+name+"\", \"id\":"+id+", \"poster\":"+poster+", \"state\":\""+state+"\"}");
						JsonReader in_json = Json.createReader(new StringReader(str.toString()));
						jsonObj_ = in_json.readObject(); // On cree un objet depuis le Json recu
						in_json.close();
						out.println(str.toString());
						out.flush();
					}
				}
			}catch(Exception e){
				System.err.println("Error entry, only double please! not: "+e.getMessage());
			}
		}
		else if(msg.length()>6 && msg.substring(0, 11).equals("{\"getinfo\":")){
			JsonReader modif_json = Json.createReader(new StringReader(msg));
			if(!modif_json.readObject().isEmpty()){ // On cree un objet depuis le Json recu
				modif_json.close();
				StringBuffer str = new StringBuffer(msg);
				str.replace(str.length()-1, str.length(), ",");// on retire '}'
				str.append(" \"name\":\""+name+"\", \"id\":"+id+", \"poster\":"+poster+", \"state\":\""+state+"\"}");
				out.println(str.toString());
				out.flush();
			}
		}
		else
			System.err.println("Error entry");
	}

}
