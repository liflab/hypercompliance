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
	 * A flag indicating if quantifiers should be evaluated only on completed
	 * traces.
	 */
	protected final boolean m_onlyCompleted;
	
	/**
	 * A flag indicating if successive quantifiers apply to distinct traces.
	 */
	protected final boolean m_distinct;

	/**
	 * An enumerated type used to designate each quantifier in an expression.
	 * Possible values are:
	 * <ul>
	 * <li>{@code ALL}: universal quantification over traces in the log</li>
	 * <li>{@code SOME}: existential quantification over traces in the log</li>
	 * </ul>
	 */
	/*@ non_null @*/ public static enum QuantifierType {ALL, SOME}

	public Quantify(/*@ non_null @*/ Processor phi, boolean completed, /*@ non_null @*/ QuantifierType ... quantifiers)
	{
		super(1, 1);
		m_quantifiers = quantifiers;
		m_phi = phi;
		m_inputLog = new ArrayList<LogUpdate>();
		m_onlyCompleted = completed;
		if (m_quantifiers[0] == QuantifierType.ALL)
		{
			m_root = new UniversalNode(0);
		}
		else
		{
			m_root = new ExistentialNode(0);
		}
		m_distinct = true;
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
		 * The edge from the parent of this node to this node.
		 */
		/*@ non_null @*/ protected QuantifierEdge m_parentEdge;

		/**
		 * The nesting level of this quantifier.
		 */
		protected final int m_level;
		
		/**
		 * The set of trace identifiers seen so far.
		 */
		/*@ non_null @*/ protected final Set<Object> m_seenIdentifiers;

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
			m_parentEdge = null;
		}
		
		/**
		 * Sets the edge that goes from the parent of this node to this node.
		 * @param e The edge
		 */
		public void setParentEdge(QuantifierEdge e)
		{
			m_parentEdge = e;
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
				m_children.add(new QuantifierEdge(this, trace_id, new_child));
				// Push all history into this new sub-tree
				for (LogUpdate e : m_inputLog)
				{
					new_child.push(e);
				}
				m_seenIdentifiers.add(trace_id);
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
		
		@Override
		public String toString()
		{
			StringBuilder out = new StringBuilder();
			toString(out, "", "");
			return out.toString();
		}
		
		protected void toString(StringBuilder out, String edge, String indent)
		{
			out.append(indent).append(edge).append(" ").append(getSymbol()).append("\n");
			String new_indent = indent + " ";
			for (QuantifierEdge e : m_children)
			{
				e.getDestination().toString(out, e.toString(), new_indent);
			}
		}
		
		/*@ non_null @*/ protected abstract String getSymbol();

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
				new_child = new UniversalNode(level);
			}
			else
			{
				new_child = new ExistentialNode(level);
			}
			for (Object id : m_seenIdentifiers)
			{
				QuantifierNode n = copy(level + 1, new_trace_id);
				new_child.m_children.add(new QuantifierEdge(new_child, id, n));
			}
			{
				QuantifierNode n = copy(level + 1, new_trace_id);
				new_child.m_children.add(new QuantifierEdge(new_child, new_trace_id, n));
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
		 * The source node of this edge.
		 */
		/*@ non_null @*/ protected final QuantifierNode m_source;

		/**
		 * The destination node of this edge.
		 */
		/*@ non_null @*/ protected final QuantifierNode m_destination;

		/**
		 * Creates a new quantifier edge.
		 * @param source The source node of this edge
		 * @param trace_id The trace identifier this quantifier edge is associated
		 * with
		 * @param destination The destination node of this edge
		 */
		public QuantifierEdge(/*@ non_null @*/ QuantifierNode source, /*@ non_null @*/ Object trace_id, /*@ non_null @*/ QuantifierNode destination)
		{
			super();
			m_source = source;
			m_traceIdentifier = trace_id;
			m_destination = destination;
			m_destination.setParentEdge(this);
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
		 * Gets the source node of this edge.
		 * @return The node
		 */
		/*@ pure non_null @*/ public QuantifierNode getSource()
		{
			return m_source;
		}
		
		/**
		 * Gets the label node of this edge.
		 * @return The label
		 */
		/*@ pure non_null @*/ public Object getLabel()
		{
			return m_traceIdentifier;
		}

		/**
		 * Gets the destination node of this edge.
		 * @return The node
		 */
		/*@ pure non_null @*/ public QuantifierNode getDestination()
		{
			return m_destination;
		}
		
		@Override
		public String toString()
		{
			return m_traceIdentifier.toString();
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
			if (m_children.isEmpty())
			{
				return null;
			}
			boolean all_true = true;
			for (QuantifierEdge e : m_children)
			{
				Value v = e.getDestination().getVerdict();
				if (v == Value.FALSE)
				{
					return Value.FALSE;
				}
				if (v == null || v == Value.INCONCLUSIVE)
				{
					all_true = false;
				}
			}
			return all_true ? Value.TRUE : Value.INCONCLUSIVE;
		}
		
		@Override
		protected String getSymbol()
		{
			return "\u2200";
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
			if (m_children.isEmpty())
			{
				return null;
			}
			boolean all_false = true;
			for (QuantifierEdge e : m_children)
			{
				Value v = e.getDestination().getVerdict();
				if (v == Value.TRUE)
				{
					return Value.TRUE;
				}
				if (v == null || v == Value.INCONCLUSIVE)
				{
					all_false = false;
				}
			}
			return all_false ? Value.FALSE : Value.INCONCLUSIVE;
		}
		
		@Override
		protected String getSymbol()
		{
			return "\u2203";
		}
	}

	protected class LeafNode extends QuantifierNode
	{
		/*@ non_null @*/ protected final Pushable[] m_pushables;

		/*@ non_null @*/ protected final Processor m_phiInstance;

		/*@ non_null @*/ protected final QueueSink m_sink;
		
		/*@ non_null @*/ protected final boolean[] m_completed;
		
		protected boolean m_allCompleted;
		
		/*@ null @*/ protected Troolean.Value m_verdict;

		LeafNode(int level)
		{
			super(level);
			m_phiInstance = Quantify.this.m_phi.duplicate();
			m_pushables = new Pushable[m_phiInstance.getInputArity()];
			m_completed = new boolean[m_pushables.length];
			for (int i = 0; i < m_pushables.length; i++)
			{
				m_pushables[i] = m_phiInstance.getPushableInput(i);
				m_completed[i] = false;
			}
			m_allCompleted = false;
			m_sink = new QueueSink();
			Connector.connect(m_phiInstance, m_sink);
			m_verdict = null;
		}
		
		@Override
		protected String getSymbol()
		{
			return "P";
		}

		@Override
		protected void push(LogUpdate event, Set<Integer> positions)
		{
			Object o = event.getEvent();
			for (int i : positions)
			{
				if (o == null)
				{
					m_pushables[i].notifyEndOfTrace();
					m_completed[i] = true;
				}
				else
				{
					m_pushables[i].push(o);
				}
			}
			if (o == null)
			{
				m_allCompleted = allCompleted();
			}
		}
		
		protected boolean allCompleted()
		{
			for (boolean b : m_completed)
			{
				if (!b)
				{
					return false;
				}
			}
			return true;
		}

		@Override
		public Value getVerdict()
		{
			if (m_onlyCompleted && !m_allCompleted)
			{
				return null;
			}
			Queue<?> q = m_sink.getQueue();
			if (!q.isEmpty())
			{
				m_verdict = (Value) q.remove();
			}
			return m_verdict;
		}
	}
}
