# serverless
Lambda Application for forgot password reset through AWS Lambda Simple Email Service(SES) to CI/CD stack

# CSYE 6225 - Summer 2020

## Student Info:

Name - Sridhar Prasad Panneerselvam
NU ID - 001347216
Mail - panneerselvam.s@northeastern.edu

Technology Stack
Java 11, Spring Boot 4.6.2, DynamoDB, Terraform

User stories for Web Application REST API call:

User should be able to request link for password reset in the system via email.
As a user, I want to the link to update the password sent to me in an email.
HTTP request being stateless should return right away and not wait for processing to finish. Processing of the request should be handled in background. The password token should exist in a 15 minute window.
A separate thread in the application will accept the username and trigger a link with a token to a SNS topic which inturn will trigger AWS Lambda function. Email will be sent to the user from the Lamdba function.
Lambda Function specification:
Lambda function will be invoked by the SNS notification.
Lambda function is responsible for sending email to the user.
As a user, I should be able to only have 1 reset token active in database (DynamoDB) at a time.
As a user, I expect the password reset token (value) and username (key) be stored in DynamoDB with TTL of 15 minutes.
As a user, I expect the password reset token to expire after 15 minutes if it is not used by then.
As a user, I expect the to receive the password reset link via email.
As a user, if I make multiple requests to reset password when there is a active token in the database, I should only receive 1 email.
CI/CD Pipeline for Lambda Function:
Every commit to the serverless repository should trigger a CircleCI build and deployment of your updates function to AWS Lambda.