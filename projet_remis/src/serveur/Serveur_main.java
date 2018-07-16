package serveur;

import java.io.IOException;
import java.net.ServerSocket;

public class Serveur_main {

	public static ServerSocket ss = null;
	public static Thread t;
	
	public static void main(String []args) {
		System.out.println("\n\t\tCOTE SERVEUR\n\n");
		try {
			ss = new ServerSocket(2009);
			System.out.println("PHASE 1 - \tOUVERTURE PORT. \t\tLe serveur est à l'écoute du port "+ss.getLocalPort());
		
			//Utilisation du Thread pour Accepter des connexions
			t = new Thread(new Accepter_connexion(ss));
			t.start();
		
		} catch (IOException e) {
			System.err.println("Le port "+ss.getLocalPort()+" est déjà utilisé !");
		}
	}
}
