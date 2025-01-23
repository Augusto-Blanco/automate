package net.jmb.cryptobot.spring.interceptor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import net.jmb.cryptobot.service.CommonService;

public class LoggingRequestInterceptor extends CommonService implements ClientHttpRequestInterceptor {

	    @Override
	    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
	        traceRequest(request, body);
	        ClientHttpResponse response = execution.execute(request, body);
	        traceResponse(response);
	        return response;
	    }

	    protected void traceRequest(HttpRequest request, byte[] body) {
	    	String bodyString;
			try {
				bodyString = new String(body, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				bodyString = new String(body);
			}
	        getLogger().info("<=== Request ===>");
	        getLogger().info("URI           : {}", request.getURI());
	        getLogger().info("Method        : {}", request.getMethod());
	        getLogger().info("Headers       : {}", request.getHeaders() );
	        getLogger().info("Request body  : {}", bodyString);
	    }

		protected void traceResponse(ClientHttpResponse response) {
			getLogger().info("<=== Response ===>");
			try {
				getLogger().info("Status code   : {}", response.getStatusCode());
				getLogger().info("Status text   : {}", response.getStatusText());
				getLogger().info("Headers       : {}", response.getHeaders());
				if (!response.getStatusCode().isError()) {
					StringBuilder inputStringBuilder = new StringBuilder();
					BufferedReader bufferedReader = new BufferedReader(
							new InputStreamReader(response.getBody(), "UTF-8"));
					String line = bufferedReader.readLine();
					while (line != null) {
						inputStringBuilder.append(line);
						inputStringBuilder.append('\n');
						line = bufferedReader.readLine();
					}
					getLogger().info("Response body : {}", inputStringBuilder.toString());
				}
			} catch (IOException e) {
				getLogger().error("Erreur de lecture de la réponse", e);
			}
		}



}
