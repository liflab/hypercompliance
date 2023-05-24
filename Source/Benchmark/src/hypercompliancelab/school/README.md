# School Admission Process

This folder contains the Business Process Model and Notation (BPMN) engine and related properties of a typical school admission process.

## Process Description

The school admission process begins when an application is submitted, after which an officer is assigned to review the application. Based on the review, additional documents may be requested from the applicant. Once all required documents are received, an interview with the applicant is scheduled. Following the interview, the officer evaluates the applicant and the application, which results in either an acceptance or a rejection letter being sent to the applicant.

We implemented the process using Activiti, an open-source workflow engine. All decisions in the process are automated based on random values. The `Pickers` class in the `hypercompliancelab.school.process.pickers` package contains several methods for generating random choices for a simulated school application process. The class is equipped with a set of methods that randomly pick applicants and employees, decide whether to request additional documents, determine progress conditions, and make acceptance decisions. The acceptance decision method has a unique feature that adjusts the probability of acceptance based on the applicant's GPA, adding a degree of realism to the simulation. Other methods provide a randomly picked interview date and GPA. The class uses a combination of other classes and random generator objects to achieve its goals, ensuring the simulated process remains unpredictable and varied.






## Folder Structure

This repository is organized as follows:

- `delegates/`: Contains the classes used to implement the automated delegates for the tasks in the process.
- `entities/`:  ontains the classes used to represent the entities involved in the admission process.
- `logging/`: Contains the classes used to log the execution of the admission process into XES an file.
- `pickers/`: Contains the classes used to generate random choices for the simulated process.


### Important Classes
The `SchoolAdmissionProcess` class within the `hypercompliancelab.school.process` package serves as the main class for the school admission process simulation. This class creates and utilizes an Activiti process engine to run a simulated school admission process with a specified number of instances. It sets up and configures the process engine, deploys the process definition from a .bpmn file, and initiates the process instances with varying parameters based on randomly generated applicants and their corresponding properties. The log data resulting from these process instances is subsequently saved to a .xes file.  


The `SchoolAdmissionSource` class is part of the `hypercompliancelab.school` package and provides a source of `LogUpdate` events generated from the School Admission process for the lab. It executes the process a `NUMBER_OF_TRACES` and generates a log an XES file. 
 
## Properties

 
The properties we check on this process include:

1. **AcceptanceRate:** This hyperquery checks if the acceptance ratio of a process is above a given threshold.
2. **BalancedLoad:** Implements the hyperpolicy that ensures fair distribution of applications among employees.
3. **ConsistencyCondition:** This property ensures that every application trace that contains a low GPA ends in the same state, ensuring fairness in the process.
4. **EvilEmployee:** Implements the hyperpolicy that checks whether there exist an employee that is rejecting all applications. This helps in preventing any potential bias in the process.
5. **SameState:** Evaluates the hyperpolicy that no more than 'k' instances of a process can be in the same state at the same time. Each event in a trace is taken to be the current state of the process. This helps in maintaining a balanced workflow.
6. **StaffLoad:** Implements the hyperpolicy stipulating that at any moment, the number of active cases assigned to an employee must not exceed a constant 'n'. This ensures manageable workloads for employees.

For more detailed descriptions of each property, please refer to the source code in the `hypercompliancelab.school.properties` package.
