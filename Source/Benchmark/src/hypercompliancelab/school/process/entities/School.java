package hypercompliancelab.school.process.entities;

import hypercompliancelab.school.process.pickers.Pickers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/***
 * Used to generate the school's employees and applicants.
 */
public class School {

    private static List<Employee> admissionOfficers = null;

    public static List<Employee> generateOfficers(int instances) {
        if (admissionOfficers == null) {
            List<Employee> employees = new ArrayList<>();

            for (int i = 1; i <= instances; i++) {
                employees.add(new Employee("Admissions Officer " + i, "Admissions Officer"));
            }
            admissionOfficers = employees;
            return employees;

        } else return admissionOfficers;

    }


    public static List<Applicant> generateApplicants(int instances) {
        List<Applicant> applicantList = new ArrayList<>();

        for (int i = 1; i <= instances; i++) {
            String name = "Applicant" + i;
            double GPA = Pickers.getGPA();
            String school = "School" + i;
            LocalDate dob = LocalDate.of(2000, i % 12 + 1, i % 28 + 1);
            String nationality = (i % 2 == 0) ? "CountryA" : "CountryB";
            boolean isInternational = i % 2 == 0;
            Applicant applicant = new Applicant(name, GPA, school, dob, nationality, isInternational);
            applicantList.add(applicant);
        }

        return applicantList;
    }

}

