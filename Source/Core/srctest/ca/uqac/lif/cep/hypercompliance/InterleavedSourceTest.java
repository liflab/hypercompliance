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

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

import ca.uqac.lif.cep.Pullable;
import ca.uqac.lif.cep.tuples.FixedTupleBuilder;
import ca.uqac.lif.cep.tuples.Tuple;

/**
 * Unit tests for {@link InterleavedSource}.
 */
public class InterleavedSourceTest
{
  @Test
  public void test1()
  {
    FixedTupleBuilder builder = new FixedTupleBuilder("foo", "ts");
    Log log = new Log();
    log.appendTo(0, Arrays.asList(
        builder.createTuple("abc", 0),
        builder.createTuple("def", 10)));
    log.appendTo(1, Arrays.asList(
        builder.createTuple("baz", 1),
        builder.createTuple("biz", 9)));
    log.appendTo(2, Arrays.asList(
        builder.createTuple("buz", 3)));
    InterleavedSource is = new InterleavedSource(log, "ts");
    Pullable p = is.getPullableOutput();
    LogUpdate upd;
    upd = (LogUpdate) p.pull();
    assertEquals(0, upd.getId());
    assertEquals("abc", ((Tuple) upd.getEvent()).get("foo"));
    upd = (LogUpdate) p.pull();
    assertEquals(1, upd.getId());
    assertEquals("baz", ((Tuple) upd.getEvent()).get("foo"));
    upd = (LogUpdate) p.pull();
    assertEquals(2, upd.getId());
    assertEquals("buz", ((Tuple) upd.getEvent()).get("foo"));
    upd = (LogUpdate) p.pull();
    assertEquals(1, upd.getId());
    assertEquals("biz", ((Tuple) upd.getEvent()).get("foo"));
    upd = (LogUpdate) p.pull();
    assertEquals(0, upd.getId());
    assertEquals("def", ((Tuple) upd.getEvent()).get("foo"));
  }
}
