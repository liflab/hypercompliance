/*
  A BeepBeep palette to evaluate hypercompliance queries.
  Copyright (C) 2023 Sylvain Hallé and Chukri Soueidi

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

import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.experiment.ExperimentFactory;
import ca.uqac.lif.labpal.region.Point;
import ca.uqac.lif.synthia.Seedable;
import hypercompliancelab.Describable;
import hypercompliancelab.HyperqueryExperiment;
import hypercompliancelab.LabFileSystem;

import hypercompliancelab.school.properties.*;
import java.lang.reflect.Constructor;

import static hypercompliancelab.HyperqueryExperimentFactory.QUERY;
import static hypercompliancelab.HyperqueryExperimentFactory.SCENARIO;

public class SchoolAdmissionExperimentFactory extends ExperimentFactory<HyperqueryExperiment> implements Seedable {
    protected int m_seed;

    protected LabFileSystem m_fs;

    public SchoolAdmissionExperimentFactory(Laboratory lab, LabFileSystem fs) {
        super(lab);
        m_seed = 0;
        m_fs = fs;
    }

    @Override
    public SchoolAdmissionExperimentFactory setSeed(int seed) {
        m_seed = seed;
        return this;
    }

    @Override
    /*@ pure null @*/ protected HyperqueryExperiment createExperiment(Point p) {
        Processor source = null;
        Processor query = null;
        String scenario = (String) p.get(SCENARIO);
        String hyperquery = (String) p.get(QUERY);
        // Select the appropriate source
        switch (scenario) {

            case SchoolAdmissionSource.NAME:
                source = new SchoolAdmissionSource(m_fs);
                break;
        }

        // Select the appropriate hyperquery
        switch (hyperquery) {

            case EvilEmployee.NAME:
                query = new EvilEmployee();
                break;
            case StaffLoad.NAME:
                query = new StaffLoad();
                break;
            case ConsistencyCondition.NAME:
                query = new ConsistencyCondition();
                break;
            case BalancedLoad.NAME:
                query = new BalancedLoad();
                break;
            case AcceptanceRate.NAME:
                query = new AcceptanceRate();
                break;
            case SameState.NAME:
                query = new SameState();
                break;


        }
        if (source == null || query == null) {
            return null;
        }
        HyperqueryExperiment he = new HyperqueryExperiment(source, query);
        he.setInterval(100);
        if (query instanceof Describable) {
            he.setQueryDescription(((Describable) query).getDescription());
        }
        if (source instanceof Describable) {
            he.setScenarioDescription(((Describable) source).getDescription());
        }
        he.writeInput(SCENARIO, scenario);
        he.writeInput(QUERY, hyperquery);
        return he;
    }

    @Override
    protected Constructor<? extends HyperqueryExperiment> getPointConstructor(Point p) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Constructor<? extends HyperqueryExperiment> getEmptyConstructor(Point p) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Class<? extends HyperqueryExperiment> getClass(Point p) {
        // TODO Auto-generated method stub
        return null;
    }

}
