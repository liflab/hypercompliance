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
package ca.uqac.lif.cep.hypercompliance;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.Pushable;
import ca.uqac.lif.cep.SynchronousProcessor;
import ca.uqac.lif.cep.ltl.Troolean;
import ca.uqac.lif.cep.ltl.Troolean.Value;
import ca.uqac.lif.cep.tmf.QueueSink;

public class Quantify extends SynchronousProcessor
{
	/*@ non_null @*/ protected final QuantifierType[] m_quantifiers;

	/*@ non_null @*/ protected final List<LogUpdate> m_inputLog;

	/**
	 * The processor to evaluate on n-uplets of traces.
	 */
	/*@ non_null @*/ protected final Processor m_phi;

	/**
	 * The root node of the quantifier node structure.
	 */
	/*@ non_null @*/ protected final QuantifierNode m_root;

	/**
	 * An enumerated type used to designate each quantifier in an expression.
	 * Possible values are:
	 * <ul>
	 * <li>{@code ALL}: universal quantification over traces in the log</li>
	 * <li>{@code SOME}: existential quantification over traces in the log</li>
	 * </ul>
	 */
	/*@ non_null @*/ public static enum QuantifierType {ALL, SOME}

	public Quantify(/*@ non_null @*/ Processor phi, /*@ non_null @*/ QuantifierType ... quantifiers)
	{
		super(1, 1);
		m_quantifiers = quantifiers;
		m_phi = phi;
		m_inputLog = new ArrayList<LogUpdate>();
		if (m_quantifiers[0] == QuantifierType.ALL)
		{
			m_root = new UniversalNode(0);
		}
		else
		{
			m_root = new ExistentialNode(0);
		}
	}

	@Override
	protected boolean compute(Object[] inputs, Queue<Object[]> outputs)
	{
		LogUpdate upd = (LogUpdate) inputs[0];
		m_root.push(upd);
		Troolean.Value verdict = m_root.getVerdict();
		m_inputLog.add(upd);
		outputs.add(new Object[] {verdict});
		return true;
	}

	@Override
	public Processor duplicate(boolean with_state)
	{
		throw new UnsupportedOperationException("This processor cannot be duplicated");
	}

	protected abstract class QuantifierNode
	{
		/**
		 * The list of children of this node.
		 */
		/*@ non_null @*/ protected final List<QuantifierEdge> m_children;

		/**
		 * The nesting level of this quantifier.
		 */
		protected final int m_level;
		
		/**
		 * The set of trace identifiers seen so far.
		 */
		protected final Set<Object> m_seenIdentifiers;

		/**
		 * Creates a new quantifier node.
		 * @param level The nesting level of this quantifier
		 */
		QuantifierNode(int level)
		{
			super();
			m_children = new ArrayList<QuantifierEdge>();
			m_level = level;
			m_seenIdentifiers = new HashSet<Object>();
		}

		/**
		 * Determines if the current node has an edge labelled with a given trace
		 * identifier.
		 * @param id The trace identifier
		 * @return {@code true} if the node has such an edge, {@code false}
		 * otherwise
		 */
		/*@ pure @*/ protected boolean hasEdgeFor(/*@ null @*/ Object id)
		{
			for (QuantifierEdge e : m_children)
			{
				if (e.isFor(id))
				{
					return true;
				}
			}
			return false;
		}

		/**
		 * Pushes a new event into the quantifier structure.
		 * @param u The log update event
		 */
		public final void push(LogUpdate u)
		{
			push(u, new HashSet<Integer>());
		}

		protected void push(LogUpdate u, Set<Integer> positions)
		{
		  Object trace_id = u.getId();
			if (!m_seenIdentifiers.contains(trace_id))
			{
				// First spawn a copy of the sub-tree and append it as a new child
				QuantifierNode new_child = copy(m_level + 1, trace_id);
				m_children.add(new QuantifierEdge(trace_id, new_child));
				// Push all history into this new sub-tree
				for (LogUpdate e : m_inputLog)
				{
					new_child.push(e);
				}
			}
			// Then push new event into all children
			for (QuantifierEdge e : m_children)
			{
				if (e.isFor(trace_id))
				{
					Set<Integer> new_positions = new HashSet<Integer>();
					new_positions.addAll(positions);
					new_positions.add(m_level);
					e.m_destination.push(u, new_positions);
				}
				else
				{
					// Don't bother creating a new set if we don't add anything
					e.m_destination.push(u, positions);
				}
			}
		}

		/**
		 * Gets the current verdict of this quantifier node.
		 * @return The verdict
		 */
		/*@ non_null @*/ public abstract Troolean.Value getVerdict();

		protected QuantifierNode copy(int level, Object new_trace_id)
		{
			if (level == m_quantifiers.length)
			{
				return new LeafNode(level + 1);
			}
			QuantifierType qt = m_quantifiers[level];
			QuantifierNode new_child = null;
			if (qt == QuantifierType.ALL)
			{
				new_child = new UniversalNode(m_level);
			}
			else
			{
				new_child = new ExistentialNode(m_level);
			}
			for (Object id : m_seenIdentifiers)
			{
				QuantifierNode n = copy(level + 1, new_trace_id);
				new_child.m_children.add(new QuantifierEdge(id, n));
			}
			{
				QuantifierNode n = copy(level + 1, new_trace_id);
				new_child.m_children.add(new QuantifierEdge(new_trace_id, n));
			}
			return new_child;
		}
	}

	/**
	 * A labelled edge between two quantifier nodes.
	 */
	protected class QuantifierEdge
	{
		/**
		 * The trace identifier this quantifier edge is associated with.
		 */
		/*@ non_null @*/ protected final Object m_traceIdentifier;

		/**
		 * The destination node of this edge.
		 */
		/*@ non_null @*/ protected final QuantifierNode m_destination;

		/**
		 * Creates a new quantifier edge.
		 * @param trace_id The trace identifier this quantifier edge is associated
		 * with
		 * @param destination The destination node of this edge
		 */
		public QuantifierEdge(/*@ non_null @*/ Object trace_id, /*@ non_null @*/ QuantifierNode destination)
		{
			super();
			m_traceIdentifier = trace_id;
			m_destination = destination;
		}

		/**
		 * Determines if this edge has the same label as a given trace identifier.
		 * @param id The trace identifier
		 * @return {@code true} if the edge has the same label, {@code false}
		 * otherwise
		 */
		/*@ pure @*/ public boolean isFor(/*@ null @*/ Object id)
		{
			return m_traceIdentifier.equals(id);
		}

		/**
		 * Gets the destination node of this edge.
		 * @return The node
		 */
		/*@ pure non_null @*/ public QuantifierNode getDestination()
		{
			return m_destination;
		}
	}

	/**
	 * A quantifier node applying universal quantification.
	 */
	protected class UniversalNode extends QuantifierNode
	{
		/**
		 * Creates a new universal node.
		 * @param level The nesting level of this quantifier
		 */
		public UniversalNode(int level)
		{
			super(level);
		}

		@Override
		public Value getVerdict()
		{
			boolean all_true = true;
			for (QuantifierEdge e : m_children)
			{
				Value v = e.getDestination().getVerdict();
				if (v == Value.FALSE)
				{
					return Value.FALSE;
				}
				if (v == Value.INCONCLUSIVE)
				{
					all_true = false;
				}
			}
			return all_true ? Value.TRUE : Value.INCONCLUSIVE;
		}
	}

	/**
	 * A quantifier node applying universal quantification.
	 */
	protected class ExistentialNode extends QuantifierNode
	{
		/**
		 * Creates a new existential node.
		 * @param level The nesting level of this quantifier
		 */
		public ExistentialNode(int level)
		{
			super(level);
		}

		@Override
		public Value getVerdict()
		{
			boolean all_false = true;
			for (QuantifierEdge e : m_children)
			{
				Value v = e.getDestination().getVerdict();
				if (v == Value.TRUE)
				{
					return Value.TRUE;
				}
				if (v == Value.INCONCLUSIVE)
				{
					all_false = false;
				}
			}
			return all_false ? Value.FALSE : Value.INCONCLUSIVE;
		}
	}

	protected class LeafNode extends QuantifierNode
	{
		/*@ non_null @*/ protected final Pushable[] m_pushables;

		/*@ non_null @*/ protected final Processor m_phiInstance;

		/*@ non_null @*/ protected final QueueSink m_sink;
		
		/*@ null @*/ protected Troolean.Value m_verdict;

		LeafNode(int level)
		{
			super(level);
			m_phiInstance = Quantify.this.m_phi.duplicate();
			m_pushables = new Pushable[m_phiInstance.getInputArity()];
			for (int i = 0; i < m_pushables.length; i++)
			{
				m_pushables[i] = m_phiInstance.getPushableInput(i);
			}
			m_sink = new QueueSink();
			Connector.connect(m_phiInstance, m_sink);
			m_verdict = null;
		}

		@Override
		protected void push(LogUpdate event, Set<Integer> positions)
		{
			for (int i : positions)
			{
				m_pushables[i].push(event.getEvent());
			}
		}

		@Override
		public Value getVerdict()
		{
			Queue<?> q = m_sink.getQueue();
			if (!q.isEmpty())
			{
				m_verdict = (Value) q.remove();
			}
			return m_verdict;
		}
	}
}
