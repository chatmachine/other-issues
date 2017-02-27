package com.example.vertxtest.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.example.vertxtest.server.Server;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;

public class Client {
	private static final Logger log = LogManager.getLogger(Client.class);
	public void launch(){
		Vertx clientVertx = Vertx.vertx();
		HttpClientOptions httpClientOptions = new HttpClientOptions();
		httpClientOptions.setMaxPoolSize(1);
		httpClientOptions.setMaxWaitQueueSize(3);
		HttpClient httpClient = clientVertx.createHttpClient(httpClientOptions);
		
		//This first request will block the pool for 15 seconds
		final HttpClientRequest req1 = httpClient.get(Server.PORT, "localhost", "/").setTimeout(15000).exceptionHandler(t->{
			log.info("Request 1 saw exception {}", t.getMessage());
		});
		req1.handler(resp->{
			log.info("Request 1 request recieved response.");
			req1.setTimeout(15000);
		});
		req1.end();
		
		
		try {
			Thread.sleep(1000); //give some time to let the first request start
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		
		for(int i=0; i<3; i++){ //these 3 requests will fill up the wait queue for 5 seconds.
			final int reqNum = i+2;
			final HttpClientRequest req2 =
			httpClient.get(Server.PORT, "localhost", "/").setTimeout(5000).exceptionHandler(t->{
				log.info("Request {} saw exception {}", reqNum, t.getMessage());
			});
			req2.handler(resp->{
				log.info("Request {} recieved response.", reqNum);
				req2.setTimeout(5000);
			}).end();
		}
		//1+5 = 6 seconds, all three waiting requests should have timed out.
		try {
			Thread.sleep(10000); //wait for 10 seconds, to allow for some leniency. The wait queue should have timed out by now.
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		log.info("by this time, you should have seen three timeout exceptions in the logs above, thereby freeing up all three slots in the wait queue");

		log.info("Sending a new request. This should not get a connection-wait-queue-size-maxed out exception");
		HttpClientRequest req5 = httpClient.get(Server.PORT, "localhost", "/").setTimeout(10000).exceptionHandler(t->{
			log.info("Request 5 saw exception {}", t.getMessage());
		});
		req5.handler(resp->{
			log.info("Request 5 recieved response. HTTP ", resp.statusCode());
			req5.setTimeout(1000);
		}).end();
		try {
			Thread.sleep(30000); //wait for 30 seconds, and check again. By this time, the connection pool itself should have got freed up.
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		log.info("by this time, the connection pool should be free. This request should go through");
		httpClient.get(Server.PORT, "localhost", "/").setTimeout(10000).exceptionHandler(t->{
			log.info("Request 6 saw exception {}", t.getMessage());
		}).handler(resp->{
			log.info("Request 6 recieved response. HTTP ", resp.statusCode());
		}).end();
	}
}
