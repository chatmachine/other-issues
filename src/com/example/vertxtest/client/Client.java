package com.example.vertxtest.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.example.vertxtest.server.Server;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;

public class Client {
	private static final Logger log = LogManager.getLogger(Client.class);
	public void launch(){
		Vertx clientVertx = Vertx.vertx();
		HttpClientOptions httpClientOptions = new HttpClientOptions();
		httpClientOptions.setMaxPoolSize(1);
		httpClientOptions.setMaxWaitQueueSize(3);
		HttpClient httpClient = clientVertx.createHttpClient(httpClientOptions);
		
		httpClient.get(Server.PORT, "localhost", "/").setTimeout(3000).exceptionHandler(t->{
			log.info("Request 1 saw exception {}", t.getMessage());
		}).handler(resp->{
			log.info("Request 1 request recieved response.");
		}).end();
		
		try {
			Thread.sleep(500); //give some time to let the first request start
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		for(int i=0; i<3; i++){ //these 3 requests will initially fill up the wait queue. 1 should be sent after 3 seconds (when the first request above ends via timeout), and 2 should remain in the wait queue.
			final int reqNum = i+2;
			httpClient.get(Server.PORT, "localhost", "/").setTimeout(5000).exceptionHandler(t->{
				log.info("Request {} saw exception {}", reqNum, t.getMessage());
			}).handler(resp->{
				log.info("Request {} recieved response.");
			}).end();
		}
		//After 3+5 = 7 seconds, the wait queue size should reduce to 2.
		try {
			Thread.sleep(10000); //wait for 10 seconds, to allow for some leniency. The wait queue should have at least one slot open by now.
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		log.info("by this time, you should have seen at one timeout exception in the logs above, thereby freeing up one slot in the wait queue");
		log.info("this new request should not get a connection-pool-busy exception");
		httpClient.get(Server.PORT, "localhost", "/").setTimeout(10000).exceptionHandler(t->{
			log.info("Request 5 saw exception {}", t.getMessage());
		}).handler(resp->{
			log.info("Request 5 recieved response. HTTP ", resp.statusCode());
		}).end();
		try {
			Thread.sleep(30000); //wait for 30 seconds, and check again
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		httpClient.get(Server.PORT, "localhost", "/").setTimeout(10000).exceptionHandler(t->{
			log.info("Request 6 saw exception {}", t.getMessage());
		}).handler(resp->{
			log.info("Request 6 recieved response. HTTP ", resp.statusCode());
		}).end();
	}
}
