/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.src;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.sqs.model.Message;
import com.iit.db.DBManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.util.Map;

public class WorkerThread implements Runnable {

    //private String command;
    private Message message;
    private AmazonS3 s3;
    private String jobID;

    public int getJobID() {
        return Integer.parseInt(jobID);
    }

    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

    public WorkerThread(Message msg, AmazonS3 s3) {
        this.message = msg;
        this.s3 = s3;
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + " Start. ");
        processCommand();
        downloader();
        System.out.println(Thread.currentThread().getName() + "End.");
    }

    private void processCommand() {
        try {
            System.out.println("  Message " + message);
            System.out.println("    MessageId:     " + message.getMessageId());
            System.out.println("    ReceiptHandle: " + message.getReceiptHandle());
            System.out.println("    MD5OfBody:     " + message.getMD5OfBody());
            System.out.println("    Body:          " + message.getBody());
            setJobID(message.getBody());
            for (Map.Entry<String, String> entry : message.getAttributes().entrySet()) {
                System.out.println("  Attribute");
                System.out.println("    Name:  " + entry.getKey());
                System.out.println("    Value: " + entry.getValue());
            }

            //Thread.sleep(5000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void downloader() {
        try {
            System.out.println(Thread.currentThread().getName() + " Job id : " + getJobID());
            ResultSet rs = DBManager.getResult(getJobID());
            while (rs.next()) {
                System.out.println("Result >>" + rs.getString(5));
                if (rs.getString(6).equalsIgnoreCase("TRANSFERED")) {
                    String bName = rs.getString(4);
                    String key = rs.getString(5);
                    System.out.println("Downloading an object " + bName + " " + key);
                    S3Object object = s3.getObject(new GetObjectRequest(bName, key));
                    s3.getObject(
                            new GetObjectRequest(bName, key),
                            new File("/tmp" + key));
                    
                    invokeCmd("/tmp/" + key);
                }
            }
        } catch (Exception ex) {
            System.err.println(Thread.currentThread().getName() + "Failed to download");
            ex.printStackTrace();
        }
    }

    private String scriptGen(String fileLoc, String type) {
        StringBuffer cmd = new StringBuffer();
        if (type.endsWith("ipad")) {
            cmd.append("sudo HandBrakeCLI -i ").append(fileLoc)
                    .append("-o ").append(fileLoc.replace(".mp4", "_ipad.mp4")).append("ipad -e x264 -q 32 -B 128 -w 800 --loose-anamorphic -O");
        }
        return cmd.toString();
    }

    private void invokeCmd(String fileName) throws IOException {
        String s = null;
        Process p = Runtime.getRuntime().exec(scriptGen(fileName, "ipad"));
        int exitVal = p.exitValue();
        System.out.println("Process exitValue: " + exitVal);
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

        BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

        // read the output from the command
        System.out.println("Here is the standard output of the command:\n");
        while ((s = stdInput.readLine()) != null) {
            System.out.println(s);
        }

        // read any errors from the attempted command
        System.out.println("Here is the standard error of the command (if any):\n");
        while ((s = stdError.readLine()) != null) {
            System.out.println(s);
        }
    }
}
