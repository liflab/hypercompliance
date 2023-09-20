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

/**
 * A repository of example pipelines illustrating the features of the
 * <i>Hypercompliance</i> palette.
 * <p>
 * In addition to BeepBeep's core engine, the examples in this package require
 * the following
 * <a href="https://github.com/liflab/beepbeep-3-palettes">palettes</a>:
 * <ul>
 * <li>Diagnostics</li>
 * <li>Ltl</li>
 * <li>Tuples</li>
 * </ul>
 * 
 * <h3>Examples contained in this project</h3>
 * <p>
 * The examples in this project can be categorized as follows.
 * 
 * <h4>Properties</h4>
 * <p>
 * A (compliance) <em>property</em> is a subset of &Sigma;*. Since properties
 * are not the focus of this palette, no example is given.
 * 
 * <h4>Hyperproperties</h4>
 * <p>
 * A <em>hyperproperty</em> is a generalization of a property, defined as a
 * subset of 2<sup>&Sigma;*</sup>, or stated otherwise, a function
 * <i>f</i> :  2<sup>&Sigma;*</sup> &rarr; {&top;,&bot;}.
 * <p>
 * Examples of hyperproperties in this repository:
 * <ul>
 * <li>{@link ConsistencyCondition}*</li>
 * <li>{@link EvilEmployee}*</li>
 * <li>{@link JaccardTraces}</li>
 * </ul>
 * (Examples marked with a * have been covered in the conference paper.)
 * 
 * <h4>Hyperqueries</h4>
 * <p>
 * A <em>hyperquery</em> is a generalization of a hyperproperty, defined as a
 * function <i>f</i> :  2<sup>&Sigma;*</sup> &rarr; <i>S</i> for an arbitrary
 * image <i>S</i>.
 * <p>
 * Examples of hyperqueries in this repository:
 * <ul>
 * <li>{@link AverageLength}</li>
 * <li>Directly follows*</li>
 * <li>{@link FractionEndInA}* (as a hyper-property)</li>
 * <li>Mean time interval*</li>
 * <li>{@link SameNext}*</li>
 * </ul>
 * 
 * <h4>History-aware hyperqueries</h4>
 * <p>
 * A <em>history-aware</em> hyperquery is a generalization of a hyperquery,
 * defined as a function <i>f</i> : (<i>I</i>&times;&Sigma;)* &rarr; <i>S</i>
 * for an arbitrary image <i>S</i> and a set of trace identifiers <i>I</i>. A
 * particular case of history-aware hyperquery is a history-aware
 * <em>hyperproperty</em>, where <i>S</i> = {&top;,&bot;}.
 * <p>
 * Examples of history-aware hyperproperties in this repository:
 * <ul>
 * <li>{@link IncreasingDuration}</li>
 * <li>{@link SameStateDirect}</li>
 * <li>{@link SuccessiveMoore}</li>
 * <li>{@link SuccessiveFailures}</li>
 * </ul>
 * <p>
 * Examples of history-aware hyperqueries in this repository:
 * <ul>
 * <li>{@link NumberRunning}* (as "Concurrent instances")</li>
 * <li>{@link OldestPending}</li>
 * </ul>
 */
package examples;