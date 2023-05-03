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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeBooleanImpl;
import org.deckfour.xes.model.impl.XAttributeContinuousImpl;
import org.deckfour.xes.model.impl.XAttributeDiscreteImpl;
import org.deckfour.xes.model.impl.XAttributeImpl;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.deckfour.xes.model.impl.XAttributeTimestampImpl;

import ca.uqac.lif.cep.tuples.Tuple;
import ca.uqac.lif.cep.tuples.TupleMap;

/**
 * Reads a document in the <a href="https://xes-standard.org/">XES format</a>
 * and converts it into a {@link Log} object whose events are name-value
 * {@link Tuple}s.
 * 
 * @author Sylvain Hallé
 */
public class XesToLog
{
  /**
   * The parser used to read the XML document.
   */
  protected final XesXmlParser m_parser;
  
  /**
   * The name of the attribute in each trace that corresponds to the case
   * identifier.
   */
  protected final String m_caseId;
  
  /**
   * Creates a new instance of the transformer.
   * @param case_id The name of the attribute in each trace that corresponds
   * to the case identifier
   */
  public XesToLog(String case_id)
  {
    super();
    m_parser = new XesXmlParser();
    m_caseId = case_id;
  }
  
  /**
   * Gets the log instance obtained from reading an XES document.
   * @param is An input stream open at the start of an XES file
   * @return The log instance, or {@code null} if no log could be read from the
   * file.
   */
  /*@ null @*/ public Log getLog(/*@ non_null @*/ InputStream is)
  {
    XLog xlog = null;
    try
    {
      List<XLog> xlogs = m_parser.parse(is);
      xlog = xlogs.get(0);
    }
    catch (Exception e)
    {
      return null;
    }
    Log log = new Log();
    for (XTrace xtrace : xlog)
    {
      XAttributeMap trace_atts = xtrace.getAttributes();
      XAttribute trace_id = trace_atts.get(m_caseId);
      for (XEvent event : xtrace)
      {
        log.appendTo(castValue((XAttributeImpl) trace_id), new ArrayList<Object>(Arrays.asList(eventToTuple(event))));
      }
    }
    return log;
  }
  
  /**
   * Turns an XES event into a {@link Tuple}.
   * @param e The event
   * @return The tuple
   */
  protected static Tuple eventToTuple(XEvent e)
  {
    XAttributeMap attr_map = e.getAttributes();
    TupleMap tuple = new TupleMap();
    for (Map.Entry<String,XAttribute> entry : attr_map.entrySet())
    {
      tuple.put(entry.getKey(), castValue((XAttributeImpl) entry.getValue()));
    }
    return tuple;
  }
  
  /**
   * Casts a value extracted from the XES parser into its primitive Java type.
   * @param o The extracted value
   * @return The cast value
   */
  protected static Object castValue(XAttributeImpl o)
  {
    if (o instanceof XAttributeDiscreteImpl)
    {
      // Long
      return ((XAttributeDiscreteImpl) o).getValue();
    }
    if (o instanceof XAttributeLiteralImpl)
    {
      // String
      return ((XAttributeLiteralImpl) o).getValue();
    }
    if (o instanceof XAttributeBooleanImpl)
    {
      // Boolean
      return ((XAttributeBooleanImpl) o).getValue();
    }
    if (o instanceof XAttributeContinuousImpl)
    {
      // Double
      return ((XAttributeContinuousImpl) o).getValue();
    }
    if (o instanceof XAttributeTimestampImpl)
    {
      // Milliseconds
      return ((XAttributeTimestampImpl) o).getValueMillis();
    }
    return null;
  }
}
