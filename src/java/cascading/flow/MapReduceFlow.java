/*
 * Copyright (c) 2007-2008 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.cascading.org/
 *
 * This file is part of the Cascading project.
 *
 * Cascading is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cascading is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cascading.  If not, see <http://www.gnu.org/licenses/>.
 */

package cascading.flow;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import cascading.scheme.Scheme;
import cascading.tap.Hfs;
import cascading.tap.Tap;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.log4j.Logger;
import org.jgrapht.graph.SimpleDirectedGraph;

/**
 *
 */
public class MapReduceFlow extends Flow
  {
  /** Field LOG */
  private static final Logger LOG = Logger.getLogger( MapReduceFlow.class );

  private boolean deleteSinkOnInit = false;

  public MapReduceFlow( JobConf jobConf )
    {
    this( jobConf.getJobName(), jobConf, false );
    }

  public MapReduceFlow( String name, JobConf jobConf )
    {
    this( name, jobConf, false );
    }

  public MapReduceFlow( String name, JobConf jobConf, boolean deleteSinkOnInit )
    {
    this.deleteSinkOnInit = deleteSinkOnInit;

    setName( name );
//    setJobConf( jobConf );
    setSources( createSources( jobConf ) );
    setSinks( createSinks( jobConf ) );
    setTraps( createTraps( jobConf ) );
    setStepGraph( makeStepGraph( jobConf ) );
    }

  private SimpleDirectedGraph<FlowStep, Integer> makeStepGraph( JobConf jobConf )
    {
    SimpleDirectedGraph<FlowStep, Integer> stepGraph = new SimpleDirectedGraph<FlowStep, Integer>( Integer.class );

    Tap sink = getSinks().values().iterator().next();
    FlowStep step = new MapReduceFlowStep( sink.toString(), jobConf, sink );

    stepGraph.addVertex( step );

    return stepGraph;
    }

  private Map<String, Tap> createSources( JobConf jobConf )
    {
    Path[] paths = jobConf.getInputPaths();

    Map<String, Tap> taps = new HashMap<String, Tap>();

    for( Path path : paths )
      taps.put( path.toString(), new Hfs( new NullScheme(), path.toString() ) );

    return taps;
    }

  private Map<String, Tap> createSinks( JobConf jobConf )
    {
    Map<String, Tap> taps = new HashMap<String, Tap>();

    String path = jobConf.getOutputPath().toString();

    taps.put( path, new Hfs( new NullScheme(), path, deleteSinkOnInit ) );

    return taps;
    }

  private Map<String, Tap> createTraps( JobConf jobConf )
    {
    return new HashMap<String, Tap>();
    }

  public class NullScheme extends Scheme
    {
    public void sourceInit( Tap tap, JobConf conf ) throws IOException
      {
      }

    public void sinkInit( Tap tap, JobConf conf ) throws IOException
      {
      }

    public Tuple source( WritableComparable key, Writable value )
      {
      if( value instanceof Comparable )
        return new Tuple( key, (Comparable) value );
      else
        return new Tuple( key );
      }

    public void sink( Fields fields, Tuple tuple, OutputCollector outputCollector ) throws IOException
      {

      }
    }
  }
