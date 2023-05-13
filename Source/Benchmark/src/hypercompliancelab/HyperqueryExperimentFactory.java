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
package hypercompliancelab;

import java.lang.reflect.Constructor;

import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.experiment.ExperimentFactory;
import ca.uqac.lif.labpal.region.Point;
import ca.uqac.lif.synthia.Seedable;
import hypercompliancelab.simple.NumberRunning;
import hypercompliancelab.simple.SameNumberDAggregation;
import hypercompliancelab.simple.SameNumberDQuantify;
import hypercompliancelab.simple.SimpleSource;
import hypercompliancelab.xes.HospitalSource;
import hypercompliancelab.xes.LazyInterleavedSource;
import hypercompliancelab.xes.LoanApplicationSource;
import hypercompliancelab.xes.WaboSource;

public class HyperqueryExperimentFactory extends ExperimentFactory<HyperqueryExperiment> implements Seedable
{
	protected int m_seed;
	
	protected LabFileSystem m_fs;
	
	public HyperqueryExperimentFactory(Laboratory lab, LabFileSystem fs)
	{
		super(lab);
		m_seed = 0;
		m_fs = fs;
	}
	
	@Override
	public HyperqueryExperimentFactory setSeed(int seed)
	{
		m_seed = seed;
		return this;
	}
	
	@Override
	/*@ pure null @*/ protected HyperqueryExperiment createExperiment(Point p)
	{
		Processor source = null;
		Processor query = null;
		String scenario = (String) p.get(SourceProvider.SCENARIO);
		String hyperquery = (String) p.get(HyperqueryProvider.QUERY);
		// Select the appropriate source
		switch (scenario)
		{
		case SimpleSource.NAME:
			source = new SimpleSource(10000, m_seed);
			break;
		case WaboSource.NAME:
			source = new WaboSource(m_fs);
			break;
		case HospitalSource.NAME:
			source = new HospitalSource(m_fs);
			break;
		case LoanApplicationSource.NAME:
			source = new LoanApplicationSource(m_fs);
			break;
		}
		// Select the appropriate hyperquery
		switch (hyperquery)
		{
		// We deliberately keep fully qualified class names to avoid confusion
		case hypercompliancelab.simple.AverageLength.NAME:
			query = new hypercompliancelab.simple.AverageLength();
			break;
		case NumberRunning.NAME:
			query = new NumberRunning();
			break;
		case SameNumberDAggregation.NAME:
			query = new SameNumberDAggregation();
			break;
		case SameNumberDQuantify.NAME:
			query = new SameNumberDQuantify();
			break;
		case hypercompliancelab.xes.AverageLength.NAME:
			query = new hypercompliancelab.xes.AverageLength(((LazyInterleavedSource) source).getEndCondition());
			break;
		case hypercompliancelab.xes.JaccardLog.NAME:
			query = new hypercompliancelab.xes.JaccardLog(((LazyInterleavedSource) source).getEndCondition());
			break;
		case hypercompliancelab.xes.LiveInstances.NAME:
			query = new hypercompliancelab.xes.LiveInstances(((LazyInterleavedSource) source).getEndCondition());
			break;
		case hypercompliancelab.xes.MeanInterval.NAME:
			query = new hypercompliancelab.xes.MeanInterval(((LazyInterleavedSource) source).getTimestamp());
			break;
		case hypercompliancelab.xes.SameNext.NAME:
			query = new hypercompliancelab.xes.SameNext(((LazyInterleavedSource) source).getAction());
			break;
		}
		if (source == null || query == null)
		{
			return null;
		}
		HyperqueryExperiment he = new HyperqueryExperiment(source, query);
		if (query instanceof Describable)
		{
			he.setQueryDescription(((Describable) query).getDescription());
		}
		if (source instanceof Describable)
		{
			he.setScenarioDescription(((Describable) source).getDescription());
		}
		he.writeInput(SourceProvider.SCENARIO, scenario);
		he.writeInput(HyperqueryProvider.QUERY, hyperquery);
		return he;
	}

	@Override
	protected Constructor<? extends HyperqueryExperiment> getPointConstructor(Point p)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Constructor<? extends HyperqueryExperiment> getEmptyConstructor(Point p)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Class<? extends HyperqueryExperiment> getClass(Point p)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
