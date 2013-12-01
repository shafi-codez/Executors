package com.iit.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import java.util.HashMap;

/**
 * Class Responsible for the Data Base work
 *
 * @author Eduardo Hernandez Marquina
 * @author Hector Veiga
 * @author Gerardo Travesedo
 *
 */
public class DBManager {

    private static String url; //  "jdbc:mysql://ServerName:Port/DBName"
    private static String username;
    private static String password;
    private static Connection conn;

    public static void main(String[] s) {
        //getResult();
        //updateJobInfo(null);
        getResult(3);
    }

    public DBManager() {
    }

    public DBManager(String serverNameDB, int portDB, String nameDB,
            String userNanme, String password) {
        setUrl("jdbc:mysql://" + serverNameDB + ":" + portDB + "/" + nameDB);
        System.out.println(getUrl());
        setUsername(userNanme);
        setPassword(password);
    }

    public static Connection getConn() {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/itm411db", "itm411db", "itm411db");
        } catch (Exception e) {
            System.err.println("Connection Failed :" + e.getLocalizedMessage());
        }
        return conn;
    }

    public static ResultSet getResult(int uid) {
        try {
            Connection con = DBManager.getConn();
            ResultSet rs = null;
            Statement st = con.createStatement();
            String qr = "SELECT * FROM REQUEST WHERE JID="+uid;
            rs = st.executeQuery(qr);
            
            return rs;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    
    

    public void startConnection() throws SQLException {
        //this.connection = DriverManager.getConnection(getUrl(), getUsername(), getPassword());
    }

    public Connection getConnection() {
        return conn;
    }

    public static String getUrl() {
        return url;
    }

    public static void setUrl(String url) {
        DBManager.url = url;
    }

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username) {
        DBManager.username = username;
    }

    public static String getPassword() {
        return password;
    }

    public static void setPassword(String password) {
        DBManager.password = password;
    }
}