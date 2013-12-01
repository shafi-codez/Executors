/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.src;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.ListQueuesResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.iit.db.DBManager;
import com.util.Constants;
import com.src.WorkerThread;
import java.io.File;
import java.io.FileInputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author shafi
 */
public class queue {

    static Logger log = Logger.getLogger(queue.class.getName());

    public static void main(String[] s) {
        AmazonSQS sqs = new AmazonSQSClient(new ClasspathPropertiesFileCredentialsProvider());
        AmazonS3 s3 = new AmazonS3Client(new ClasspathPropertiesFileCredentialsProvider());

        log.info("===========================================");
        log.info("Getting Started with Amazon SQS");
        log.info("===========================================\n");

        try {
            String myQueueUrl = Constants.jobQueue;

            // Send a message
            System.out.println("Sending a message to MyQueue.\n");
            sqs.sendMessage(new SendMessageRequest(myQueueUrl, "3"));
            sqs.sendMessage(new SendMessageRequest(myQueueUrl, "2"));
            sqs.sendMessage(new SendMessageRequest(myQueueUrl, "1"));
            //sqs.changeMessageVisibility();

            ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(myQueueUrl).
                    withMaxNumberOfMessages(Constants.TaskPerWorker);

            System.out.println("Receiving messages from MyQueue.\n");

            List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
            System.out.println("Message Size " + messages.size());
            ExecutorService executor = Executors.newFixedThreadPool(Constants.TaskPerWorker);

            while (messages.size() > 0) {
                for (Message message : messages) {
                    Runnable worker = new WorkerThread(message);
                    executor.execute(worker);
                    //WorkerThread task = new WorkerThread(message);
                }
                System.out.println();

                // Delete a message
                System.out.println("Deleting a message.\n");
                String messageRecieptHandle = messages.get(0).getReceiptHandle();
                sqs.deleteMessage(new DeleteMessageRequest(myQueueUrl, messageRecieptHandle));

                executor.shutdown();
                while (!executor.isTerminated()) {
                }
                System.out.println("Finished all threads");
            }


            System.out.println();
            ResultSet rs = DBManager.getResult(3);
            while (rs.next()) {
                System.out.println("Result >>" + rs.getString(5));
                if (rs.getString(6).equalsIgnoreCase("PENDING")) {
                    String bName = rs.getString(4);
                    String key = rs.getString(5);
                    System.out.println("Downloading an object " + bName + " " + key);
                    S3Object object = s3.getObject(new GetObjectRequest(bName, key));
                    s3.getObject(
                            new GetObjectRequest(bName, key),
                            new File("E:\\lunatmp\\" + key));

                }
            }



        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it "
                    + "to Amazon SQS, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with SQS, such as not "
                    + "being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        } catch (Exception ex) {
            Logger.getLogger(queue.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String scriptGen(String fileLoc, String type) {
        StringBuffer cmd = new StringBuffer();
        if (type.endsWith("ipad")) {
            cmd.append("sudo HandBrakeCLI -i ").append(fileLoc)
                    .append("-o ").append(fileLoc).append("ipad -e x264 -q 32 -B 128 -w 800 --loose-anamorphic -O");
        }
        return cmd.toString();
    }
}
