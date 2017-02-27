package com.example.vertxtest.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;

public class Server {
	private static final Logger log = LogManager.getLogger(Server.class);
	public static final int PORT = 8282;
	public void launch(){
		Vertx serverVertx = Vertx.vertx();
		HttpServer httpServer = serverVertx.createHttpServer();
		httpServer.requestHandler(req -> {
			log.info("Got request {} {}", req.method(), req.absoluteURI());
			req.response().setChunked(true);
			req.response().write("Hi");
			log.info("Not ending the response, so that the client is left waiting.");
		});
		httpServer.listen(PORT, res -> {
			if(res.failed()){
				log.error("Couldn't start server. ", res.cause());
			}else{
				log.info("Server started on port {}", PORT);
			}
		});
	}
}
