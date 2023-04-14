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

import static ca.uqac.lif.labpal.region.ConditionalRegion.filter;
import static ca.uqac.lif.labpal.region.DiscreteRange.range;
import static ca.uqac.lif.labpal.region.ExtensionDomain.extension;
import static ca.uqac.lif.labpal.region.ProductRegion.product;
import static ca.uqac.lif.labpal.table.ExperimentTable.table;
import static ca.uqac.lif.labpal.table.TransformedTable.transform;
import static ca.uqac.lif.labpal.util.PermutationIterator.permute;

import static hypercompliancelab.HyperqueryExperiment.EVENTS;
import static hypercompliancelab.HyperqueryExperiment.MEMORY;
import static hypercompliancelab.HyperqueryExperiment.TIME;

import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.plot.Plot;
import ca.uqac.lif.labpal.region.Region;
import ca.uqac.lif.spreadsheet.chart.Chart.Axis;
import ca.uqac.lif.spreadsheet.chart.gnuplot.GnuplotScatterplot;
import ca.uqac.lif.spreadsheet.functions.ExpandAsColumns;
import hypercompliancelab.simple.AverageLength;
import hypercompliancelab.simple.NumberRunning;
import hypercompliancelab.simple.SameNumberDAggregation;
import hypercompliancelab.simple.SameNumberDQuantify;
import hypercompliancelab.simple.SimpleSource;

public class MainLab extends Laboratory
{
	@Override
	public void setup()
	{
		/* Setup a factory to get instances of experiments. */
		HyperqueryExperimentFactory factory = new HyperqueryExperimentFactory(this).setSeed(0);
		
		{
			// Experiments for the simple scenario
			Region simple_reg = product(
					extension(SourceProvider.SCENARIO, SimpleSource.NAME),
					extension(HyperqueryProvider.QUERY,
							AverageLength.NAME, NumberRunning.NAME, 
							SameNumberDAggregation.NAME, SameNumberDQuantify.NAME));
			add(new Plot(
					add(
							transform(
									table(HyperqueryProvider.QUERY, EVENTS, TIME).add(factory, simple_reg)
										.setTitle("Progressive elapsed time (simple scenario)"),
									new ExpandAsColumns(HyperqueryProvider.QUERY, TIME))),
					new GnuplotScatterplot().setCaption(Axis.Y, "Time (ms)")));
			add(new Plot(
					add(
							transform(
									table(HyperqueryProvider.QUERY, EVENTS, MEMORY).add(factory, simple_reg)
										.setTitle("Progressive memory consumption (simple scenario)"),
									new ExpandAsColumns(HyperqueryProvider.QUERY, MEMORY))),
					new GnuplotScatterplot().setCaption(Axis.Y, "Memory (B)")));
			
		}
	}
	
	public static void main(String[] args)
	{
		initialize(args, MainLab.class);
	}

}
