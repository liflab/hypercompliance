package hypercompliancelab.school.process.entities;

import java.io.Serializable;

/***
 * Represents an employee of the school
 */
public class Employee implements Serializable {
    private String name;
    private String jobTitle;

    public Employee() {

    }

    public Employee(String name, String jobTitle) {
        this.name = name;
        this.jobTitle = jobTitle;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    @Override
    public String toString() {
        return name;
    }

}
