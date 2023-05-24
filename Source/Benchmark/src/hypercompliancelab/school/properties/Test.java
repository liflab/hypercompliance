package hypercompliancelab.school.properties;

import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.Pullable;
import ca.uqac.lif.cep.Pushable;
import ca.uqac.lif.cep.hypercompliance.InterleavedSource;
import ca.uqac.lif.cep.hypercompliance.Log;
import ca.uqac.lif.cep.hypercompliance.LogUpdate;
import ca.uqac.lif.cep.hypercompliance.XesToLog;
import ca.uqac.lif.cep.io.Print;
import ca.uqac.lif.fs.FileSystemException;
import ca.uqac.lif.fs.HardDisk;
import hypercompliancelab.school.process.SchoolAdmissionProcess;
import hypercompliancelab.school.process.logging.LogReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static ca.uqac.lif.cep.Connector.connect;

public class Test {

    public static void runScenario(List<String> scenarios) throws FileSystemException, IOException{
        for (String scenario : scenarios) {
            System.out.println("Checking property: " + scenario);

            Processor hyperpolicy = null;
            switch (scenario) {
                case "EvilEmployee":
                    hyperpolicy = new EvilEmployee();

                    break;
                case "StaffLoad":
                    hyperpolicy = new StaffLoad();
                    break;
                case "BalancedLoad":
                    hyperpolicy = new BalancedLoad();
                    break;
                case "ConsistencyCondition":
                    hyperpolicy = new ConsistencyCondition();
                    break;
                case "SameState":
                    hyperpolicy = new SameState();
                    break;
                case "AcceptanceRate":
                    hyperpolicy = new AcceptanceRate();
                    break;
                case "AllEndedChecker":
                    hyperpolicy = new AllEndedChecker().getHyperpolicy();
                    break;
            }

//            PrintThrough pt = new PrintThrough();
//            connect(pt, hyperpolicy, new Print.Println());
//            Pushable p = pt.getPushableInput();

//            /* Push a few events to illustrate the operation. */
            Pushable p = hyperpolicy.getPushableInput();
            connect(hyperpolicy, new Print.Println());


            String filePath= "data" + "/" + "School_Admission.xes";
            HardDisk hd = new HardDisk("data").open();
            OutputStream os = hd.writeTo("School_Admission.xes");
            SchoolAdmissionProcess.runAndProduceLogs(1000, os);
            os.close();
            hd.close();
            

            Log log = new LogReader("application_id", filePath).readLog();
            InterleavedSource is = new InterleavedSource(log, "timestamp");
            is.loop(false);

            Pullable pullable = is.getPullableOutput();

            while (pullable.hasNext()) {
                LogUpdate upd;
                upd = (LogUpdate) pullable.pull();
                p.push(upd);
            }

            System.out.println("Finished checking property: " + scenario);

        }
    }

    public static void main(String[] args) throws FileSystemException, IOException {

        List<String> scenarios = new ArrayList<String>() {{
            add("EvilEmployee"); //Done
            add("StaffLoad"); // Done
            add("BalancedLoad"); //Done
            add("ConsistencyCondition"); // Done
            add("UnderReview"); // ?
            add("AcceptanceRate"); //Done
        }};

        runScenario(scenarios);


    }
}
