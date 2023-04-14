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

import java.util.concurrent.locks.Lock;

import ca.uqac.lif.azrael.PrintException;
import ca.uqac.lif.azrael.PrintHandler;
import ca.uqac.lif.azrael.size.ReferencePrintHandler;
import ca.uqac.lif.azrael.size.SizePrinter;
import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.Pullable;
import ca.uqac.lif.cep.Pushable;

/**
 * A {@link SizePrinter} adapted to calculate the memory footprint of BeepBeep
 * processors. These objects present two challenges that require a modification
 * of the original size printer:
 * <ol>
 * <li>BeepBeep processors use instances of {@link ReentrantLock}, and
 * traversing these objects by reflection causes a stack overflow.</li>
 * <li>BeepBeep processors are connected to each other, which can cause the
 * same object to be counted multiple times. For example, a single instance of
 * {@link SynchronousProcessor.InputPushable} is stored in the processor
 * it belongs to, but also in the processor it is connected to.</li>
 * </ol>
 * The workaround is to stop the recursive traversal of objects by reflection
 * when these types of objects are encountered.
 * 
 * @author Sylvain Hallé
 *
 */
public class ProcessorSizePrinter extends SizePrinter
{
	public ProcessorSizePrinter()
	{
		this.addHandler(new LockHandler(this));
		this.addHandler(new EnumHandler(this));
		this.addHandler(new PullableHandler(this));
		this.addHandler(new PushableHandler(this));
		this.addHandler(new ClassHandler(this));
	}
	
	protected static class LockHandler extends ReferencePrintHandler
	{
		public LockHandler(SizePrinter printer)
		{
			super(printer);
		}

		@Override
		public boolean canHandle(Object o)
		{
			return o instanceof Lock;
		}

		@Override
		public Number getSize(Object o) throws PrintException
		{
			return OBJECT_SHELL_SIZE;
		}
	}
	
	protected static class ClassHandler extends ReferencePrintHandler
	{
		public ClassHandler(SizePrinter printer)
		{
			super(printer);
		}

		@Override
		public boolean canHandle(Object o)
		{
			return o instanceof Class;
		}

		@Override
		public Number getSize(Object o)
		{
			return OBJECT_SHELL_SIZE + 3 * INT_FIELD_SIZE + LONG_FIELD_SIZE + OBJREF_SIZE + 16;
		}

		@Override
		public void reset()
		{
			// Do nothing
		}
	}
	
	protected static class EnumHandler extends ReferencePrintHandler
	{
		public EnumHandler(SizePrinter printer)
		{
			super(printer);
		}

		@Override
		public boolean canHandle(Object o)
		{
			return o instanceof Enum;
		}

		@SuppressWarnings("rawtypes")
		@Override
		public Number getSize(Object o) throws PrintException
		{
			// An enum object has an int field and a string field that contains its name
			Enum e = (Enum) o;
			return OBJECT_SHELL_SIZE + INT_FIELD_SIZE + m_printer.print(e.name()).intValue();
		}

		@Override
		public void reset()
		{
			// Do nothing
		}
	}
	
	protected static class PullableHandler extends ReferencePrintHandler
	{
		public PullableHandler(SizePrinter printer)
		{
			super(printer);
		}

		@Override
		public boolean canHandle(Object o)
		{
			return o instanceof Pullable;
		}

		@Override
		public Number getSize(Object o) throws PrintException
		{
			return OBJECT_SHELL_SIZE;
		}

		@Override
		public void reset()
		{
			// Do nothing
		}
	}
	
	protected static class PushableHandler extends ReferencePrintHandler
	{
		public PushableHandler(SizePrinter printer)
		{
			super(printer);
		}

		@Override
		public boolean canHandle(Object o)
		{
			return o instanceof Pushable;
		}

		@Override
		public Number getSize(Object o) throws PrintException
		{
			return OBJECT_SHELL_SIZE;
		}

		@Override
		public void reset()
		{
			// Do nothing
		}
	}
	
	protected static class GroupProcessorHandler implements PrintHandler<Number>
	{
		@Override
		public boolean canHandle(Object o)
		{
			return o instanceof GroupProcessor;
		}

		@Override
		public Number handle(Object o) throws PrintException
		{
			return 0;
		}

		@Override
		public void reset()
		{
			// Do nothing
		}
	}
}
