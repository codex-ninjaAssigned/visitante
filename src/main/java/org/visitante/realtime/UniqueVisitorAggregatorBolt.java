/*
 * visitante: Web analytic using Hadoop Map Reduce and Storm
 * Author: Pranab Ghosh
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing, softwarSessionSummarizere
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.visitante.realtime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.chombo.storm.GenericBolt;
import org.chombo.storm.MessageHolder;
import org.chombo.storm.MessageQueue;
import org.chombo.util.ConfigUtility;

import backtype.storm.task.TopologyContext;
import backtype.storm.tuple.Tuple;

/**
 * Aggregates unique counts from multiple upstream bolts
 * @author pranab
 *
 */
public class UniqueVisitorAggregatorBolt extends  GenericBolt {
	private Map<String, Long> uniqueItemCounts = new HashMap<String, Long>();
	private int numUniqueCounterBolt;
	private MessageQueue msgQueue;
	
	@Override
	public Map<String, Object> getComponentConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void intialize(Map stormConf, TopologyContext context) {
		numUniqueCounterBolt = ConfigUtility.getInt(stormConf, "unique.count.bolt.threads", 1);		
		String countQueue = ConfigUtility.getString(stormConf, "output.count.queue");
		msgQueue = MessageQueue.createMessageQueue(stormConf, countQueue);
	}

	@Override
	public boolean process(Tuple input) {
		boolean status = true;
		String countingBoltID = input.getStringByField(UniqueVisitorTopology.BOLT_ID);
		if (uniqueItemCounts.containsKey(countingBoltID)) {
			//missed synchronization
			uniqueItemCounts.clear();
		}
		
		long uniqueCount = input.getLongByField(UniqueVisitorTopology.UNIQUE_COUNT);
		uniqueItemCounts.put(countingBoltID, uniqueCount);
		if (uniqueItemCounts.size() == numUniqueCounterBolt) {
			//write to queue
			long totalUniqueCount = 0;
			for (String boltID : uniqueItemCounts.keySet()) {
				totalUniqueCount += uniqueItemCounts.get(boltID);
			}
			msgQueue.send("" + totalUniqueCount);
			
			//clear cache
			uniqueItemCounts.clear();
		}
		
		return status;
	}

	@Override
	public List<MessageHolder> getOutput() {
		// TODO Auto-generated method stub
		return null;
	}

}
