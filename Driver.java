//Driver Java File for phase 3 of cs1555 project
//Made to use as front end. with PittSocial.java as backend, and Postgres DB as back end

//Make sure you have postgresql-42.2.5.jar in local directory
//  -----------------------------------------------------   //
//Compile with: javac Driver.java
//Run with: java -cp postgresql-42.2.5.jar:. Driver
//  -----------------------------------------------------   //

import java.sql.*;
import java.lang.Thread;

public class Driver {
    private static Connection connection; // used to hold the jdbc connection to the DB
    private static String lastLoginDate;

    static String dbuser = "postgres";
    static String dbpass = "zhao139";
    static String dburl = "jdbc:postgresql://localhost:5432/pitt_social?currentSchema=public";
    static PittSocial ps = new PittSocial(dburl, dbuser, dbpass,true); // Create the PittSocial Connection

    public static void main(String[] args) throws InterruptedException {

        System.out.println("Welcome to Pitt Social Driver");
        clearData();

        // Create 5 users
        System.out.println("Before Creating Users");
        displayProfile();
        Thread.sleep(500);
        System.out.println("Creating 5 New Users");
        ps.createUser("User1", "user1@email.com", "u1", "1995-01-01");
        ps.createUser("User2", "user2@email.com", "u2", "1991-01-02");
        ps.createUser("User3", "user3@email.com", "u3", "1997-01-03");
        ps.createUser("User4", "user4@email.com", "u4", "1992-01-04");
        ps.createUser("User5", "user5@email.com", "u5", "1994-01-05");
        System.out.println("Testing Creating User Corner Case");
        ps.createUser("User6", "user5@email.com", "u5", "1999-01-01");

        Thread.sleep(500); //give time
        System.out.println("After Creatinng Users");
        displayProfile();
        Thread.sleep(2000); //quick gander

        //test login
        System.out.println("Testinng Login");
        ps.login("user2@email.com", "u2");
        ps.login("user3@email.com", "u3");
        ps.login("user4@email.com", "u4");
        ps.login("user1@email.com", "u1");
        ps.login("user5@email.com", "u5");

        System.out.println("Testing Login Corner Case");
        ps.login("user100@gmail.com", "u1");
        ps.login("user5@email.com", "u1");


        //test friendship sends
        System.out.println("Before Sending Friend Requests");
        displaypendingFriend();
        Thread.sleep(500);
        System.out.println("Sending Various Friend Requests");
        ps.initiateFriendship(2, 3, "Sending from User 2 to 3", "Y");
        ps.initiateFriendship(3, 1, "Sending from User 3 to 1", "Y");
        ps.initiateFriendship(4, 1, "Sending from User 4 to 1", "Y");
        ps.initiateFriendship(3, 4, "Sending from User 3 to 4", "Y");
        ps.initiateFriendship(4, 5, "Sending from User 4 to 5", "Y");
        System.out.println("Testing Sending Friend Requests Corner Case");
        ps.initiateFriendship(2, 2, "Sending from user 2 to 2", "N");
        ps.initiateFriendship(2, 100, "Sending from 2 to 100", "N");


        Thread.sleep(500); //give time
        System.out.println("After Sending Friend Requests");
        displaypendingFriend();
        Thread.sleep(2000); //quick gander


        //Create 4 groups
        System.out.println("Creating 4 Groups");
        System.out.println("Before Creating Groups");
        displayGroupInfo();
        displaygroupMember();
        Thread.sleep(500);

        ps.createGroup(1, "Group #1", 2, "Group made by User 1");
        ps.createGroup(1, "Group #2", 5, "Made for testing of user 1 owning both group1 and group2");
        ps.createGroup(2, "Group #3", 5, "Group made by User 2");
        ps.createGroup(3, "Group #4", 5, "Group made by User 3");

        Thread.sleep(500); //give time
        System.out.println("After Creating Groups");
        displayGroupInfo();
        System.out.println("");

        Thread.sleep(500);
        displaygroupMember();
        Thread.sleep(2000); //quick gander

        //have the other 2 request to join the 4 groups
        System.out.println("Before Initiating  Adding Group");
        displaypendingFriend();
        System.out.println();
        Thread.sleep(500);
        System.out.println("Initiating Adding Group");
        ps.initiateAddingGroup(4, 1, "Let User #4 join group #1");
        ps.initiateAddingGroup(4, 2, "Let User #4 join group #2");
        ps.initiateAddingGroup(4, 3, "Let User #4 join group #3");
        ps.initiateAddingGroup(4, 4, "Let User #4 join group #4");
        ps.initiateAddingGroup(5, 1, "Let User #5 join group #1");
        ps.initiateAddingGroup(5, 2, "Let User #5 join group #2");
        ps.initiateAddingGroup(5, 3, "Let User #5 join group #3");
        ps.initiateAddingGroup(5, 4, "Let User #5 join group #4");
        //Corner case
        System.out.println("Testing corner case");
        ps.initiateAddingGroup(4, 5, "Let User#4 join an unknown group #5");
        ps.initiateAddingGroup(4, 1, "Let User#4 join group #1 again");

        System.out.println("After Initiating Adding Group");
        Thread.sleep(500);
        displaypendinggroupMember();
        Thread.sleep(2000);

        //Have users confirm requests sent to them
        System.out.println("Before Confirm Requests");
        displayFriend();
        Thread.sleep(500);
        displaygroupMember();
        Thread.sleep(500);
        System.out.println("");
        System.out.println("Testing Confirm Requests");
        ps.confirmRequests(1, 2);
        ps.confirmRequests(2, 2);
        ps.confirmRequests(5, 2);

        System.out.println("After Confirm Requests");
        Thread.sleep(500);
        displaypendingFriend();
        System.out.println("");

        Thread.sleep(500);
        displaypendinggroupMember();
        System.out.println("");

        Thread.sleep(500);
        displaygroupMember();
        System.out.println("");

        Thread.sleep(500);
        displayFriend();
        Thread.sleep(2000);

        System.out.println("Before Sending Message To User");
        displaymessageInfo();
        Thread.sleep(500);
        displaymessageRecipient();
        Thread.sleep(500);
        System.out.println("Testing Sending Message To User");
        ps.sendMessageToUser(1, 2, "Hello User 2, im user 1");
        ps.sendMessageToUser(3, 2, "Hello User 2, im user 3");
        ps.sendMessageToUser(4, 2, "Hello User 2, im user 4");
        ps.sendMessageToUser(5, 2, "Hello User 2, im user 5");
        ps.sendMessageToUser(1,3,"Hello user3, im user1");


        Thread.sleep(500);
        displaymessageInfo();
        System.out.println("");

        Thread.sleep(2000);
        displaymessageRecipient();
        Thread.sleep(500);
        System.out.println();


        System.out.println("Before Sending Message To Group");
        displaymessageInfo();
        Thread.sleep(500);
        displaymessageRecipient();
        Thread.sleep(500);
        System.out.println("Testing Sending Message To Group");
        ps.sendMessageToGroup(1, 2, "Hello Group 2, im user 1");
        ps.sendMessageToGroup(2, 2, "Hello Group 2, im user 2");
        ps.sendMessageToGroup(3, 2, "Hello Group 2, im user 3");
        ps.sendMessageToGroup(4, 2, "Hello Group 2, im user 1");
        System.out.println("Testing some corner case");
        ps.sendMessageToGroup(2,4,"User 2 is not in group 4");

        System.out.println("After Sending Message To Group");
        Thread.sleep(500);
        displaymessageInfo();
        System.out.println("");

        Thread.sleep(2000);
        displaymessageRecipient();
        Thread.sleep(500);


        System.out.println("Displaying New Messages for User 2");
        ps.displayNewMessages(2);

        System.out.println("");

        System.out.println("Displaying Messages for User 4");
        ps.displayMessages(4);


        Thread.sleep(1000);
        System.out.println("Testing display friend");
        System.out.println("Displaying User 1's Friends");
        ps.displayFriendsHelper(1);
        System.out.println("Displaying User 2's Friends");
        ps.displayFriendsHelper(2);
        System.out.println("Displaying User 3's Friends");
        ps.displayFriendsHelper(3);
        System.out.println("Displaying User 4's Friends");
        ps.displayFriendsHelper(4);
        System.out.println("Displaying User 5's Friends");
        ps.displayFriendsHelper(5);


        System.out.println("Testing Search User");
        Thread.sleep(500);
        System.out.println("Running a search for name User1, email user2@email.com, name User3, email user4@email.com ");
        String userSearchInput = "User1 user2@email.com User3 user4@email.com";
        String[] userSearchArr = userSearchInput.trim().split("\\s+");
        ps.searchForUser(1, userSearchArr);

        System.out.println("Running a search for name User2, email user2@email.com, name user100, email user101@email.com");
        String userSearchInput2 = "User2 user2@email.com user100 user101@email.com";
        String[] userSearchArr2 = userSearchInput2.trim().split("\\s+");
        ps.searchForUser(1, userSearchArr2);

        System.out.println("Running a search for name User1, user2@email.com, User3, user4@email.com, User5");
        String userSearchInput3 = "User1 user2@email.com User3 user4@email.com User5";
        String[] userSearchArr3 = userSearchInput3.trim().split("\\s+");
        ps.searchForUser(1, userSearchArr3);
        Thread.sleep(2000);

        System.out.println("Testing 3 Degree");
        System.out.println("3 Degree from 1 to 5");
        ps.threeDegrees(1, 5);
        Thread.sleep(500);

        System.out.println("");
        System.out.println("3 Degree from 3 to 1");
        ps.threeDegrees(3, 1);
        Thread.sleep(500);
        System.out.println();

        System.out.println("");
        System.out.println("3 Degree from 3 to 2");
        ps.threeDegrees(3, 2);
        Thread.sleep(500);
        System.out.println();

        System.out.println("Testing Top Message");
        System.out.println("");
        ps.topMessages(2, 3, 1);
        Thread.sleep(500);
        ps.topMessages(1,3,1);


        System.out.println("Testing log out");
        ps.logout(1);
        ps.logout(2);
        ps.logout(3);
        ps.logout(4);
        ps.logout(5);


        Thread.sleep(500);

        System.out.println("Testing Drop User");
        System.out.println("Before Dropping User1");
        displayProfile();
        Thread.sleep(500);
        System.out.println();
        displayGroupInfo();
        Thread.sleep(500);
        System.out.println();
        displaygroupMember();
        Thread.sleep(500);
        System.out.println();
        displaymessageInfo();
        Thread.sleep(500);
        System.out.println();
        displaymessageRecipient();
        Thread.sleep(500);
        System.out.println();

        System.out.println("Dropping user 1");
        ps.dropUser(1);

        System.out.println("After Dropping user1");
        displayProfile();
        Thread.sleep(500);
        System.out.println();
        displayGroupInfo();
        Thread.sleep(500);
        System.out.println();
        displaygroupMember();
        Thread.sleep(500);
        System.out.println();
        displaymessageInfo();
        Thread.sleep(500);
        System.out.println();
        displaymessageRecipient();
        Thread.sleep(500);
        System.out.println();

        System.out.println();
        System.out.println("Before Dropping User2");
        displayProfile();
        Thread.sleep(500);
        System.out.println();
        displayGroupInfo();
        Thread.sleep(500);
        System.out.println();
        displaygroupMember();
        Thread.sleep(500);
        System.out.println();
        displaymessageInfo();
        Thread.sleep(500);
        System.out.println();
        displaymessageRecipient();
        Thread.sleep(500);
        System.out.println();

        System.out.println("Dropping user 2");
        ps.dropUser(2);

        System.out.println("After Dropping user2");
        displayProfile();
        Thread.sleep(500);
        System.out.println();
        displayGroupInfo();
        Thread.sleep(500);
        System.out.println();
        displaygroupMember();
        Thread.sleep(500);
        System.out.println();
        displaymessageInfo();
        Thread.sleep(500);
        System.out.println();
        displaymessageRecipient();
        Thread.sleep(500);
        System.out.println();

        System.out.println("After Dropping Every One Else");
        ps.dropUser(3);
        ps.dropUser(4);
        ps.dropUser(5);

        displayProfile();
        Thread.sleep(500);
        System.out.println();
        displayGroupInfo();
        Thread.sleep(500);
        System.out.println();
        displaygroupMember();
        Thread.sleep(500);
        System.out.println();
        displaymessageInfo();
        Thread.sleep(500);
        System.out.println();
        displaymessageRecipient();
        Thread.sleep(500);
        System.out.println();
        displayFriend();
        Thread.sleep(500);
        System.out.println();
        displaypendingFriend();
        Thread.sleep(500);
        System.out.println();
        displaypendinggroupMember();


        ps.exit();
    }

    //** Helper Functions */
    //use to clear tuples from tables at the start of the driver function
    private static void clearData() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException ex) {
            System.out.println("Class not found exception caught: " + ex.getMessage());
        }
        try (Connection c = DriverManager.getConnection(dburl, dbuser, dbpass)) {
            String dropAll = "Delete From profile CASCADE;" +
                    "Delete from friend CASCADE;" +
                    "Delete from pendingFriend CASCADE;" +
                    "Delete from messageInfo CASCADE;" +
                    "Delete from messageRecipient CASCADE;" +
                    "Delete from groupInfo CASCADE;" +
                    "Delete from groupMember CASCADE;" +
                    "Delete from pendingGroupMember CASCADE;";
            PreparedStatement dropper = c.prepareStatement(dropAll);
            dropper.executeUpdate();
        } catch (SQLException e) {
        }
    }

    private static void displayProfile() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException ex) {
            System.out.println("Class not found exception caught: " + ex.getMessage());
        }
        try (Connection c = DriverManager.getConnection(dburl, dbuser, dbpass)) {
            String table_query = "Select * from profile";
            PreparedStatement query_table_statement = c.prepareStatement(table_query);
            ResultSet tbrs = query_table_statement.executeQuery();

            System.out.println("\t\t\t profile \t\t\t");

            System.out.println("\tuserID\t|" + "\tname\t|" + "\temail\t\t\t|" + "\tpassword\t|" + "\tdate_of_birth\t\t|" + "\tlastlogin\t");

            while (tbrs.next()) {
                System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------");
                int id = tbrs.getInt("userID");
                String name = tbrs.getString("name");
                String email = tbrs.getString("email");
                String password = tbrs.getString("password");
                Date d = tbrs.getDate("date_of_birth");
                Timestamp t = tbrs.getTimestamp("lastlogin");
                System.out.println("\t" + id + "\t|\t" + name + "\t|\t" + email + "\t\t|\t" + password + "\t\t|\t" + d.toString() + "\t\t|\t" + t.toString() + "\t");
            }

        } catch (SQLException e) {
            System.out.println("Failure: " + e.toString());
        }
    }

    private static void displaypendingFriend() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException ex) {
            System.out.println("Class not found exception caught: " + ex.getMessage());
        }
        try (Connection c = DriverManager.getConnection(dburl, dbuser, dbpass)) {
            String table_query = "Select * from pendingFriend";
            PreparedStatement query_table_statement = c.prepareStatement(table_query);
            ResultSet tbrs = query_table_statement.executeQuery();

            System.out.println("\t\t\t pendingFriend \t\t\t");
            System.out.println("\tfromID\t|" + "\ttoID\t|" + "\tmessage\t\t\t");

            while (tbrs.next()) {
                System.out.println("--------------------------------------------------------------------------------");
                int fromid = tbrs.getInt("fromID");
                int toid = tbrs.getInt("toID");
                String message = tbrs.getString("message");
                System.out.println("\t" + fromid + "\t|\t" + toid + "\t|\t" + message + "\t\t");
            }

        } catch (SQLException e) {
            System.out.println("Failure: " + e.toString());
        }
    }

    private static void displayGroupInfo() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException ex) {
            System.out.println("Class not found exception caught: " + ex.getMessage());
        }
        try (Connection c = DriverManager.getConnection(dburl, dbuser, dbpass)) {
            String table_query = "Select * from groupInfo";
            PreparedStatement query_table_statement = c.prepareStatement(table_query);
            ResultSet tbrs = query_table_statement.executeQuery();

            System.out.println("\t\t\t groupInfo \t\t\t");

            System.out.println("\tgID\t|" + "\tname\t\t|" + "\tsize\t|" + "\tdescription\t");

            while (tbrs.next()) {
                System.out.println("----------------------------------------------------------------------------------------");
                int id = tbrs.getInt("gID");
                String name = tbrs.getString("name");
                int limit = tbrs.getInt("size");
                String desc = tbrs.getString("description");
                System.out.println("\t" + id + "\t|\t" + name + "\t|\t" + limit + "\t|\t" + desc + "\t\t");
            }

        } catch (SQLException e) {
            System.out.println("Failure: " + e.toString());
        }
    }

    private static void displaypendinggroupMember() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException ex) {
            System.out.println("Class not found exception caught: " + ex.getMessage());
        }
        try (Connection c = DriverManager.getConnection(dburl, dbuser, dbpass)) {
            String table_query = "Select * from pendingGroupMember";
            PreparedStatement query_table_statement = c.prepareStatement(table_query);
            ResultSet tbrs = query_table_statement.executeQuery();

            System.out.println("\t\t\t pendingGroupMember\t\t\t");
            System.out.println("\tgID\t|" + "userID\t\t|" + "\tmessage\t");

            while (tbrs.next()) {
                System.out.println("-----------------------------------------------------------------------------------");
                int gid = tbrs.getInt("gID");
                int uid = tbrs.getInt("userID");
                String message = tbrs.getString("message");
                System.out.println("\t" + gid + "\t|\t" + uid + "\t|\t" + message + "\t");
            }

        } catch (SQLException e) {
            System.out.println("Failure: " + e.toString());
        }
    }

    private static void displaygroupMember() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException ex) {
            System.out.println("Class not found exception caught: " + ex.getMessage());
        }
        try (Connection c = DriverManager.getConnection(dburl, dbuser, dbpass)) {
            String table_query = "Select * from groupMember";
            PreparedStatement query_table_statement = c.prepareStatement(table_query);
            ResultSet tbrs = query_table_statement.executeQuery();

            System.out.println("\t\t\t groupMember \t\t\t");
            System.out.println("\tgID\t|" + "\tuserID\t\t|" + "\trole\t");

            while (tbrs.next()) {
                System.out.println("---------------------------------------------------------------------------------");
                int gid = tbrs.getInt("gID");
                int uid = tbrs.getInt("userID");
                String role = tbrs.getString("role");
                System.out.println("\t" + gid + "\t|\t" + uid + "\t\t|\t" + role + "\t");
            }

        } catch (SQLException e) {
            System.out.println("Failure: " + e.toString());
        }
    }

    private static void displayFriend() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException ex) {
            System.out.println("Class not found exception caught: " + ex.getMessage());
        }
        try (Connection c = DriverManager.getConnection(dburl, dbuser, dbpass)) {
            String table_query = "Select * from friend";
            PreparedStatement query_table_statement = c.prepareStatement(table_query);
            ResultSet tbrs = query_table_statement.executeQuery();

            System.out.println("\t\t\t friend \t\t\t");

            System.out.println("\tuserID1\t|" + "\tuserID2\t\t|" + "\tdate\t\t|" + "\tmessage\t");

            while (tbrs.next()) {
                System.out.println("----------------------------------------------------------------------------------------");
                int id1 = tbrs.getInt("userID1");
                int id2 = tbrs.getInt("userID2");
                Date d = tbrs.getDate("JDate");
                String message = tbrs.getString("message");
                System.out.println("\t" + id1 + "\t|\t" + id2 + "\t\t|\t" + d.toString() + "\t|\t" + message + "\t\t");
            }

        } catch (SQLException e) {
            System.out.println("Failure: " + e.toString());
        }
    }

    //I have yet to call this table. May need reformatted to look better
    private static void displaymessageInfo() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException ex) {
            System.out.println("Class not found exception caught: " + ex.getMessage());
        }
        try (Connection c = DriverManager.getConnection(dburl, dbuser, dbpass)) {
            String table_query = "Select * from messageInfo";
            PreparedStatement query_table_statement = c.prepareStatement(table_query);
            ResultSet tbrs = query_table_statement.executeQuery();

            System.out.println("\t\t\t messageInfo \t\t\t");

            System.out.println("\tmsgID\t|" + "\tfromID\t|" + "\tmessage\t\t\t\t|" + "\ttoUserID\t|" + "\ttoGroupID\t\t|" + "\ttimestamp\t");

            while (tbrs.next()) {
                System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------");
                int msgid = tbrs.getInt("msgID");
                int fromid = tbrs.getInt("fromID");
                String msg = tbrs.getString("message");
                int toUserID = tbrs.getInt("toUserID");
                int toGroupID = tbrs.getInt("toGroupID");
                Timestamp t = tbrs.getTimestamp("timesent");
                System.out.println("\t" + msgid + "\t|\t" + fromid + "\t|\t" + msg + "\t\t|\t" + toUserID + "\t\t|\t" + toGroupID + "\t\t|\t" + t.toString() + "\t");
            }

        } catch (SQLException e) {
            System.out.println("Failure: " + e.toString());
        }
    }

    //I have yet to call this table. May need reformatted to look better
    private static void displaymessageRecipient() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException ex) {
            System.out.println("Class not found exception caught: " + ex.getMessage());
        }
        try (Connection c = DriverManager.getConnection(dburl, dbuser, dbpass)) {
            String table_query = "Select * from messageRecipient";
            PreparedStatement query_table_statement = c.prepareStatement(table_query);
            ResultSet tbrs = query_table_statement.executeQuery();

            System.out.println("\tmessageRecipient \t\t");

            System.out.println("\tmsgId\t|" + "\tuserID\t\t");

            while (tbrs.next()) {
                System.out.println("--------------------------------------");
                int msgid = tbrs.getInt("msgID");
                int userid = tbrs.getInt("userID");
                System.out.println("\t" + msgid + "\t|\t" + userid + "\t\t");
            }
        } catch (SQLException e) {
            System.out.println("Failure: " + e.toString());
        }
    }

}