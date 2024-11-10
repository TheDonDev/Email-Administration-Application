package emailapp;

import java.util.Scanner;

public class Email {
    private String firstName;
    private String lastName;
    private String password;
    private String department;
    private int mailboxCapacity;
    private String alternateEmail;

    // Constructor to recieve the first name and last name
    public Email(String firstName, String lastName){
        this.firstName = firstName;
        this.lastName = lastName;
        System.out.println("EMAIL CREATED: " + this.firstName + " " + this.lastName );
        
        // Call for method asking for the department - return the department 
        this.department = setDepartment();
        System.out.println("Department: " + this.department);
    }
 // Ask for department 
 private String setDepartment(){
    System.out.print("DEPARTMENT CODES\n1 for Sales\n2 for Development\n3 for Accounting\n0 for None.\nEnter Department Code: ");
    Scanner in = new Scanner(System.in);
    int depChoice = in.nextInt();
    if (depChoice == 1 ) {return "sales";}
    else if (depChoice == 2) { return "dev"; }
    else if (depChoice == 3) { return "acct";}
    else { return "";}

 }
    
}
