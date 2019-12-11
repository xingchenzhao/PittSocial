import java.util.GregorianCalendar;
import java.util.*;

public class nathanInsert {

  public static int randBetween(int start, int end) //Function to generate a random between 2 numbers
  {
    return start + (int)Math.round(Math.random() * (end - start)); //return the num generated
  }

  public static void main(String[] args) {
    GregorianCalendar gc = new GregorianCalendar();

    int year = randBetween(1950, 2010); //Get a random year to use

    gc.set(gc.YEAR, year); //set the year

    int dayOfYear = randBetween(1, gc.getActualMaximum(gc.DAY_OF_YEAR)); //get a random day in the year

    gc.set(gc.DAY_OF_YEAR, dayOfYear);

    System.out.println(gc.get(gc.YEAR) + "-" + (gc.get(gc.MONTH) + 1) + "-" + gc.get(gc.DAY_OF_MONTH));

    //profile insert for loop
    for(int x = 1; x <= 100; x++) {
      year = randBetween(1950, 2010);
      gc.set(gc.YEAR, year);
      dayOfYear = randBetween(1, gc.getActualMaximum(gc.DAY_OF_YEAR));
      gc.set(gc.DAY_OF_YEAR, dayOfYear);
      System.out.println("insert into profile\n" +
          "values (" + x + ", 'user_" + x + "', 'user_" + x + "@email.com', '" + gc.get(gc.YEAR) + "-" + (gc.get(gc.MONTH) + 1) + "-" + gc.get(gc.DAY_OF_MONTH) + "', to_timestamp('10-JAN-2019 09:00:00', 'DD-MON-YYYY HH24:MI:SS'));");
    }

    System.out.println("\n\n\n");

    //friend insert for loop
    int a, b;
    for(int y = 1; y <= 100; y ++){
      a = y;
      b = y+1;
      if(a == 99)
      {}
      else
        b = b%100;
      System.out.println("insert into friend\n" +
          "values ("+ a + ", " + b + ", '2019-01-01', '" + a + " to " + b + "');");
    }

    System.out.println("\n\n\n");


    //Group insert for loop
    int c; //gID
    int lim = 4; //limit of 4 -- could be changed?
    String gName = "Default name "; //group name - initalized to default
    String description = "Default Description for group "; //group description -- initalized to default
    for(int z = 1; z <= 10; z++)
    {
      System.out.println("insert into "+(char)34 +"group"+(char)34+"\n" +
          "values (" + z + ", '" + gName +" "+ z + "', " + lim + ", '" + description + " " + z + "');");
    }

    System.out.println("\n\n\n");


    //Message insert for loop
    int msgID, fromID;
    //int toUserID = 0, toGroupID = 0; //initialized to 0 ( null value ), as stated in project doc
    //instead of using these as int values, i just put the string NULL, since SQL will be able to read it that way
    String message = "default message ";
    for(int z1 = 1; z1 <= 300; z1++)
    {
      msgID = z1;
      fromID = z1;

      if(fromID%100 == 0)
      {
        fromID = 100;
      }
      else
        fromID = fromID%100;

      System.out.println("insert into "+ (char)34 +"message"+ (char)34 +"\n" +
          "values (" + msgID + ", " + fromID + ", '" + message + " " + z1 + "', NULL" + ", NULL"+ ", to_timestamp('10-JAN-2019 09:00:00', 'DD-MON-YYYY HH24:MI:SS'));");
    }
  }
}