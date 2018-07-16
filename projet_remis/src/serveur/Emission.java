package serveur;

import java.io.PrintWriter;

import javax.json.JsonObject;


public class Emission implements Runnable {

	private PrintWriter out_;
	private JsonObject objJson_ = null;
	
	public Emission(PrintWriter out, JsonObject obj) {
		this.out_ = out;
		objJson_ = obj;
	}
	
	public void run() {
		out_.println(objJson_);
		out_.flush();
		try{
			System.out.println("Message from "+objJson_.getString("name")+" transmitted");
		}catch(NullPointerException e){
			System.err.println("Message transmitted, but value: "+e.getMessage());
		}
	}
}
