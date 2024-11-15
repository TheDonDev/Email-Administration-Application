package emailapp;

import java.util.Scanner;

public class Email {
    private String firstName;
    private String lastName;
    private String password;
    private String department;
    private String email;
    private int mailboxCapacity = 500;
    private int defaultPasswordLength = 10;
    private String alternateEmail;
    private String companySuffix = "aeycompany.com";

    // Constructor to recieve the first name and last name
    public Email(String firstName, String lastName){
        this.firstName = firstName;
        this.lastName = lastName;
        
        // Call for method asking for the department - return the department 
        this.department = setDepartment();

        // Call a method that returns a random password
        this.password = randomPassword(defaultPasswordLength);
        System.out.println("Your password is: " + this.password);

        // Combine elements to generate email
        email = firstName.toLowerCase() + "." + lastName.toLowerCase() + "@" + department + "." + companySuffix;
        System.out.println("Your Email is: " + email);
    }
 // Ask for department 
 private String setDepartment(){
    System.out.print("New Worker " + firstName + " Please Enter your " + " DEPARTMENT CODE: \n1 for Sales\n2 for Development\n3 for Accounting\n0 for None.\nEnter Department Code:");
    Scanner in = new Scanner(System.in);
    int depChoice = in.nextInt();
    if (depChoice == 1 ) {return "sales";}
    else if (depChoice == 2) { return "dev"; }
    else if (depChoice == 3) { return "acct";}
    else { return "";}

 }
    // Generate a random password
    private String randomPassword(int length){
        String passwordSet = "ABCDEFGHIJKLMNOPUVWXYZabcdefghijklmnopuvwxyz0123456789!@#$%^&*";
        char[] password = new char[length];
        for (int  i=0; i<length; i++){
            int rand = (int) (Math.random() * passwordSet.length());
            password[i] = passwordSet.charAt(rand);   
        }
        return new String(password);
    }
}
