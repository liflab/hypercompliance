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
package examples;

import ca.uqac.lif.cep.Pushable;
import ca.uqac.lif.cep.functions.Cumulate;
import ca.uqac.lif.cep.functions.CumulativeFunction;
import ca.uqac.lif.cep.functions.TurnInto;
import ca.uqac.lif.cep.hypercompliance.Aggregate;
import ca.uqac.lif.cep.hypercompliance.LogUpdate;
import ca.uqac.lif.cep.hypercompliance.SliceLog.Choice;
import ca.uqac.lif.cep.mtnp.DrawPlot;
import ca.uqac.lif.cep.mtnp.UpdateTable;
import ca.uqac.lif.cep.mtnp.UpdateTableStream;
import ca.uqac.lif.cep.tmf.Fork;
import ca.uqac.lif.cep.util.Numbers;
import ca.uqac.lif.mtnp.plot.gral.Scatterplot;
import plots.BitmapJFrame;

import static ca.uqac.lif.cep.Connector.connect;

import ca.uqac.lif.cep.Connector;

/**
 * Example of a hyperquery that evaluates the number of currently
 * running (i.e. not complete) traces in a log.
 */
public class NumberRunning
{
	public static void main(String[] args)
	{
		/* We fork the input stream of log updates into two. */
		Fork f = new Fork();
		
		/* The first stream turns events into "1", and computes their
		 * cumulative sum, which creates a counter. */
		TurnInto one = new TurnInto(1);
		connect(f, 0, one, 0);
		Cumulate sum = new Cumulate(new CumulativeFunction<Number>(Numbers.addition));
		connect(one, sum);
		
		/* The second stream is an aggregation. Each trace inside a log is turned
		 * into a sequence of 1s, and last events of each active trace are added.
		 * This results in a count of the number of active traces at any given
		 * moment. */
		Aggregate a = new Aggregate(new TurnInto(1), Choice.ACTIVE, new Cumulate(new CumulativeFunction<Number>(Numbers.addition)), new Object[] {0});
		connect(f, 1, a, 0);
		
		/* The resulting x-stream and y-streams are then pushed into an
		 * {@link UpdateTableStream} processor. We instantiate this processor,
		 * telling it to create an empty {@link Table} object with two
		 * columns, called "Timestamp" and "Active instances". */
		UpdateTable table = new UpdateTableStream("Timestamp", "Active instances");
		connect(sum, 0, table, 0);
		connect(a, 0, table, 1);
		
		/* The next step is to create a plot out of the table's content.
		 * The {@link DrawPlot} processor receives a Table and passes it to
		 * a {@link ca.uqac.lif.mtnp.Plot Plot} object from the MTNP
		 * library. In our case, we want to create a scatterplot from the
		 * table's contents. */
		DrawPlot draw = new DrawPlot(new Scatterplot());
		Connector.connect(table, draw);
		
		/* Each event that comes out of the DrawPlot processor is an array
		 * of bytes corresponding to a bitmap image. To display that image,
		 * we use the BitmapJFrame processor, which opens a window and
		 * displays the image inside. */
		BitmapJFrame window = new BitmapJFrame();
		Connector.connect(draw, window);
		
		/* We need to call the start() method so that the window becomes
		 * visible. */
		window.start();
		
		Pushable p = f.getPushableInput();
		p.push(new LogUpdate(0, "b"));
		p.push(new LogUpdate(1, "b"));
		p.push(new LogUpdate(0, "a"));
		p.push(new LogUpdate(0, null)); // End of trace 0
		p.push(new LogUpdate(1, "b"));
		p.push(new LogUpdate(2, "a"));
		p.push(new LogUpdate(2, null)); // End of trace 2
		p.push(new LogUpdate(1, null)); // End of trace 1
	}
}
