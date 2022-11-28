package de.jade.ecs.map.xychart;

import org.scheduler.Scheduler;
import org.scheduler.SchedulerConfiguration;
import org.scheduler.agent.Agent;
import org.scheduler.agent.behaviour.LinearDrivingBehaviour;
import org.scheduler.agent.behaviour.NmeaPosReportUdpOutputBehaviour;
import org.scheduler.agent.state.ShipState;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DynamicSimulation implements Runnable {
	public static Map<Integer, ShipState> shipStatements = new HashMap<>();
	public static int count = 0;

	@Override
	public void run() {

		double freq = 1;
		double simulationSpeed = 10;
		Scheduler scheduler = new Scheduler(new SchedulerConfiguration(freq, simulationSpeed));

/*		*//** Ship A **//*
		ShipState shipStateA = new ShipState(11111111, new Point2D.Double(53.89741, 7.545902), 76, 14);
		Agent agentA = new Agent(scheduler, new LinearDrivingBehaviour(shipStateA), new NmeaPosReportUdpOutputBehaviour(shipStateA, 2947));
		scheduler.registerAgent(agentA);*/

		/** Ship A **/
		ShipState shipStateA = new ShipState(11111111, new Point2D.Double(7.545902, 53.89741), 76, 14);
		Agent agentA = new Agent(scheduler, new LinearDrivingBehaviour(shipStateA), new NmeaPosReportUdpOutputBehaviour(shipStateA, 2948));
		scheduler.registerAgent(agentA);

		/** Ship B **/
		ShipState shipStateB = new ShipState(22222222, new Point2D.Double(7.624865, 53.96585), 136, 10);
		Agent agentB = new Agent(scheduler, new LinearDrivingBehaviour(shipStateB), new NmeaPosReportUdpOutputBehaviour(shipStateB, 2948));
		scheduler.registerAgent(agentB);

		/** Ship c **/
		ShipState shipStateC = new ShipState(33333333, new Point2D.Double(7.545902, 53.91741), 77, 14);
		Agent agentC = new Agent(scheduler, new LinearDrivingBehaviour(shipStateC), new NmeaPosReportUdpOutputBehaviour(shipStateC, 2948));
		scheduler.registerAgent(agentC);

		scheduler.start();


		while (true) {
			try {
				shipStatements.put(shipStateA.getMMSI(), shipStateA);
				shipStatements.put(shipStateB.getMMSI(), shipStateB);
				shipStatements.put(shipStateC.getMMSI(), shipStateC);
				TimeUnit.SECONDS.sleep(10);
				count++;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}



/*		try {
			TimeUnit.SECONDS.sleep(60);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/
/*		scheduler.stop();*/
	}
}


