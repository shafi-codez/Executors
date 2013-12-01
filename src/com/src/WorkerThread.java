/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.src;

import com.amazonaws.services.sqs.model.Message;
import java.util.Map;

public class WorkerThread implements Runnable {

    //private String command;
    private Message message;

    public WorkerThread(Message msg) {
        this.message = msg;
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + " Start. ");
        processCommand();
        System.out.println(Thread.currentThread().getName() + "End.");
    }

    private void processCommand() {
        try {
            System.out.println("  Message " + message);
            System.out.println("    MessageId:     " + message.getMessageId());
            System.out.println("    ReceiptHandle: " + message.getReceiptHandle());
            System.out.println("    MD5OfBody:     " + message.getMD5OfBody());
            System.out.println("    Body:          " + message.getBody());
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
    
    

}
