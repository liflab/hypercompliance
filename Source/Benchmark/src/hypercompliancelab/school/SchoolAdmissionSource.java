/*
  A BeepBeep palette to evaluate hypercompliance queries.
  Copyright (C) 2023 Sylvain Hallé

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  GNU General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package hypercompliancelab.school;

import ca.uqac.lif.cep.functions.*;
import ca.uqac.lif.cep.hypercompliance.Log;
import ca.uqac.lif.cep.hypercompliance.LogUpdate;
import ca.uqac.lif.cep.tuples.FetchAttribute;
import ca.uqac.lif.cep.tuples.Tuple;
import ca.uqac.lif.cep.tuples.TupleMap;
import ca.uqac.lif.cep.util.Equals;
import ca.uqac.lif.fs.FileSystemException;
import hypercompliancelab.Describable;
import hypercompliancelab.FetchAttributeOr;
import hypercompliancelab.LabFileSystem;
import hypercompliancelab.school.process.SchoolAdmissionProcess;
import hypercompliancelab.xes.LazyInterleavedSource;

import java.util.Arrays;

/**
 * A source of {@link LogUpdate} events that takes its data from the School Admission process. It runs the process a given number of times, and generates a log from the traces it produces.

 * @author Chukri Soueidi
 */
public class SchoolAdmissionSource extends LazyInterleavedSource implements Describable {

    /**
     * The number of traces to generate.
     */
    public static int NUMBER_OF_TRACES = 1000;

    /**
     * The name of this source, which also gives the name to the corresponding
     * scenario.
     */
    /*@ non_null @*/ public static final transient String NAME = "School Admission";

    public static Object lock = new Object();

    /**
     * The name of the XES file.
     */
    protected static final transient String s_xesFilename = "School_Admission.xes";


    /**
     * Creates a new instance of the source.
     *
     * @param fs The file system where the XES files are downloaded to.
     */
    public SchoolAdmissionSource(LabFileSystem fs) {
        super("timestamp", "application_id", fs, s_xesFilename);
    }

    @Override
    protected void populateFromLog(Log log) {
        // Add the END event at the end of each trace in the log
//  	Tuple end_event = new TupleMap();
//		end_event.put("action_code", "END");
//		end_event.put("timestamp", Long.MAX_VALUE);
//		log.appendToAll(Arrays.asList(end_event));
        super.populateFromLog(log);
    }

    @Override
    public Function getEndCondition() {
        return new FunctionTree(Equals.instance, new Constant("END"), new FunctionTree(new FetchAttribute("state"), StreamVariable.X));
    }

    @Override
    public Function getAction() {
        return new ToEventLabel();
    }

    protected static class ToEventLabel extends UnaryFunction<Tuple, EventLabel> {
        public ToEventLabel() {
            super(Tuple.class, EventLabel.class);
        }

        @Override
        public EventLabel getValue(Tuple t) {
            return new EventLabel((String) t.get("state"));
        }
    }

    protected static class EventLabel {

        private final String m_step;

        public EventLabel(String step) {
            super();

            m_step = step;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof EventLabel)) {
                return false;
            }
            EventLabel el = (EventLabel) o;
            return m_step.compareTo(el.m_step) == 0;
        }

        @Override
        public int hashCode() {
            return m_step.hashCode();
        }
    }

    @Override
    public Function getTimestamp() {
        return new FetchAttribute("timestamp");
    }

    @Override
    public boolean prerequisitesFulfilled() throws FileSystemException {
        return false;
    }


    /***
     * Runs the process and produces the log file. It is synchronized such that only one thread produces the log file one time only.
     * @throws FileSystemException
     */
    @Override
    public void fulfillPrerequisites() throws FileSystemException {

        synchronized (lock) {

            // If the file already exists, do nothing
            if (m_fs.isFile(m_filename)) return;

            System.out.println("." + m_fs.pwd() + "/" + s_xesFilename);

            SchoolAdmissionProcess.runAndProduceLogs(NUMBER_OF_TRACES, "." + m_fs.pwd() + "/" + s_xesFilename);
        }
    }

    @Override
    public void clean() throws FileSystemException {
        if (m_fs.isFile(s_xesFilename)) {
            m_fs.delete(s_xesFilename);
        }
    }

    @Override
    public String getDescription() {
        return "A log of cases for a synthetic school admissions process";
    }
}
