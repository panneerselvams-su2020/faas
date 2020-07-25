package com.cloud;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.UUID;

public class ResetPasswordEmail implements RequestHandler<SNSEvent, Object>{
	static final String domain= System.getenv("domain");
	static final String dynamoTable =System.getenv("dynamo");

	static final String FROM = "no-reply@"+domain;

	static final String CONFIGSET = "ConfigSet";


	
	DynamoDB dynamoDB;

 //private String domainName= System.getenv("domainName");
	
	public String handleRequest(SNSEvent request, Context context) {
	String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime());
	long now = Calendar.getInstance().getTimeInMillis()/1000;
 long TTL = 15 * 60;
 long totalTTL = TTL + now ;
	try {
	context.getLogger().log("trying to connect to dynamodb");
	long ttlDBValue = 0;
	init();
	long unixTime = Instant.now().getEpochSecond()+15*60;
	Table table = dynamoDB.getTable(dynamoTable);
	
	
	String To = request.getRecords().get(0).getSNS().getMessage();
	context.getLogger().log("Request"+To);

	Item item = table.getItem("id", To);
	if (item != null) {
 context.getLogger().log("Checking for timestamp");
 ttlDBValue = item.getLong("TimeToLive");

 }
	if(item==null|| (ttlDBValue < now && ttlDBValue != 0)) {

 Item itemPut = new Item()
 .withPrimaryKey("id", To)
 .withString("token", context.getAwsRequestId())
 .withNumber("TimeToLive", totalTTL);


 context.getLogger().log("inside email");
 table.putItem(itemPut);

 try {
 String TO = request.getRecords().get(0).getSNS().getMessage();
 String token = context.getAwsRequestId();
 AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder.defaultClient();
 SendEmailRequest req = new SendEmailRequest().withDestination(new Destination().withToAddresses(TO))
 .withMessage(new Message().withBody(new Body().withHtml(new Content().withCharset("UTF-8").withData("Please click on the below link to reset the password<br/>"+ "<p><a href='#'>https://"+domain+"/reset?email="+TO+"&token="+token+"</a></p>")))
 .withSubject(
 new Content().withCharset("UTF-8")
 .withData("Password Reset Link for webapp")))
 .withSource(FROM);
 client.sendEmail(req);
 context.getLogger().log ("Email sent!");
 } catch (Exception ex) {
 context.getLogger().log ("The email was not sent. Error message: "
 + ex.getMessage());
 }
	}
	} 
	catch(AmazonServiceException ase){
 context.getLogger().log("Could not complete operation");
 context.getLogger().log("Error Message: " + ase.getMessage());
 context.getLogger().log("HTTP Status: " + ase.getStatusCode());
 context.getLogger().log("AWS Error Code: " + ase.getErrorCode());
 context.getLogger().log("Error Type: " + ase.getErrorType());
 context.getLogger().log("Request ID: " + ase.getRequestId());
 }
 catch (AmazonClientException ace) {
 context.getLogger().log("Internal error occured communicating with DynamoDB");
 context.getLogger().log("Error Message: " + ace.getMessage());
 }
 catch(Exception e){
 context.getLogger().log("this is the exception"+e);
 }

 context.getLogger().log("Invocation completed: " + timeStamp);
	
	return null;
	
}
	
	private void init() {
 AmazonDynamoDB aDBclient = AmazonDynamoDBClientBuilder.defaultClient();
 dynamoDB = new DynamoDB(aDBclient);


 }
 


 }