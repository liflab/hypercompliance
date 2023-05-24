package hypercompliancelab.school.process.logging;

import ca.uqac.lif.cep.hypercompliance.Log;
import ca.uqac.lif.cep.hypercompliance.XesToLog;

import java.io.*;

/***
 * This class is used to read XES log files.
 */
public class LogReader {

    private String trace_identifier;
    private String logFileName;

    public LogReader(String trace_identifier, String logFileName) {
        this.trace_identifier = trace_identifier;
        this.logFileName = logFileName;
    }

    public Log readLog() {
        File file = new File(logFileName);
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        XesToLog x = new XesToLog(trace_identifier);
        Log log = x.getLog(inputStream);
        try {
            inputStream.close(); // remember to close the stream when finished
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return log;
    }
}
