package com.cloud.lambdaserverless;


import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.*;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
public class ResetPasswordEmail implements RequestHandler<SNSEvent, Object>
{

	static final String FROM = "no-reply@" + "prod.sridharprasadpanneerselvam.me";

	static final String CONFIGSET = "ConfigSet";


	static final String TEXTBODY = "This email was sent through Amazon SES " + "using the AWS SDK for Java.";
	
	DynamoDB dynamoDB;

    //private String domainName= System.getenv("domainName");
	
	public String handleRequest(SNSEvent request, Context context) {
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime());
		
	try {
	    context.getLogger().log("connecting to dynamodb");
	    init();
	    long unitTime = Instant.now().getEpochSecond()+15*60;
	    Table t = dynamoDB.getTable("csye6225");
	    context.getLogger().log("In DynamoDB Table");
	    
		String lambda = request.getRecords().get(0).getSNS().getMessage();
		Item it = t.getItem("id", lambda);
		if(it==null) {
		
            Item item = new Item().withPrimaryKey("id", lambda).withString("token", context.getAwsRequestId()).withNumber("TimeToLive", unitTime);


            context.getLogger().log("processing email");
            t.putItem(item);

            try {
                String sns = request.getRecords().get(0).getSNS().getMessage();
                String token = context.getAwsRequestId();
                AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder.defaultClient();
                SendEmailRequest req = new SendEmailRequest().withDestination(new Destination().withToAddresses(sns)).withMessage(new Message().withBody(new Body().withHtml(new Content().withCharset("UTF-8").withData("Please click on the below link to reset the password<br/>"+ "<p><a href='#'>https://csye6225-prod.sridharprasadpanneerselvam.me/reset?email="+sns+"&token="+token+"</a></p>"))).withSubject(new Content().withCharset("UTF-8").withData("Password Reset Link For webapp"))).withSource(FROM);
                client.sendEmail(req);
                context.getLogger().log ("Email sent!");
            } catch (Exception ex) {
                context.getLogger().log ("Error sending email. Error message: "
                        + ex.getMessage());
            }
		}
	} 
	catch(AmazonServiceException ase){
        context.getLogger().log("Could not complete operation");
        context.getLogger().log("Error Message:  " + ase.getMessage());
        context.getLogger().log("HTTP Status:    " + ase.getStatusCode());
        context.getLogger().log("AWS Error Code: " + ase.getErrorCode());
        context.getLogger().log("Error Type:     " + ase.getErrorType());
        context.getLogger().log("Request ID:     " + ase.getRequestId());
    }
    catch (AmazonClientException ace) {
        context.getLogger().log("Internal error occured communicating with DynamoDB");
        context.getLogger().log("Error Message:  " + ace.getMessage());
    }
    catch(Exception e){
        context.getLogger().log("this is the exception"+e);
    }

      context.getLogger().log("Invocation completed: " + timeStamp);
		
	return null;
	
}
	
	private void init() {
        AmazonDynamoDB DBclient = AmazonDynamoDBClientBuilder.defaultClient();
        dynamoDB = new DynamoDB(DBclient);


    }
    
}

