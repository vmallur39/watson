package myPackage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;


public class Watson {

	public static void main(String[] args) throws MalformedURLException, IOException {
		
		String clientId = "4d23785f-a69f-4247-a569-ff888fb63924";
		String clientSecret = "yM5cA4vU4hT0oB7eU0uJ3iL4dQ2dP5wT8rE3sI2rN8rN7uB2aO";
		String accessToken;
		
		String fileName = "Test";
		String fileDescription = "Test file";
		String fileLocation = "C:/CSV/SampleCSVFile_2kb.csv";
		
		String authCodeUrl = "https://api.ibm.com/watsonanalytics/run/clientauth/v1/auth" + "?"
			+ "response_type=code&client_id=" + clientId + "&scope=userContext&redirect_uri=https%3A%2F%2Flocalhost:9080&state=12345";
		
		System.out.println(authCodeUrl);
		// Enter this URL in a browser and paste the authorization code below.		
		
		String authCode = "fsxQn6xvXjLzKPus6wPxq3mgzND%2BBtolomKx6KaDL%2FRTkDNVWiW7B05hrYhJGuauvQ5GSM2OMuyfi8PAhRdEcjk7K8oe0fB9N6%2Fcmc2cZg92sq2gYtrPK4nxrYbQGPTfU9vDtLZx%2FLy%2FK9CdU7YDvNQXN2Xs73ddaQRjxAwbz0t70%2FjbBKLCfqP5iLqnuiOPWAlqKBnJ%2B26psNfTkyGG9qC1pHHEX6Fyui7kBL0U5iY%2FqiWsXjOlG7LR4Fb3k6kJJx%2BmizLvSw4ZgUEst%2B8GG%2BwkMe24WM6HAMA8NX4LRessXy1CIdctcw%3D%3D";
			
		// HTTP headers
		HttpPost httpPost = new HttpPost("https://api.ibm.com/watsonanalytics/run/oauth2/v1/token");
		httpPost.addHeader("X-IBM-Client-Id", clientId);
		httpPost.addHeader("X-IBM-Client-Secret", clientSecret);		
		
		// HTTP body
		String body = "grant_type=authorization_code&code=" + authCode;		
		HttpEntity entity = new ByteArrayEntity(body.getBytes("UTF-8"));
		httpPost.setEntity(entity);
		
		HttpClient httpClient = HttpClientBuilder.create().build();		
		HttpResponse response = httpClient.execute(httpPost);
        
		String result = EntityUtils.toString(response.getEntity());		
		JSONObject jo = new JSONObject(result);		
		try {
			accessToken = jo.getString("access_token");
		}
		catch (Exception e) {
			accessToken = "N/A";
		}
		System.out.println("Access token: " + accessToken);
		
		if (accessToken != "N/A") {
			// Create data set
			httpPost = new HttpPost("https://api.ibm.com/watsonanalytics/run/data/v1/datasets");
			httpPost.addHeader("X-IBM-Client-Id", clientId);
			httpPost.addHeader("X-IBM-Client-Secret", clientSecret);
			httpPost.addHeader("Authorization", "Bearer " + accessToken);
			httpPost.addHeader("Content-Type", "application/json");
			httpPost.addHeader("Accept", "application/json");
			
			body = "{ \"description\" : \"" + fileDescription + "\", \"name\" : \"" + fileName + "\"}";
			entity = new ByteArrayEntity(body.getBytes("UTF-8"));
			httpPost.setEntity(entity);
			
			httpClient = HttpClientBuilder.create().build();
			response = httpClient.execute(httpPost);
			result = EntityUtils.toString(response.getEntity());		
			
			jo = new JSONObject(result);		
			String datasetId = jo.getString("id");
			
			// Update the data set	
			HttpPut httpPut = new HttpPut("https://api.ibm.com/watsonanalytics/run/data/v1/datasets/" + datasetId + "/content");
			httpPut.addHeader("X-IBM-Client-Id", clientId);
			httpPut.addHeader("X-IBM-Client-Secret", clientSecret);
			httpPut.addHeader("Authorization", "Bearer " + accessToken);			
			
			String csvFile = fileLocation;
			BufferedReader br = null;
			String line = "";
			
			body = "";		
			try {
				br = new BufferedReader(new FileReader(csvFile));
				while ((line = br.readLine()) != null) {
					body += line + "\n";
				    System.out.println(line);
				}
			} 	
			catch (Exception e) {
				System.out.println(e.getMessage());
			}
			
			httpPut.setEntity(new StringEntity(body, ContentType.TEXT_PLAIN));
			
			httpClient = HttpClientBuilder.create().build();
			response = httpClient.execute(httpPut);		
		}		
	}
}