package org.cloudbus.cloudsim.examples.test;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

public class TestRestMsg {
	@Test
	public void testGetMsg(){
		 DefaultHttpClient httpclient = new DefaultHttpClient();
		    try {
		      // specify the host, protocol, and port
		      HttpHost target = new HttpHost("localhost", 8161, "http");
		      
		      // specify the get request
		      HttpGet getRequest = new HttpGet("/demo/message/mytopic?readTimeout=10000&destination=mytopic&type=topic&clientId=consumerA");

		      System.out.println("executing request to " + target);

		      HttpResponse httpResponse = httpclient.execute(target, getRequest);
		      HttpEntity entity = httpResponse.getEntity();

		      System.out.println("----------------------------------------");
		      System.out.println(httpResponse.getStatusLine());
		      org.apache.http.Header[] headers =  httpResponse.getAllHeaders();
		      for (int i = 0; i < headers.length; i++) {
		        System.out.println(headers[i]);
		      }
		      System.out.println("----------------------------------------");

		      if (entity != null) {
		        System.out.println(EntityUtils.toString(entity));
		      }

		    } catch (Exception e) {
		      e.printStackTrace();
		    } finally {
		      // When HttpClient instance is no longer needed,
		      // shut down the connection manager to ensure
		      // immediate deallocation of all system resources
		      httpclient.getConnectionManager().shutdown();
		    }
		  }
	
	
	@Test
	public void testUnScribeMsg(){
		 DefaultHttpClient httpclient = new DefaultHttpClient();
		    try {
		      // specify the host, protocol, and port
		      HttpHost target = new HttpHost("localhost", 8161, "http");
		      
		      // specify the get request
		      HttpDelete deleteRequest = new HttpDelete("/demo/message/foo/bar");

		      System.out.println("executing request to " + target);

		      HttpResponse httpResponse = httpclient.execute(target, deleteRequest);
		      HttpEntity entity = httpResponse.getEntity();

		      System.out.println("----------------------------------------");
		      System.out.println(httpResponse.getStatusLine());
		      org.apache.http.Header[] headers =  httpResponse.getAllHeaders();
		      for (int i = 0; i < headers.length; i++) {
		        System.out.println(headers[i]);
		      }
		      System.out.println("----------------------------------------");

		      if (entity != null) {
		        System.out.println(EntityUtils.toString(entity));
		      }

		    } catch (Exception e) {
		      e.printStackTrace();
		    } finally {
		      // When HttpClient instance is no longer needed,
		      // shut down the connection manager to ensure
		      // immediate deallocation of all system resources
		      httpclient.getConnectionManager().shutdown();
		    }
		  }

	
	
	@Test
	public void testSendMsg(){
		 DefaultHttpClient httpclient = new DefaultHttpClient();
		    try {
		      // specify the host, protocol, and port
		      HttpHost target = new HttpHost("localhost", 8161, "http");
		      
		      // specify the get request
		      HttpPost postRequest = new HttpPost("/demo/message/mytopic?type=topic&destination=mytopic&clientId=consumerA&body1=hello");
		      List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		      formparams.add(new BasicNameValuePair("body", "i'm hungry"));
		      formparams.add(new BasicNameValuePair("param2", "value2"));
		      UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
		      postRequest.setEntity(entity);
		      System.out.println("executing request to " + target);

		      HttpResponse httpResponse = httpclient.execute(target, postRequest);
		      HttpEntity entity1 = httpResponse.getEntity();

		      System.out.println("----------------------------------------");
		      System.out.println(httpResponse.getStatusLine());
		      org.apache.http.Header[] headers =  httpResponse.getAllHeaders();
		      for (int i = 0; i < headers.length; i++) {
		        System.out.println(headers[i]);
		      }
		      System.out.println("----------------------------------------");

		      if (entity != null) {
		        System.out.println(EntityUtils.toString(entity));
		      }

		    } catch (Exception e) {
		      e.printStackTrace();
		    } finally {
		      // When HttpClient instance is no longer needed,
		      // shut down the connection manager to ensure
		      // immediate deallocation of all system resources
		      httpclient.getConnectionManager().shutdown();
		    }
		  }
}
