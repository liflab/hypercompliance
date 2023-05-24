package hypercompliancelab.school.process.entities;

import java.io.Serializable;
import java.time.LocalDate;

/***
 * Represents an applicant to the school
 */
public class Applicant implements Serializable {
    String name;
    double GPA;
    String school;
    LocalDate dob;
    String nationality;
    boolean isInternational;

    public boolean getIsInternational() {
        return isInternational;
    }

    public Applicant(String name, double GPA, String school, LocalDate dob, String nationality, boolean isInternational) {
        this.name = name;
        this.GPA = GPA;
        this.school = school;
        this.dob = dob;
        this.nationality = nationality;
        this.isInternational = isInternational;
    }

    @Override
    public String toString() {
        return "Applicant{" + "name=" + name + ", GPA=" + GPA + ", school=" + school + ", dob=" + dob + ", nationality=" + nationality + ", isInternational=" + isInternational + '}';
    }

    public double getGPA() {
        return GPA;
    }
}
