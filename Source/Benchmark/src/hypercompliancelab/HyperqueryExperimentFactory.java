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
import hypercompliancelab.simple.AverageLength;
import hypercompliancelab.simple.NumberRunning;
import hypercompliancelab.simple.SimpleSource;

public class HyperqueryExperimentFactory extends ExperimentFactory<HyperqueryExperiment>
{
	public HyperqueryExperimentFactory(Laboratory lab)
	{
		super(lab);
	}
	
	@Override
	/*@ pure null @*/ protected HyperqueryExperiment createExperiment(Point p)
	{
		Processor source = null;
		Processor query = null;
		String scenario = (String) p.get(SourceProvider.SCENARIO);
		if (scenario.compareTo(SimpleSource.NAME) == 0)
		{
			source = new SimpleSource(10000);
		}
		String hyperquery = (String) p.get(HyperqueryProvider.QUERY);
		switch (hyperquery)
		{
		case AverageLength.NAME:
			query = new AverageLength();
			break;
		case NumberRunning.NAME:
			query = new NumberRunning();
			break;
		}
		if (source == null || query == null)
		{
			return null;
		}
		HyperqueryExperiment he = new HyperqueryExperiment(source, query);
		he.writeInput(SourceProvider.SCENARIO, scenario);
		he.writeInput(HyperqueryProvider.QUERY, hyperquery);
		he.setSourceLength(10000);
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
