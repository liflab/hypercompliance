package hypercompliancelab.school.process.pickers;


import ca.uqac.lif.synthia.random.RandomInteger;
import ca.uqac.lif.synthia.sequence.Playback;
import hypercompliancelab.school.process.entities.Applicant;
import hypercompliancelab.school.process.entities.Employee;
import hypercompliancelab.school.process.entities.School;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;


/***
 * Provides all random choices for the process.
 */
public class Pickers {

    static Playback<Employee> officerPicker;
    static Playback<Applicant> applicantPicker;

    static CustomGaussian processProgressPicker;

    static List<Applicant> applicants = null;

    static List<Employee> officers = null;

    static int numberOfOfficers = 10;

    static {
        processProgressPicker = new CustomGaussian();
    }

    public static Applicant getApplicant(int instances) {

        if (applicants == null) {
            applicants = School.generateApplicants(instances);
            RandomInteger applicantSeed = new RandomInteger(0, instances - 1);
            applicantPicker = new Playback<Applicant>(applicantSeed.pick(), applicants);
            applicantPicker.setLoop(true);
        }

        return applicantPicker.pick();
    }

    public static Employee getEmployee() {

        if (officers == null) {
            officers = School.generateOfficers(numberOfOfficers);
            RandomInteger officersSeed = new RandomInteger(0, numberOfOfficers - 1);
            officerPicker = new Playback<Employee>(officersSeed.pick(), officers);
            officerPicker.setLoop(true);
        }

        return officerPicker.pick();

    }

    public static boolean getRequestAdditionalDocumentsDecision() {
        return new java.util.Random().nextBoolean();
    }


    public static ProcessProgressCondition getProgressCondtion() {
        return new ProcessProgressCondition(processProgressPicker.pick(0, 20));
    }

    public static Random random = new Random();

    public static boolean getAcceptanceDecision(Object employee, Object applicant) {

        Employee e = (Employee) employee;
        Applicant a = (Applicant) applicant;
        double gpa = a.getGPA();

        // If GPA is above 3, threshold to 0.7, so there is a higher chance the boolean will be true.
        // If GPA is below 2, threshold to 0.3, so there is a higher chance the boolean will be false.
        // For GPAs between 2 and 3, the threshold interpolates between 0.3 and 0.7.
        double threshold = (gpa < 2) ? 0.3 : ((gpa > 3) ? 0.7 : 0.3 + 0.4 * (gpa - 2));

        // Generate a random double and compare with the threshold
        return random.nextDouble() < threshold;
    }

    public static Instant getInterviewDate() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(new RandomInteger().pick());
        return Instant.now();
    }


    public static double getGPA() {
        return Math.round(((new Random().nextDouble()) + 0.25) * 4.0 * 100.0) / 100.0;
    }
}
