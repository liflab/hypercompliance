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


import static ca.uqac.lif.labpal.region.ExtensionDomain.extension;
import static ca.uqac.lif.labpal.region.ProductRegion.product;
import static ca.uqac.lif.labpal.table.ExperimentTable.table;
import static ca.uqac.lif.labpal.table.TransformedTable.transform;
import static ca.uqac.lif.labpal.latex.LatexExportable.latexify;

import static hypercompliancelab.HyperqueryExperiment.EVENTS;
import static hypercompliancelab.HyperqueryExperiment.MAX_MEMORY;
import static hypercompliancelab.HyperqueryExperiment.MEMORY;
import static hypercompliancelab.HyperqueryExperiment.THROUGHPUT;
import static hypercompliancelab.HyperqueryExperiment.TIME;
import static hypercompliancelab.HyperqueryExperiment.TOTAL_EVENTS;
import static hypercompliancelab.HyperqueryExperimentFactory.QUERY;
import static hypercompliancelab.HyperqueryExperimentFactory.SCENARIO;

import ca.uqac.lif.fs.FileSystemException;
import ca.uqac.lif.fs.FileUtils;
import ca.uqac.lif.fs.JarFile;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.experiment.ExperimentGroup;
import ca.uqac.lif.labpal.plot.Plot;
import ca.uqac.lif.labpal.region.Region;
import ca.uqac.lif.labpal.util.CliParser;
import ca.uqac.lif.labpal.util.CliParser.Argument;
import ca.uqac.lif.labpal.util.CliParser.ArgumentMap;
import ca.uqac.lif.spreadsheet.chart.Chart.Axis;
import ca.uqac.lif.spreadsheet.chart.gnuplot.GnuplotScatterplot;
import ca.uqac.lif.spreadsheet.functions.ExpandAsColumns;
import ca.uqac.lif.spreadsheet.functions.Sort;
import hypercompliancelab.school.SchoolAdmissionExperimentFactory;
import hypercompliancelab.school.SchoolAdmissionSource;
import hypercompliancelab.school.properties.*;
import hypercompliancelab.simple.NumberRunning;
import hypercompliancelab.simple.SameNumberDAggregation;
import hypercompliancelab.simple.SameNumberDQuantify;
import hypercompliancelab.simple.SimpleSource;

import hypercompliancelab.xes.*;

public class MainLab extends Laboratory {
	@Override
	public void setup() {
		// Lab metadata
		setName("BeepBeep Hypercompliance Benchmark");
		setAuthor("Sylvain Hallé, Chukri Soueidi");
		setDoi("10.5281/zenodo.8303080");
		writeDescription("description.html");

		// The local folder where downloaded files are stored
		String data_dir = "data/";

		/* Process command line parameters */
		ArgumentMap args = getCliArguments();
		if (args.hasOption("datadir")) {
			data_dir = args.getOptionValue("datadir");
			if (!data_dir.endsWith("/")) {
				data_dir += "/";
			}
		}

		LabFileSystem fs;
		try {
			fs = new LabFileSystem(".").open();
			fs.mkdir(data_dir);
			fs.chdir(data_dir);
		} catch (FileSystemException e) {
			System.err.println(e);
			return;
		}

		/* Setup a factory to get instances of experiments. */
		HyperqueryExperimentFactory factory = new HyperqueryExperimentFactory(this, fs).setSeed(0);

		{
			// Experiments for the simple scenario (auto-generated)
			ExperimentGroup g = new ExperimentGroup("Simple scenario", "Hyperqueries evaluated on an abstract auto-generated log.");
			add(g);
			Region simple_reg = product(
					extension(SCENARIO, SimpleSource.NAME),
					extension(QUERY,
							hypercompliancelab.simple.AverageLength.NAME, NumberRunning.NAME,
							SameNumberDAggregation.NAME, SameNumberDQuantify.NAME));
			add(new Plot(
					add(
							transform(
									table(HyperqueryExperimentFactory.QUERY, EVENTS, TIME).add(factory, simple_reg)
									.setTitle("Progressive elapsed time (simple scenario)")
									.setNickname(latexify("tTimeSimple")),
									new ExpandAsColumns(QUERY, TIME))),
					new GnuplotScatterplot().setCaption(Axis.Y, "Time (ms)")));
			add(new Plot(
					add(
							transform(
									table(QUERY, EVENTS, MEMORY).add(factory, simple_reg)
									.setTitle("Progressive memory consumption (simple scenario)")
									.setNickname(latexify("tMemorySimple")),
									new ExpandAsColumns(QUERY, MEMORY))),
					new GnuplotScatterplot().setCaption(Axis.Y, "Memory (B)")));
			for (Region e_r : simple_reg.all(SCENARIO, QUERY))
				g.add(factory.get(e_r));
		}



		{


			// Experiments for a set of pre-recorded XES logs from external sources
			ExperimentGroup g = new ExperimentGroup("Real-world logs", "Hyperqueries evaluated on a set of XES files retrieved from online repositories.");
			add(g);
			Region xes_reg = product(
					extension(SCENARIO, WaboSource.NAME, HospitalSource.NAME, LoanApplicationSource.NAME),
					extension(QUERY,
							hypercompliancelab.xes.AverageLength.NAME,
							hypercompliancelab.xes.DirectlyFollows.NAME,
							//							hypercompliancelab.xes.JaccardLog.NAME,
							LiveInstances.NAME,
							hypercompliancelab.xes.MeanInterval.NAME,
							hypercompliancelab.xes.SameNext.NAME)
					);
			add(transform(
					table(SCENARIO, TOTAL_EVENTS, QUERY, THROUGHPUT, MAX_MEMORY).add(factory, xes_reg)
					.setTitle("Aggregate statistics for various real-world logs").setNickname(latexify("tAggregateXES")), new Sort().by(0).excludeFirst()));
			for (Region e_r : xes_reg.all(SCENARIO)) {
				String scenario = e_r.asPoint().getString(SCENARIO);
				add(new Plot(
						add(transform(table(QUERY, EVENTS, TIME).add(factory, e_r), new ExpandAsColumns(QUERY, TIME))
								.setTitle("Running time for hyperqueries of scenario " + scenario).setNickname(latexify("tTime" + scenario))),
						new GnuplotScatterplot().setCaption(Axis.Y, "Time (ms)")));
				add(new Plot(
						add(transform(table(QUERY, EVENTS, MEMORY).add(factory, e_r), new ExpandAsColumns(QUERY, MEMORY))
								.setTitle("Memory usage for hyperqueries of scenario " + scenario).setNickname(latexify("tMemory" + scenario))),
						new GnuplotScatterplot().setCaption(Axis.Y, "Memory (B)")));
			}
			for (Region e_r : xes_reg.all(SCENARIO, QUERY))
				g.add(factory.get(e_r));
		}

		{
			SchoolAdmissionExperimentFactory s_factory = new SchoolAdmissionExperimentFactory(this, fs).setSeed(0);


			ExperimentGroup g = new ExperimentGroup("School Admission ", "Hyperqueries evaluated on logs from the School Admission scenario.");
			add(g);
			Region admissions_reg = product(
					extension(SCENARIO, SchoolAdmissionSource.NAME),
					extension(QUERY,
							LiveInstances.NAME,
							EvilEmployee.NAME,
							StaffLoad.NAME,
							ConsistencyCondition.NAME,
							BalancedLoad.NAME,
							AcceptanceRate.NAME,
							SameState.NAME
							)
					);

			add(table(SCENARIO, TOTAL_EVENTS,
					QUERY, THROUGHPUT, MAX_MEMORY)
					.add(s_factory, admissions_reg)
					.setTitle("Aggregate statistics for school admission logs").setNickname("tSchoolAggregate"));
			for (Region e_r : admissions_reg.all(SCENARIO)) {
				String scenario = e_r.asPoint().getString(SCENARIO);
				add(new Plot(
						add(transform(table(QUERY, EVENTS, TIME).add(s_factory, e_r), new ExpandAsColumns(QUERY, TIME))
								.setTitle("Running time for hyperqueries of scenario " + scenario).setNickname(latexify("tTime" + scenario))),
						new GnuplotScatterplot().setCaption(Axis.Y, "Time (ms)")));
				add(new Plot(
						add(transform(table(QUERY, EVENTS, MEMORY).add(s_factory, e_r), new ExpandAsColumns(QUERY, MEMORY))
								.setTitle("Memory usage for hyperqueries of scenario " + scenario).setNickname(latexify("tMemory" + scenario))),
						new GnuplotScatterplot().setCaption(Axis.Y, "Memory (B)")));
			}
			for (Region e_r : admissions_reg.all(SCENARIO, QUERY))
				g.add(s_factory.get(e_r));


		}
	}

	@Override
	public void setupCli(CliParser parser) {
		parser.addArgument(new Argument().withLongName("datadir").withArgument("dir").withDescription("Store downloaded data files into local folder dir"));
	}

	public void writeDescription(String path) {
		try {
			JarFile jf = new JarFile(MainLab.class).open();
			setDescription(FileUtils.readStringFrom(jf, path));
			jf.close();
		} catch (FileSystemException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		initialize(args, MainLab.class);
	}


}
