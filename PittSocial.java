//Java class to manage the Postgres server for
//CS1555 Project

//Make sure you have postgresql-42.2.5.jar in local directory
//  -----------------------------------------------------   //
//Compile with: javac PittSocial.java
//Run with: java -cp postgresql-42.2.5.jar:. PittSocial
//  -----------------------------------------------------   //

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import javax.lang.model.util.ElementScanner6;

import java.sql.*; //import the file containing definitions for the parts

//needed by java for database connection and manipulation
public class PittSocial {
    private static Connection connection; // used to hold the jdbc connection to the DB
    private static String lastLoginDate;
    static String DB_username = "postgres";
    static String DB_password = "zhao139";
    static String url = "jdbc:postgresql://localhost:5432/pitt_social?currentSchema=public";
    static Boolean driverMode = false;

    static PittSocial ps = new PittSocial(url, DB_username, DB_password,false); // Create a static connection class to use in
    // main

    public static void main(String[] args) throws SQLException {

        Scanner sc = new Scanner(System.in);
        int choice = -1;

        System.out.println("Welcome to Pitt Social");

        while (true) {
            System.out.println("Please Choose an Option:\n1. Create a User \n2. Login \n3. Exit");
            System.out.print("-> ");
            if (sc.hasNextInt()) {
                choice = sc.nextInt();
                if (choice < 1 && choice > 3) {
                    System.out.println("Invalid choice");
                    continue;
                }
            } else {
                System.out.println("Invalid choice");
                continue;
            }
            switch (choice) {
                case 1: {

                    // Go to create user interface
                    ps.createUserHelper();
                    break;
                }

                case 2: {
                    System.out.println(" --- Log In ---");
                    String login_email, login_pass;

                    Scanner login_scanner = new Scanner(System.in);

                    System.out.print("Email Address: ");
                    login_email = login_scanner.next();

                    System.out.print("Password: ");
                    login_pass = login_scanner.next();

                    // Pass values into login
                    int userID = ps.login(login_email, login_pass);

                    if (userID == 0) {
                        break;
                    }

                    // For more IO go to after login, pass the userID
                    after_login(userID);
                    break;
                }

                case 3: {
                    System.out.println("Exit");
                    try {
                        connection.close();
                    } catch (Exception Ex) {
                        System.out.println("Error connecting to DB, Machine Error" + Ex.toString());
                    }
                    System.exit(0);
                    break;
                }
            }
        }
    }

    // Constructor class to call and init connection / connection values from driver
    // to DB
    PittSocial(String dburl, String dbuser, String dbpass, Boolean driverMode) {
        this.driverMode = driverMode;
        this.DB_password = dbpass;
        this.DB_username = dbuser;
        this.url = dburl;

        try {
            // Register the PostgreSQL driver
            Class.forName("org.postgresql.Driver");

            // connect to DB...
            connection = DriverManager.getConnection(url, DB_username, DB_password);

        } catch (Exception Ex) {
            System.out.println("Error connecting to database. Machine Error: " + Ex.toString());
            System.exit(0);
        }
    }

    public static boolean inputDateChecker(String inputDateString, DateTimeFormatter df) {
        boolean dateValid = true;
        try {
            LocalDate ld = LocalDate.parse(inputDateString, df);
            System.out.println(inputDateString + " good");
        } catch (DateTimeParseException e) {
            System.out.println(inputDateString + " is invalid");
            System.out.println("Error " + e);
            return false;
        }
        return true;

    }

    /*******************1.createUser***********************/
    public void createUserHelper() {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("uuuu-MM-dd");
        df = df.withResolverStyle(ResolverStyle.STRICT);

        Scanner user_scanner = new Scanner(System.in);
        //If create user option is chosen we need to get a name, email address, and DOB for the Database
        String name, email_address, password, date_of_birth;
        System.out.println("Creating new user..");

        //Reading Name
        System.out.print("Name: ");
        name = user_scanner.nextLine();
        while (name.length() > 51 || name.length() == 0) {
            System.out.println("Name must be less than 51 charecters or above 0 charecters");
            System.out.print("Name: ");
            name = user_scanner.nextLine();
        }

        //Reading Email Address
        System.out.print("Email Address: ");
        email_address = user_scanner.nextLine();

        while (email_address.length() > 51 || email_address.length() == 0) {
            System.out.println("Email must be less than 51 charecters or above 0 charecters");
            System.out.print("Email Address: ");
            email_address = user_scanner.nextLine();
        }

        //Reading Password
        System.out.print("Password: ");
        password = user_scanner.nextLine();

        while (password.length() > 51 || password.length() == 0) {
            System.out.println("Email must be less than 51 charecters or above 0 charecters");
            System.out.print("Email Address: ");
            password = user_scanner.nextLine();
        }

        //Reading DOB
        System.out.print("Date of Birth (yyyy-mm-dd): ");
        date_of_birth = user_scanner.nextLine();
        while (!inputDateChecker(date_of_birth, df)) {//regular expression check ////!date_of_birth.matches("\\d{4}-\\d{2}-\\d{2}")
            System.out.println("The format of Date of Birth is wrong.");
            System.out.println("Date of Birth (yyyy-mm-dd):");
            date_of_birth = user_scanner.nextLine();
        }

        //make a call to createUser with given information
        createUser(name, email_address, password, date_of_birth);
    }

    public void createUser(String name, String email_address, String password, String date_of_birth) {
        try {
            Statement statement;
            connection.setAutoCommit(false); // turn off the Auto Commit
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            //Serializable prevents if there are two transactions want to create user at the same time with same email address.
            //or prevents to read same last userID.

            statement = connection.createStatement();


            //Helper formatter for last login timestamp
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            Date now = new Date();
            String now_format = formatter.format(now);
            java.sql.Timestamp date_reg = new java.sql.Timestamp(formatter.parse(now_format).getTime());

            //Need to make sure the userID is not the same as one in the data base.
            //Do this by doing a query to the profile Table
            //grabbing the last profile ID #.
            //Adding 1 to it. and putting into profile db
            String query = "select UserID from profile order by userID desc LIMIT 1;";
            ResultSet rs = statement.executeQuery(query);
            int newUserID = 0;
            if (rs.next())
                newUserID = rs.getInt("userid");

            //result returns the most recently used user ID, so we add 1 to it and use it as the new profile id for our new user
            newUserID = newUserID + 1;

            String create_user_input = "insert into profile values(?,?,?,?,?,?)";
            PreparedStatement insert_profile = connection.prepareStatement(create_user_input);
            insert_profile.setInt(1, newUserID);
            insert_profile.setString(2, name);
            insert_profile.setString(3, email_address);
            insert_profile.setString(4, password);

            Date dob = new SimpleDateFormat("yyyy-mm-dd").parse(date_of_birth);
            java.sql.Date dob_sql = new java.sql.Date(dob.getTime());

            insert_profile.setDate(5, dob_sql);
            insert_profile.setTimestamp(6, date_reg);

            insert_profile.executeUpdate();

            connection.commit();
            System.out.println("Create User Successfully!");
            rs.close();
            return;

        } catch (Exception Ex) {
            try {
                connection.rollback();
            } catch (SQLException E) {
                System.out.println("Failure: " + E.toString());
            }
            System.out.println("Fail to send message," + Ex.toString());
        }
    }

    /*******************2.login***********************/
    // Returns the userID of the given log in request
    public int login(String login_email, String login_pass) {
        try {
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            int userID;
            Scanner sc = new Scanner(System.in);
            // Prepared Statement to avoid Injection
            PreparedStatement query_profiles = connection
                    .prepareStatement("select * from profile where email = ? and password = ?");

            query_profiles.setString(1, login_email);
            query_profiles.setString(2, login_pass);
            ResultSet loginResult = query_profiles.executeQuery();

            if (loginResult.next() == false) {
                connection.rollback();
                System.out.println("Email and/or Password is wrong / not found in our Database");
                return 0;
            } else {

                System.out.println("Login Successful");
                System.out.println("Loading information");

                // need to update last login for the current user
                SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                Date d = new Date();

                userID = loginResult.getInt("userid");
                // store the last login info and

                Statement getLastLogin = connection.createStatement();
                String getLastLoginQuery = "select lastlogin from profile where userid = " + userID + ";";
                ResultSet lastLogin = getLastLogin.executeQuery(getLastLoginQuery);
                lastLogin.next();
                SimpleDateFormat fm = new SimpleDateFormat("MM-dd-YYYY HH:mm:ss");
                lastLoginDate = fm.format(lastLogin.getTimestamp("lastlogin"));

                // then update
                String login_updater = "update profile set lastlogin = to_timestamp('" + formatter.format(d)
                        + "', 'DD-MM-YYYY HH24:MI:SS') where userid = " + userID + ";";

                Statement loginTimerUpdate;
                loginTimerUpdate = connection.createStatement();
                loginTimerUpdate.executeUpdate(login_updater);
                //Thread.sleep(1500);// sleep for 2 seconds, so that we have time to switch to the other transaction
            }
            connection.commit();
            return userID;
        } catch (Exception Ex) {
            try {
                connection.rollback();
            } catch (SQLException E) {
                System.out.println("Failure: " + E.toString());
            }
            System.out.println("Machine Error: " + Ex.toString());
        }
        return 0;
    }

    // Function to simply provide the logged in user a list of options and return
    // the option chosen
    private static int loggedInInterface() {
        Scanner choice = new Scanner(System.in);
        int userChoice = 0;

        // Start User log in interface here
        System.out.println(" --- User Interface --- ");
        System.out.println(
                "1. Initiate Friendship \t 2. Create Group \t 3. Initiate Adding Group \n4. Confirm Requests \t 5. Send Message To User \t 6. Send Message to Group\t 7. Display Messages"
                        + "\n 8. Display New Messages \t 9. Display Friends \t 10. Search for User \t 11. Three Degrees \n12.Top messages\t13. Logout \t 14. Drop User \t 15. Exit Application");
        System.out.print("->");
        userChoice = choice.nextInt();
        return userChoice;
    }

    // UI for post login
    private static void after_login(int userID) {
        Scanner sc = new Scanner(System.in);
        while (true) {
            int userChoice = loggedInInterface();
            while (userChoice < 1 || userChoice > 15) {
                System.out.println("Invalid choice -> Try again \n");
                userChoice = loggedInInterface();
            }

            switch (userChoice) {
                case 1: {
                    System.out.println(" --- Initiate Friendship --- "); // done
                    ps.initFriendshipHelper(userID);
                    break;
                }
                case 2: {
                    System.out.println(" --- Create a Group --- "); // done
                    ps.creategroupHelper(userID);
                    break;
                }
                case 3: {
                    System.out.println(" --- Initiate Into a Group --- "); // done
                    ps.initiateAddingGroupHelper(userID);
                    break;
                }
                case 4: {
                    System.out.println(" --- Confirm Requests --- ");

                    //set 2nd param to 0 to tell confirmRequests that we want user input and not driver input
                    ps.confirmRequests(userID, 0);

                    break;
                }
                case 5: {
                    System.out.println(" --- Send a Message To User --- ");
                    ps.sendMessageToUserHelper(userID);
                    break;
                }

                case 6: {
                    System.out.println(" --- Send a Message To Group --- ");
                    ps.sendMessageToGroupHelper(userID);
                    break;
                }

                case 7: {
                    System.out.println(" --- Display Messages --- ");
                    ps.displayMessagesHelper(userID);
                    break;
                }
                case 8: {
                    System.out.println(" --- Display New Messages");
                    ps.displayNewMessagesHelper(userID);
                    break;
                }

                case 9: {
                    System.out.println(" --- Display Friends --- ");
                    ps.displayFriendsHelper(userID);
                    break;
                }

                case 10: {
                    System.out.println(" --- Search For A User --- ");
                    ps.searchForUserHelper(userID);
                    break;
                }
                case 11: {
                    System.out.println(" --- Three Degrees --- ");
                    Scanner scan3 = new Scanner(System.in);
                    System.out.println("To ID:");
                    int toID = scan3.nextInt();
                    ps.threeDegrees(userID, toID);
                    break;
                }
                case 12: {
                    System.out.println("---Top messages---");
                    Scanner tpInputScanner = new Scanner(System.in);
                    System.out.println("For how many user`s messages do you want to display (int x):");
                    int x = tpInputScanner.nextInt();
                    System.out.println("For how many months messages do you want to trace back (int k):");
                    int k = tpInputScanner.nextInt();
                    System.out.println("Showing results of top " + x + " users` messages within past " + k + " months");
                    ps.topMessages(userID, x, k);
                    break;
                }
                case 13: {
                    System.out.println("--- Logout ---");
                    ps.logout(userID);
                    return;
                }
                case 14: {
                    System.out.println("--- Drop User---");
                    ps.dropUser(userID);
                    return;
                }
                case 15: {
                    System.out.println("--- Exit Application ---");
                    ps.exit();
                    break;
                }
            }
        }

    }

    /*******************3. initiateFriendship***********************/
    public void initFriendshipHelper(int fromUserID) {
        Scanner toScane = new Scanner(System.in);
        System.out.println("Please enter a User ID to send a friend request to");
        System.out.print("->");
        int user_id_to_find = toScane.nextInt();

        Scanner fresh_scan = new Scanner(System.in);
        System.out.println("Write a message with the friend request (200 Charecters or less)");
        System.out.print("->");
        String user_message = fresh_scan.nextLine();
        while (user_message.length() > 200) {
            System.out.println("Message must be 200 charecters or less");
            System.out.print("->");
            user_message = fresh_scan.nextLine();
        }

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException ex) {
            // System.out.println("Class not found exception caught: " + ex.getMessage());
        }

        // Establishing Connection
        try {
            String user_finder = "select * \nfrom profile \nwhere userID = ?;";
            PreparedStatement find_user = connection.prepareStatement(user_finder);
            find_user.setInt(1, user_id_to_find);
            ResultSet user_result = find_user.executeQuery();

            boolean cont = user_result.next();
            String user_to_be_requested = null;
            if (cont == true && user_id_to_find != fromUserID) {
                user_to_be_requested = user_result.getString("name");
            } else {
                System.out.println("Not a valid user ID.");
                System.out.println("Failure");
                connection.rollback();
                return;
            }

            String check_if_already_friend_query = "select * from friend where (userid1 = ? and userid2 = ?) or (userid1 = ? and userid2 = ?)";
            PreparedStatement check_if_already_friend_statement = connection.prepareStatement(check_if_already_friend_query);
            check_if_already_friend_statement.setInt(1, user_id_to_find);
            check_if_already_friend_statement.setInt(2, fromUserID);
            check_if_already_friend_statement.setInt(3, fromUserID);
            check_if_already_friend_statement.setInt(4, user_id_to_find);
            ResultSet check_if_already_friend_rs = check_if_already_friend_statement.executeQuery();
            if (check_if_already_friend_rs.next()) {
                System.out.println("He/She is already your friend!");
                System.out.println("Failure");
                connection.rollback();
                return;
            }

            System.out
                    .println("Send Friend Request to '" + user_to_be_requested + "'\nWith Message: '" + user_message + "' ?");
            System.out.print("(Y/N): ");
            String response = toScane.next();

            int userID = fromUserID;

            connection.commit();
            //Pass all the information in function for DB
            String confirmation = ps.initiateFriendship(userID, user_id_to_find, user_message, response);
            System.out.println(confirmation);

        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (SQLException E) {
                System.out.println("Failure: " + E.toString());
            }
            e.printStackTrace();
        }

    }

    //Function to make a friend request
    public String initiateFriendship(int fromUserId, int user_id_to_find, String user_message, String confirm) {

        // Getting class file
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException ex) {
            // System.out.println("Class not found exception caught: " + ex.getMessage());
        }
        try {
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            if (confirm.equalsIgnoreCase("Y")) {
                System.out.println("Sending friend request");
                //fromusedId, Useridtofind, user_message
                String insert_string = "insert into pendingFriend\n" + "values(?,?,?);";
                PreparedStatement insert_friend_req = connection.prepareStatement(insert_string);
                insert_friend_req.setInt(1, fromUserId);
                insert_friend_req.setInt(2, user_id_to_find);
                insert_friend_req.setString(3, user_message);
                insert_friend_req.executeUpdate();
                connection.commit();
                return "Success";

            } else if (confirm.equalsIgnoreCase("N")) {
                //System.out.println("NOT Sending Friend Request");
                connection.rollback();
                return "Failure";
            } else {
                System.out.println("Not an option");
                connection.rollback();
                return "Failure";
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException E) {
                System.out.println("Failure: " + E.toString());
            }
            return "Failure: " + e.toString();
        }
    }

    /*******************4. createGroup***********************/
    public void creategroupHelper(int userID) {
        //Getting the values need to create the new group
        Scanner group_scanner = new Scanner(System.in);
        System.out.print("Group Name (Max 50 Charecters): ");
        String group_name = group_scanner.nextLine();
        while (group_name.length() > 50) {
            System.out.println("Group Name Must Be 50 Chars or less");
            System.out.print("Group Name: ");
            group_name = group_scanner.nextLine();
        }

        System.out.print("Group Limit (Enter an integer): ");
        int group_limit = group_scanner.nextInt();

        Scanner fresh_Scan = new Scanner(System.in);
        System.out.print("Enter a group description (Max 200 Charecters): ");
        String group_description = fresh_Scan.nextLine();

        ps.createGroup(userID, group_name, group_limit, group_description);
    }


    public void createGroup(int userID, String group_name, int group_limit, String group_description) {
        //Getting class file
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException ex) {
            System.out.println("Class not found exception caught: " + ex.getMessage());
        }

        //Establishing Connection
        try {
            Statement get_groupID_number;

            connection.setAutoCommit(false);
            connection.setTransactionIsolation(connection.TRANSACTION_SERIALIZABLE);


            //Getting most recently used group ID
            get_groupID_number = connection.createStatement();
            //Get the most recent gID
            String query = "select gID \n from groupInfo \n order by gID desc \n LIMIT 1;";
            ResultSet rs = get_groupID_number.executeQuery(query);
            int result = 0;
            if (rs.next()) {
                result = rs.getInt("gID");
            }
            result = result + 1;


            //creating the new group with all the values we currently have
            String insert_string = "insert into groupInfo values(?,?,?,?);";
            PreparedStatement insert_group = connection.prepareStatement(insert_string);
            insert_group.setInt(1, result);
            insert_group.setString(2, group_name);
            insert_group.setInt(3, group_limit);
            insert_group.setString(4, group_description);


            insert_group.executeUpdate();
            System.out.println("'" + group_name + "' was created with a limit of " + group_limit + ", and a description of '" + group_description + "'");

            //Need to add the currently logged in user as the first member of this group
            String user_insert_string = "insert into groupMember values (?,?,?);";
            PreparedStatement insert_user_to_group;
            insert_user_to_group = connection.prepareStatement(user_insert_string);
            insert_user_to_group.setInt(1, result);
            insert_user_to_group.setInt(2, userID);
            insert_user_to_group.setString(3, "manager");
            insert_user_to_group.executeUpdate();

            connection.commit();
            rs.close();
            System.out.println("Current logged in user (" + userID + ") was added to '" + group_name + "' as the Manager");

        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (SQLException E) {
                System.out.println("Failure: " + E.toString());
            }
            System.out.println("Fail to send message," + e.toString());
        }
    }

    /*******************5. initiateAddingGroup ***********************/

    public void initiateAddingGroupHelper(int userID) {
        Scanner groupreq_scanner = new Scanner(System.in);
        System.out.print("Group ID: ");
        int groupID = groupreq_scanner.nextInt();

        Scanner fresh_scanner = new Scanner(System.in);
        System.out.print("Message (Max 200 Characters): ");
        String message_req = fresh_scanner.nextLine();

        ps.initiateAddingGroup(userID, groupID, message_req);
    }

    public void initiateAddingGroup(int userID, int groupID, String message_req) {

        //Getting class file
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException ex) {
        }

        //Establishing Connection
        try {
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

            //check if the user is already in the group
            String checkUserInGroupQuery = "select * from groupmember where userid = " + userID;
            Statement checkUserInGroup;
            checkUserInGroup = connection.createStatement();
            ResultSet checkUserInGroupRS = checkUserInGroup.executeQuery(checkUserInGroupQuery);
            while (checkUserInGroupRS.next()) {
                if (checkUserInGroupRS.getInt("gid") == groupID) {
                    System.out.println("You are already in this Group " + groupID);
                    System.out.println("Fail to initiate into group.");
                    connection.rollback();
                    return;
                }
            }

            //Getting most recently used group ID

            //Back end - no chance of SQL injection
            Statement get_groupID_number;
            get_groupID_number = connection.createStatement();
            String query = "select gID \n from groupInfo \n order by gID desc \n LIMIT 1;";
            ResultSet rs = get_groupID_number.executeQuery(query);
            int result = 0;
            rs.next();
            result = rs.getInt("gid");

            //groupID, userID, message_req
            String req_string = "insert into pendingGroupMember\nvalues (?,?,?);";
            PreparedStatement insert_group_req = connection.prepareStatement(req_string);
            insert_group_req.setInt(1, groupID);
            insert_group_req.setInt(2, userID);
            insert_group_req.setString(3, message_req);
            insert_group_req.executeUpdate();
            connection.commit();

            System.out.println("Current user (" + userID + ") has sent a group request to group #" + groupID + " with message '" + message_req + "'");
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException E) {
                System.out.println("Failure: " + E.toString());
            }
            System.out.println("Failure: " + e.toString());
        }
    }

    /*******************6. confirmRequests ***********************/

    //Simple function to get user input on confirmation choice
    public int confirmer() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Would you like to \n1. Confirm a specific request \n2. Confirm All Requests");
        System.out.print("->");
        int confirm_choice = sc.nextInt();

        while (confirm_choice > 2 || confirm_choice < 1) {
            System.out.println("Invalid choice. Please choose \n1. Specific Confirmation \n2. Confirm all");
            System.out.print("->");
            confirm_choice = sc.nextInt();
        }
        return confirm_choice;
    }

    //Helper function called to make DBMS movements for friend requests. Returns true if done correctly and fully
    public boolean friendConfirmer(int fromId, int toId, String fromMessage) {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException ex) {
            //System.out.println("Class not found exception caught: " + ex.getMessage());
        }

        //Establishing Connection
        try {
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            String mover = "Delete\nfrom pendingFriend\nwhere fromid = ? and toid = ? ;";
            PreparedStatement move_friend_req = connection.prepareStatement(mover);
            move_friend_req.setInt(1, fromId);
            move_friend_req.setInt(2, toId);
            //Deleting tuple from pendingFriend
            move_friend_req.executeUpdate();

            //Add the information into table friend
            Date d = new Date();
            java.sql.Date d_sql = new java.sql.Date(d.getTime());


            String adder = "Insert into friend\nvalues (?, ?,?,?);";
            PreparedStatement add_f_r = connection.prepareStatement(adder);
            add_f_r.setInt(1, fromId);
            add_f_r.setInt(2, toId);
            add_f_r.setDate(3, d_sql);
            add_f_r.setString(4, fromMessage);
            add_f_r.executeUpdate();

            connection.commit();
            return true;

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException E) {
                System.out.println("Failure: " + E.toString());
            }
            System.out.println("Failure" + e.toString());
            return false;
        }
    }

    //Helper function called to make DBMS movements for group requests. Returns true if done correctly and fully
    public boolean groupConfirmer(int fromID, int groupID) {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException ex) {
            //System.out.println("Class not found exception caught: " + ex.getMessage());
        }

        //Establishing Connection
        try {
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            //delete the tuple from pendingGroupMember and add the tuple into groupMember
            String mover = "Delete\nfrom pendingGroupMember\nwhere userID = ? and gID = ? ;";
            PreparedStatement move_group_req = connection.prepareStatement(mover);
            move_group_req.setInt(1, fromID);
            move_group_req.setInt(2, groupID);


            //Deleting tuple from pendingGroupMember
            move_group_req.executeUpdate();


            //Check Group Limit with full SQL Query
            String limit_check = "SELECT CASE\n" +
                    "when (select count(gID) from groupMember where gID = ? ) < (select size from groupInfo where gID = ? )\n" +
                    "then 1 \nelse 0 \n END as Result;";
            PreparedStatement lim = connection.prepareStatement(limit_check);
            lim.setInt(1, groupID);
            lim.setInt(2, groupID);

            ResultSet rsLim = lim.executeQuery();
            rsLim.next();
            int result = rsLim.getInt("Result"); //Returns 1 if we can add. 0 if limit is reached or over

            if (result == 1) {
                //Add the information into table groupMember with role 'member'
                String adder = "Insert into groupMember\nvalues (?,?, 'member');";
                PreparedStatement inser_group_member = connection.prepareStatement(adder);
                inser_group_member.setInt(1, groupID);
                inser_group_member.setInt(2, fromID);
                inser_group_member.executeUpdate();
                connection.commit();
                return true;
            } else if (result == 0) {
                System.out.println("Group #" + groupID + " has reached its limit.");
                connection.rollback();
                return false;
            } else {
                connection.rollback();
                return false;
            }

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException E) {
                System.out.println("Failure: " + E.toString());
            }
            System.out.println("Failure: " + e.toString());
            return false;
        }
    }

    //Full Helper Function
    public void confirmRequests(int userID, int confirm_choice) {
        //Getting class file
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException ex) {
            System.out.println("Class not found exception caught: " + ex.getMessage());
        }

        //Establishing Connection
        try {
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(connection.TRANSACTION_SERIALIZABLE);
            //Start here
            //Get all friend request for given user
            String select_requests = "Select *\nfrom pendingFriend\nwhere toID = ? ";
            PreparedStatement get_friend_requests = connection.prepareStatement(select_requests);
            get_friend_requests.setInt(1, userID);
            ResultSet request_set = get_friend_requests.executeQuery();

            //Use arrays to store the fromIDs and the from Messages
            int[] fromIDs = new int[20];
            String[] fromMessages = new String[20];
            int[] gIDs = new int[10];
            int i = 0;
            int group_id = 0;

            System.out.println(" --- Friend Requests --- ");
            //Display the user# and message, to the logged in user
            while (request_set.next()) {
                fromIDs[i] = request_set.getInt("fromid");
                fromMessages[i] = request_set.getString("message");
                System.out.println((i + 1) + ". Friend Request from User #" + fromIDs[i] + ": '" + fromMessages[i] + "'");
                i++;
            }
            //System.out.println(fromIDs[i - 1]);

            //Need to see if the current logged in user is a Manager of any groups
            String group_requests = "Select *\nfrom groupMember\nwhere userid = ? and role = 'manager'";
            PreparedStatement get_group_requests = connection.prepareStatement(group_requests);
            get_group_requests.setInt(1, userID);
            ResultSet group_set = get_group_requests.executeQuery();
            connection.commit();

            int checker = 0;
            System.out.println(" --- Group Requests --- ");
            //Pull the group # that they mangage

            while (group_set.next()) {
                group_id = group_set.getInt("gid");

                //use that group # to do a select on pendingGroupMember to pull all requests given the group #
                String get_requests = "Select *\nfrom pendingGroupMember\nwhere gID = ? ;";
                PreparedStatement final_group_requests = connection.prepareStatement(get_requests);
                final_group_requests.setInt(1, group_id);
                ResultSet group_resulter = final_group_requests.executeQuery();


                while (group_resulter.next()) {
                    gIDs[i] = group_resulter.getInt("gid");
                    fromIDs[i] = group_resulter.getInt("userid");
                    fromMessages[i] = group_resulter.getString("message");
                    System.out.println((i + 1) + ". Request to join group #" + gIDs[i] + " from User #" + fromIDs[i] + ": '" + fromMessages[i] + "'");
                    i++;
                    checker++;
                }
            }

            if (i == 0) {
                System.out.println("No Outstanding Requests");
                connection.commit();
                return;
            }


            int friend_req_num = i - checker;
            Scanner sc = new Scanner(System.in);

            //This if else Bypasses user input so driver works
            if (confirm_choice == 0) {
                confirm_choice = confirmer();
            } else {
                //continues
            }
            switch (confirm_choice) {
                case 1: {
                    //Confirm a specific request
                    System.out.println("Choose a specific request # to confirm (Enter 0 to be done)");
                    System.out.print("->");
                    int req_number = sc.nextInt();

                    int friend_num = friend_req_num;
                    int group_num = i - friend_req_num;
                    int[] already_used = new int[i];
                    int pointer = 0;
                    while (req_number != 0) {
                        //make sure request is valid
                        while (req_number > i || req_number < 1) {
                            System.out.println("Invalid Request Number Please choose one between 1 and " + i);
                            System.out.print("->");
                            req_number = sc.nextInt();
                        }

                        //For loop to make sure the same request isnt confirmed twice
                        for (int chk = 0; chk < already_used.length; chk++) {
                            while (already_used[chk] == req_number) {
                                System.out.println("Request #" + already_used[chk] + " was already used");
                                System.out.println("Please enter an unanswered request");
                                System.out.print("->");
                                req_number = sc.nextInt();
                            }
                        }

                        //put the new request into the already used array, to be checked later
                        already_used[pointer] = req_number;


                        if (req_number <= friend_req_num) {
                            //Were confirming a friend request
                            System.out.println("Confirming Friend Request #" + req_number);
                            connection.commit();
                            boolean b = friendConfirmer(fromIDs[req_number - 1], userID, fromMessages[req_number - 1]);

                            System.out.println("Done: " + b);

                            friend_num--;

                        } else if (req_number > friend_req_num) {
                            //were confirming a group request
                            System.out.println("Confirming Group Request #" + req_number);
                            connection.commit();
                            boolean b = groupConfirmer(fromIDs[req_number - 1], gIDs[req_number - 1]);

                            System.out.println("Done: " + b);
                            group_num--;
                        }

                        //No more requests to be handled
                        //returns the user back to main UI
                        if (friend_num == 0 && group_num == 0) {
                            connection.commit();
                            System.out.println("No more outstanding requests");
                            return;
                        }

                        System.out.println("Choose a specific request # to confirm (Enter 0 to be done)");
                        System.out.print("->");
                        req_number = sc.nextInt();
                        pointer++;
                    }

                    //Done confirming requests

                    //Deleting all remaining things in the request arrays
                    System.out.println("Deleting Remaining Requests...");
                    for (int z = 1; z <= i; z++) {
                        //delete the remaining tuples from pendingFriend
                        String mover = "Delete\nfrom pendingFriend\nwhere fromid = ? and toid = ?;";
                        PreparedStatement delete_pend_friend = connection.prepareStatement(mover);
                        delete_pend_friend.setInt(1, fromIDs[z - 1]);
                        delete_pend_friend.setInt(2, userID);

                        //Deleting tuple from pendingFriend
                        delete_pend_friend.executeUpdate();


                        //delete the remaining tuples from pendingGroupMember
                        String deleter = "Delete\nfrom pendingGroupMember\nwhere userID = ? and gID = ? ;";
                        PreparedStatement delete_pend_group = connection.prepareStatement(deleter);
                        delete_pend_group.setInt(1, fromIDs[z - 1]);
                        delete_pend_group.setInt(2, gIDs[z - 1]);

                        //Deleting tuple from pendingGroupMember
                        delete_pend_group.executeUpdate();
                        connection.commit();
                    }
                    break;
                }
                case 2: {
                    //Confirm all
                    for (int y = 1; y <= i; y++) {
                        if (y <= friend_req_num) {
                            //Confirming friend requests
                            System.out.println("Confirming Friend Request #" + y);
                            connection.commit();
                            boolean b = friendConfirmer(fromIDs[y - 1], userID, fromMessages[y - 1]);
                            System.out.println("Done: " + b);


                        } else if (y > friend_req_num) {
                            //Confirming group requests
                            System.out.println("Confirming Group Request #" + y);
                            connection.commit();
                            boolean b = groupConfirmer(fromIDs[y - 1], gIDs[y - 1]);

                            System.out.println("Done: " + b);
                        }
                    }
                    connection.commit();
                    break;
                }
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException E) {
                System.out.println("Failure: " + E.toString());
            }
            System.out.println("Failure: " + e.toString());
        }
    }

    /*******************7. sendMessageToUser ***********************/
    public static void sendMessage(int userID, int mode) {
        Scanner sc = new Scanner(System.in);
        int fromUserID = userID, toUserID = -1, toGroupID = -1;

        String msg = "";
        if (mode == 0) {
            System.out.print("Please enter the userID you wanna sent to: ");
            if (sc.hasNextInt()) {
                toUserID = sc.nextInt();
            } else {
                System.out.println("Invalid userID");
                return;
            }
        } else {
            System.out.println("Please enter the groupID you wanna sent to: ");
            if (sc.hasNextInt()) {
                toGroupID = sc.nextInt();
            } else {
                System.out.println("Invalid GroupID");
                return;
            }
        }

        sc.nextLine();
        try {
            Statement statement;
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            // avoid from this situation:
            // Transaction1: User 1 send msg to user 2.
            // Transaction2: Drop user2.

            statement = connection.createStatement();
            String query;
            ResultSet rs;

            //display recipient
            if (mode == 0) {
                query = "select name from profile where userID = " + toUserID + ";";

                rs = statement.executeQuery(query);
                String toUserName;

                if (rs.next()) {
                    toUserName = rs.getString("name");
                    System.out.println("Recipient Name: " + toUserName);
                } else {
                    System.out.println("Failure: The recipient ID cannot be found");
                    return;
                }
            }

            //asking user to enter multiline message
            System.out.println("Message: ");

            String tempLine = "";
            while (sc.hasNextLine()) {
                tempLine = sc.nextLine();
                if (tempLine.isEmpty()) {
                    break;
                }
                msg += tempLine + "\n";
            }

            //Generate current timestamp
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            Date now = new Date();
            String now_format = formatter.format(now);
            java.sql.Timestamp date_reg = new java.sql.Timestamp(formatter.parse(now_format).getTime());


            int msgID = 0;
            query = "select msgID from messageInfo order by msgID desc LIMIT 1 ";
            rs = statement.executeQuery(query);
            if (rs.next()) {
                msgID = rs.getInt("msgID") + 1;
            }

            PreparedStatement insertMsg = connection.prepareStatement("insert into messageInfo values" + "(?,?,?,?,?,?);");
            insertMsg.setInt(1, msgID);
            insertMsg.setInt(2, fromUserID);
            insertMsg.setString(3, msg);
            if (mode == 0) {
                insertMsg.setInt(4, toUserID);
                insertMsg.setNull(5, Types.INTEGER);
            } else {
                insertMsg.setNull(4, Types.INTEGER);
                insertMsg.setInt(5, toGroupID);
            }


            insertMsg.setTimestamp(6, date_reg);

            insertMsg.executeUpdate();
            connection.commit();
            System.out.println("Send message successfully!");

            rs.close();

        } catch (Exception Ex) {
            try {
                connection.rollback();
            } catch (SQLException E) {
                System.out.println("Failure: " + E.toString());
            }
            System.out.println("Fail to send message," + Ex.toString());
        }
    }

    public void sendMessageToUserHelper(int userID) {
        Scanner sc = new Scanner(System.in);
        int fromUserID = userID, toUserID = -1, toGroupID = -1;
        System.out.print("Please enter the userID you wanna sent to: ");
        if (sc.hasNextInt()) {
            toUserID = sc.nextInt();
        } else {
            System.out.println("Invalid userID");
            return;
        }
        sc.nextLine();

        try {
            Statement statement;
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            // avoid from this situation:
            // Transaction1: User 1 send msg to user 2.
            // Transaction2: Drop user2.

            statement = connection.createStatement();
            String query;
            ResultSet rs;

            //display recipient
            query = "select name from profile where userID = " + toUserID + ";";

            rs = statement.executeQuery(query);
            String toUserName;

            if (rs.next()) {
                toUserName = rs.getString("name");
                System.out.println("Recipient Name: " + toUserName);
            } else {
                connection.rollback();
                System.out.println("Failure: The recipient ID cannot be found");
                return;
            }


            //asking user to enter multiline message
            System.out.println("Message: ");

            String msg = "";
            String tempLine = "";
            while (sc.hasNextLine()) {
                tempLine = sc.nextLine();
                if (tempLine.isEmpty()) {
                    break;
                }
                msg += tempLine + "\n";
            }
            connection.commit();
            sendMessageToUser(fromUserID, toUserID, msg);

        } catch (Exception Ex) {
            try {
                connection.rollback();
            } catch (SQLException E) {
                System.out.println("Failure: " + E.toString());
            }
            System.out.println("Fail to send message," + Ex.toString());
        }


    }

    public void sendMessageToUser(int fromUserID, int toUserID, String msg) {
        try {
            Statement statement;
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            // avoid from this situation:
            // Transaction1: User 1 send msg to user 2.
            // Transaction2: Drop user2.

            statement = connection.createStatement();
            String query;
            ResultSet rs;


            //Generate current timestamp
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            Date now = new Date();
            String now_format = formatter.format(now);
            java.sql.Timestamp date_reg = new java.sql.Timestamp(formatter.parse(now_format).getTime());


            //Get the last msgID and add 1.
            int msgID = 0;
            query = "select msgID from messageInfo order by msgID desc LIMIT 1 ";
            rs = statement.executeQuery(query);
            if (rs.next()) {
                msgID = rs.getInt("msgID");
            }
            msgID += 1;

            //insert msg into messageInfo
            PreparedStatement insertMsg = connection.prepareStatement("insert into messageInfo values" + "(?,?,?,?,?,?);");
            insertMsg.setInt(1, msgID);
            insertMsg.setInt(2, fromUserID);
            insertMsg.setString(3, msg);
            insertMsg.setInt(4, toUserID);
            insertMsg.setNull(5, Types.INTEGER);
            insertMsg.setTimestamp(6, date_reg);
            insertMsg.executeUpdate();
            connection.commit();
            System.out.println("Send message successfully!");
            rs.close();

        } catch (Exception Ex) {
            try {
                connection.rollback();
            } catch (SQLException E) {
                System.out.println("Failure: " + E.toString());
            }
            System.out.println("Fail to send message," + Ex.toString());
        }
    }


    /*******************8. sendMessageToGroup ***********************/

    public void sendMessageToGroupHelper(int userID) {
        Scanner sc = new Scanner(System.in);
        int fromUserID = userID, toGroupID = -1;
        System.out.println("Please enter the ID of the group you wanna sent to: ");
        if (sc.hasNextInt()) {
            toGroupID = sc.nextInt();
        } else {
            System.out.println("Invalid GroupID, back to the main menu");
            return;
        }
        sc.nextLine();

        try {
            Statement statement;
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            //avoid from this situation
            //T1: User1 send msg to group 2
            //T2: Drop user1

            statement = connection.createStatement();
            String query;
            ResultSet rs;

            //asking user to enter multiline message
            System.out.println("Message: ");
            String msg = "";
            String tempLine = "";

            while (sc.hasNextLine()) {
                tempLine = sc.nextLine();
                if (tempLine.isEmpty()) {
                    break;
                }
                msg += tempLine + "\n";
            }
            connection.commit();
            sendMessageToGroup(fromUserID, toGroupID, msg);

        } catch (Exception Ex) {
            try {
                connection.rollback();
            } catch (SQLException E) {
                System.out.println("Failure: " + E.toString());
            }
            System.out.println("Fail to send message, " + Ex.toString());
        }
    }


    public void sendMessageToGroup(int fromUserID, int toGroupID, String msg) {
        try {
            Statement statement;
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            //avoid from this situation
            //T1: User1 send msg to group 2
            //T2: Drop user1

            statement = connection.createStatement();
            String query;
            ResultSet rs;

            //Generate current timestamp
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            Date now = new Date();
            String now_format = formatter.format(now);
            java.sql.Timestamp date_reg = new java.sql.Timestamp(formatter.parse(now_format).getTime());

            int msgID = 0;
            query = "select msgID from messageInfo order by msgID desc LIMIT 1 ";
            rs = statement.executeQuery(query);
            if (rs.next()) {
                msgID = rs.getInt("msgID");
            }
            msgID += 1;

            PreparedStatement insertMsg = connection.prepareStatement("insert into messageInfo values " + "(?,?,?,?,?,?)");
            insertMsg.setInt(1, msgID);
            insertMsg.setInt(2, fromUserID);
            insertMsg.setString(3, msg);
            insertMsg.setNull(4, Types.INTEGER);
            insertMsg.setInt(5, toGroupID);
            insertMsg.setTimestamp(6, date_reg);
            insertMsg.executeUpdate();
            connection.commit();
            System.out.println("Send Message Successfully!");
            rs.close();

        } catch (Exception Ex) {
            try {
                connection.rollback();
            } catch (SQLException E) {
                System.out.println("Failure: " + E.toString());
            }
            System.out.println("Fail to send message, " + Ex.toString());
        }

    }


    /*******************9. displayMessages ***********************/

    public void displayMessagesHelper(int userID) {
        displayMessages(userID);
    }

    public void displayMessages(int userID) {
        try {
            //since it does not involve i/o operation
            //we do default autocommit and read committed.
            Statement statement = connection.createStatement();

            //select all the message that the user recieved
            String query = "select * from messageRecipient where userID = " + userID + ";";
            ResultSet rs = statement.executeQuery(query);

            System.out.println("--- Your Messages (UserID 0 or GroupID 0 means it was no longer in the database) ---");
            int msgCount = 1;
            ResultSet msg;
            Statement selectMsg = connection.createStatement();

            //iteration all message that the user has
            while (rs.next()) {
                query = "select * from messageInfo where msgID = " + rs.getInt("msgID") + ";";
                msg = selectMsg.executeQuery(query);
                while (msg.next()) {
                    if (msg.getInt("touserid") != 0) {
                        System.out.println("|--- From UserID " + msg.getInt("fromid") + " ---|");
                        System.out.println("     TimeSent: " + msg.getTimestamp("timesent"));
                        System.out.println("     MsgID " + msg.getString("msgid") + " : " + msg.getString("message"));
                        System.out.println("|---------------------------------------------------|");
                        System.out.println();
                    } else {
                        System.out.println("|--- From GroupID " + msg.getInt("togroupid") + " ---|");
                        System.out.println("     TimeSent: " + msg.getTimestamp("timesent"));
                        System.out.println("     MsgID " + msg.getString("msgid") + " : " + msg.getString("message"));
                        System.out.println("|---------------------------------------------------|");
                        System.out.println();
                    }
                    msgCount++;
                }
            }
            connection.commit();
        } catch (Exception Ex) {
            System.out.println("Fail to display msg, Error: " + Ex.toString());
        }
    }

    /*******************10. displayNewMessages ***********************/

    public void displayNewMessagesHelper(int userID) {
        displayNewMessages(userID);
    }

    public void displayNewMessages(int userID) {
        try {
            Statement statement = connection.createStatement();
            //select all the message that the user recieved.

            String query = "select * from messageRecipient where userID = " + userID + ";";
            ResultSet rs = statement.executeQuery(query);

            System.out.println("--- Your New Messages (UserID 0 or GroupID 0 means it was no longer in the database) ---");
            int msgCount = 1;
            ResultSet msg;
            Statement selectMsg = connection.createStatement();
            //iterate all messages and display the new messages
            while (rs.next()) {
                query = "select * from messageInfo where msgID = " + rs.getInt("msgID") + " and timesent >= timestamp '" + lastLoginDate + "' ;";
                msg = selectMsg.executeQuery(query);
                while (msg.next()) {
                    if (msg.getInt("touserid") != 0) {
                        System.out.println("|--- From UserID " + msg.getInt("fromid") + " ---|");
                        System.out.println("     TimeSent: " + msg.getTimestamp("timesent"));
                        System.out.println("     MsgID " + msg.getString("msgid") + " : " + msg.getString("message"));
                        System.out.println("|---------------------------------------------------|");
                        System.out.println();
                    } else {
                        System.out.println("|--- From GroupID " + msg.getInt("togroupid") + " ---|");
                        System.out.println("     TimeSent: " + msg.getTimestamp("timesent"));
                        System.out.println("     MsgID " + msg.getString("msgid") + " : " + msg.getString("message"));
                        System.out.println("|---------------------------------------------------|");
                        System.out.println();
                    }
                    msgCount++;
                }
            }
            connection.commit();

        } catch (Exception Ex) {

            System.out.println("Fail to display new msg, Error: " + Ex.toString());
        }
    }


    /*******************11. displayFriends ***********************/
    public void displayFriendsHelper(int userID) {
        try {
            Statement selectFriendStatement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            String query = "select * from friend where userid1 = " + userID + "OR userid2 = " + userID;
            ResultSet allFriendsSet = selectFriendStatement.executeQuery(query);

            System.out.println("--- Your Friends ---");
            ResultSet friendsProfileSet;

            Statement selectFriendsPrifleStatement = connection.createStatement();
            while (allFriendsSet.next()) {
                int friendUserId = allFriendsSet.getInt("userid1");
                if (friendUserId == userID) {
                    friendUserId = allFriendsSet.getInt("userid2");
                }
                query = "select * from profile where userid = " + friendUserId;
                friendsProfileSet = selectFriendsPrifleStatement.executeQuery(query);
                while (friendsProfileSet.next()) {
                    System.out.println("UserId: " + friendUserId + " Name: " + friendsProfileSet.getString("name"));
                }
            }
            if(!driverMode){
                displayFriends(userID, allFriendsSet);
            }
            connection.commit();
        } catch (Exception Ex) {
            try {
                connection.rollback();
            } catch (SQLException E) {
                System.out.println("Failure: " + E.toString());
            }
            System.out.println("Fail to display friends," + Ex.toString());
        }
    }

    public void displayFriends(int userID, ResultSet allFriendsSet) {
        Scanner sc = new Scanner(System.in);
        try {
            while (true) {
                System.out.println("Enter a friend's user id to retrieve the profile, or Enter 0 to return to the main menu");
                if (sc.hasNextInt()) {
                    int specifiedFriendUserID = sc.nextInt();

                    //if userInput is 0, then return to the main menu
                    if (specifiedFriendUserID == 0) {
                        connection.rollback();
                        System.out.println("Exit... return to the main menu...");
                        return;
                    }

                    //if userInput is him/herself, then return to the main menu
                    if (specifiedFriendUserID == userID) {
                        System.out.println("You cannot see your profile in this function, try again ");
                        continue;
                    }

                    //check if the user enter a real friend.
                    allFriendsSet.beforeFirst();
                    boolean foundFriend = false;
                    while (allFriendsSet.next()) {
                        int friendUserId = allFriendsSet.getInt("userid1");
                        if (friendUserId != specifiedFriendUserID) {
                            friendUserId = allFriendsSet.getInt("userid2");
                            if (friendUserId == specifiedFriendUserID) {
                                foundFriend = true;
                                break;
                            }
                        } else {
                            foundFriend = true;
                            break;
                        }
                    }

                    if (!foundFriend) {
                        System.out.println("This is not your friend. Try again...");
                        continue;
                    }


                    //Retrieve this friend's profile
                    ResultSet friendsProfileSet;
                    PreparedStatement selectFriendProfile = connection.prepareStatement("select * from profile where userid = " + specifiedFriendUserID);
                    friendsProfileSet = selectFriendProfile.executeQuery();
                    while (friendsProfileSet.next()) {
                        int profileUserID = friendsProfileSet.getInt("userid");
                        String profileName = friendsProfileSet.getString("name");
                        String profileEmail = friendsProfileSet.getString("email");
                        Date profileDOB = friendsProfileSet.getDate("date_of_birth");
                        Timestamp profileLastLogin = friendsProfileSet.getTimestamp("lastlogin");

                        System.out.println("--- Friend's Profile ---");
                        System.out.println("User Id: " + profileUserID);
                        System.out.println("Name: " + profileName);
                        System.out.println("Email: " + profileEmail);
                        System.out.println("Date of Birth: " + profileDOB);
                        System.out.println("Last Login: " + profileLastLogin);
                        System.out.println();
                    }
                    connection.commit();
                } else {
                    connection.rollback();
                    System.out.println("Invalid Input! Back to the main menu");
                    return;
                }
            }

        } catch (Exception Ex) {
            try {
                connection.rollback();
            } catch (SQLException E) {
                System.out.println("Failure: " + E.toString());
            }
            System.out.println("Fail to display friends," + Ex.toString());
        }
    }


    /*******************12. SearchForUser ***********************/

    public void searchForUserHelper(int userID) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Search User(case sensitive):  ");
        String userSearchInput = sc.nextLine();
        String[] userSearchArr = userSearchInput.trim().split("\\s+");

        searchForUser(userID, userSearchArr);
    }


    public void searchForUser(int login_userID, String[] userSearchArr) {
        try {
            Statement statement;
            //default autocommit and read committed isolation level
            statement = connection.createStatement();
            String userSearchQuery;
            PreparedStatement userSearchStatement;
            ResultSet currentProfile;
            for (int i = 0; i < userSearchArr.length; i++) {
                String nameOrEmail = userSearchArr[i];
                userSearchQuery = "select * from profile where name = ? or email = ? ";
                userSearchStatement = connection.prepareStatement(userSearchQuery);
                userSearchStatement.setString(1, nameOrEmail);
                userSearchStatement.setString(2, nameOrEmail);
                currentProfile = userSearchStatement.executeQuery();
                while (currentProfile.next()) {
                    System.out.print("userid: " + currentProfile.getInt("userid"));
                    System.out.print(" name: " + currentProfile.getString("name"));
                    System.out.print(" email: " + currentProfile.getString("email"));
                    System.out.print(" date_of_birth: " + currentProfile.getDate("date_of_birth"));
                    System.out.println(" lastlogin: " + currentProfile.getTimestamp("lastlogin"));
                    System.out.println("----------------------------------------------------------------------------------------------------");
                }
            }

            connection.commit();

        } catch (Exception Ex) {
            try {
                connection.rollback();
            } catch (SQLException E) {
                System.out.println("Failure: " + E.toString());
            }
            System.out.println("Fail to search user," + Ex.toString());
        }
    }

    /*******************13. threeDegrees ***********************/
    public void threeDegrees(int userId, int toId) {
        try {
            if (userId == toId) {
                System.out.println("You are actually user " + toId + "!");
                return;
            }

            PreparedStatement checkFriendShip = connection.prepareStatement("select f.userid1 as fid from friend f where f.userid2 = ? and f.userid1 = ? union all\n" +
                    "select f.userid2 as fid from friend f where f.userid1 = ? and f.userid2 = ?;");
            checkFriendShip.setInt(1, userId);
            checkFriendShip.setInt(2, toId);
            checkFriendShip.setInt(3, userId);
            checkFriendShip.setInt(4, toId);

            ResultSet rs = checkFriendShip.executeQuery();
            if (rs.next()) {
                System.out.println("Current user " + userId + " and input user " + toId + " are friends (" + userId + " ---> " + toId + ")");
                connection.commit();
                return;
            }
            System.out.println("Current user " + userId + " and input user " + toId + " are not friends");

            PreparedStatement selectFriends = connection.prepareStatement("select f.userid1 as fid from friend f where f.userid2 = ? union all\n" +
                    "select f.userid2 as fid from friend f where f.userid1 = ?;");
            selectFriends.setInt(1, userId);
            selectFriends.setInt(2, userId);
            ResultSet rs1 = selectFriends.executeQuery();
            while (rs1.next()) {
                checkFriendShip.setInt(1, rs1.getInt(1));
                checkFriendShip.setInt(2, toId);
                checkFriendShip.setInt(3, rs1.getInt(1));
                checkFriendShip.setInt(4, toId);
                rs = checkFriendShip.executeQuery();
                if (rs.next()) {
                    System.out.println("Two degress found (" + userId + " ---> " + rs1.getInt(1) + " ---> " + toId + ")");
                    connection.commit();
                    return;
                }
                System.out.println("Two degrees attempt failed for (" + userId + " ---> " + rs1.getInt(1) + " -x-> " + toId + ")");
            }

            rs1 = selectFriends.executeQuery();
            while (rs1.next()) {
                Statement statement2 = connection.createStatement();
                String selectAllFriends2 = "select f.userid1 as fid from friend f where f.userid2 = " + rs1.getInt(1) + " union all\n" +
                        " select f.userid2 as fid from friend f where f.userid1 = " + rs1.getInt(1) + ";";
                ResultSet rs2 = statement2.executeQuery(selectAllFriends2);
                while (rs2.next()) {
                    if (rs2.getInt(1) == userId) {
                        continue;
                    }
                    checkFriendShip.setInt(1, rs2.getInt(1));
                    checkFriendShip.setInt(2, toId);
                    checkFriendShip.setInt(3, rs2.getInt(1));
                    checkFriendShip.setInt(4, toId);
                    rs = checkFriendShip.executeQuery();
                    if (rs.next()) {
                        System.out.println("Three degrees found (" + userId + " ---> " + rs1.getInt(1) + " ---> " + rs2.getInt(1) + " ---> " + toId + ")");
                        connection.commit();
                        return;
                    }
                    System.out.println("Three degrees attempt failed for (" + userId + " ---> " + rs1.getInt(1) + " ---> " + rs2.getInt(1) + " -x-> " + toId + ")");
                }
            }


        } catch (Exception e) {
            System.out.println("Fail to display threeDegrees, Error: " + e.toString());
        }
    }


    /*******************14. topMessages ***********************/

    public void topMessages(int userId, int numOfUsers, int numOfMonths) {
        try {
            PreparedStatement selectAllMsg = connection.prepareStatement("select m.msgid as mid, m.fromid as id from messageinfo m where m.togroupid is null and m.touserid = ? and age(current_timestamp, m.timesent) < interval '" + numOfMonths + " months' union all\n" +
                    " select m.msgid as mid, m.touserid as id from messageinfo m where m.togroupid is null and m.fromid =  ? and age(current_timestamp, m.timesent) < interval '" + numOfMonths + " months';");
            selectAllMsg.setInt(1, userId);
            selectAllMsg.setInt(2, userId);
            ResultSet Am = selectAllMsg.executeQuery();
            Map<Integer, Integer> groups = new HashMap<Integer, Integer>();
            while (Am.next()) {
                Integer uid = Am.getInt("id");
                Integer ctOfMid = groups.get(uid);
                if (ctOfMid == null) {
                    ctOfMid = new Integer(0);
                    groups.put(uid, ctOfMid);
                }
                ctOfMid += 1;
                groups.put(uid, ctOfMid);
            }

            Map<Integer, List<Integer>> ct2uid = new TreeMap<>(Collections.reverseOrder());
            for (int uid : groups.keySet()) {
                List<Integer> listOfUid = ct2uid.get(groups.get(uid));
                if (listOfUid == null) {
                    listOfUid = new ArrayList<Integer>();
                    ct2uid.put(groups.get(uid), listOfUid);
                }
                listOfUid.add(uid);
            }

            int i = 0;
            for (int ct : ct2uid.keySet()) {
                for (int y = 0; y < ct2uid.get(ct).size() && i < numOfUsers; y++, i++) {
                    System.out.println("UserId " + ct2uid.get(ct).get(y) + " has " + ct + " messages with you");
                }
            }
            connection.commit();
        } catch (Exception Ex) {
            try {
                connection.rollback();
            } catch (SQLException E) {
                System.out.println("Failure: " + E.toString());
            }
            System.out.println("Fail to display msg, Error: " + Ex.toString());
        }
    }


    /*******************15. logout ***********************/
    public void logout(int userID) {
        try {

            // need to update last login for the current user
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            Date d = new Date();

            // then update
            String login_updater = "update profile set lastlogin = to_timestamp('" + formatter.format(d)
                    + "', 'DD-MM-YYYY HH24:MI:SS') where userid = " + userID + ";";

            Statement loginTimerUpdate;
            loginTimerUpdate = connection.createStatement();
            loginTimerUpdate.executeUpdate(login_updater);
            connection.commit();
        } catch (SQLException Ex) {
            try {
                connection.rollback();
            } catch (SQLException E) {
                System.out.println("Failure: " + E.toString());
            }
            System.out.println("Fail to log out, " + Ex.toString());
        }
    }

    /*******************16. dropUser ***********************/
    public void dropUser(int userId) {
        try {
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

            Statement statement = connection.createStatement();
            String deleteFromProfile = "delete from profile where userid = " + userId + ";";
            statement.executeUpdate(deleteFromProfile);


            Statement deleteFromMsgStatement = connection.createStatement();
            String deleteFromMessageInfoQuery = "delete from messageInfo where fromid is NULL and touserid is NULL and togroupid is NULL;";
            deleteFromMsgStatement.executeUpdate(deleteFromMessageInfoQuery);

            connection.commit();

            return;


        } catch (Exception Ex) {
            try {
                connection.rollback();
            } catch (SQLException E) {
                System.out.println("Failure: " + E.toString());
            }
            System.out.println("Fail to drop user, " + Ex.toString());
        }
    }

    /*******************17. exit ***********************/
    public void exit() {
        try {
            connection.close();
            System.exit(0);
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
    }
}