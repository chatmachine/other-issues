package com.example.vertxtest;

import com.example.vertxtest.client.Client;
import com.example.vertxtest.server.Server;

public class Launcher {
	public static void main(String[] args) {
		Server server = new Server();
		Client client = new Client();
		
		server.launch();
		client.launch();
	}

}
