package org.scheduler.agent.behaviour;

import org.scheduler.Tick;
import org.scheduler.agent.Agent;

import java.time.temporal.TemporalAdjuster;

public interface ITickExecution {
	
	/**
	 * executes a {@link Tick}. The {@link Tick} contains a {@link TemporalAdjuster} that represents the duration/timespan that is to be executed
	 * @param tick
	 */
	public void execute(Tick tick);
	
	/**
	 * set the agent, which is owning this ITickExecution instance
	 * @param agent
	 */
	public void setAgent(Agent agent);

}
