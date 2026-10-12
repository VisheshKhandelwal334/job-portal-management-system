package com.jobportal;

import com.jobportal.ui.AdminMenu;
import com.jobportal.ui.CompanyMenu;
import com.jobportal.ui.Display;
import com.jobportal.ui.StudentMenu;
import com.jobportal.util.InputUtil;

/** Entry point of the console-based Job Portal Management System. */
public class Main {

    public static void main(String[] args) {
        StudentMenu student = new StudentMenu();
        CompanyMenu company = new CompanyMenu();
        AdminMenu admin = new AdminMenu();

        while (true) {
            Display.title("JOB PORTAL MANAGEMENT SYSTEM");
            System.out.println("  1. Student Login");
            System.out.println("  2. Student Register");
            System.out.println("  3. Company Login");
            System.out.println("  4. Company Register");
            System.out.println("  5. Admin Login");
            System.out.println("  0. Exit");
            int choice = InputUtil.integer("Choose: ");
            try {
                switch (choice) {
                    case 1: student.login(); break;
                    case 2: student.register(); break;
                    case 3: company.login(); break;
                    case 4: company.register(); break;
                    case 5: admin.login(); break;
                    case 0:
                        System.out.println("\n  Thank you for using Job Portal. Goodbye!");
                        return;
                    default: System.out.println("  ! Invalid option.");
                }
            } catch (RuntimeException e) {
                // last line of defence: never crash the whole app on an unexpected error
                System.out.println("\n  ! Unexpected error: " + e.getMessage());
            }
        }
    }
}
