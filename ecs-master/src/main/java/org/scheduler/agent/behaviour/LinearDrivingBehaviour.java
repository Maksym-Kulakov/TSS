package org.scheduler.agent.behaviour;

import org.apache.sis.referencing.CommonCRS;
import org.apache.sis.referencing.GeodeticCalculator;
import org.opengis.geometry.DirectPosition;
import org.scheduler.Tick;
import org.scheduler.agent.Agent;
import org.scheduler.agent.state.ShipState;

import java.awt.geom.Point2D;
import java.time.LocalTime;

/** LinearDrivingBehaviour
 * 
 * Sailes ship as set in given shipState
 * 
 * @author chris
 *
 */
public class LinearDrivingBehaviour implements ITickExecution {

	private ShipState shipState = null;
	
	private GeodeticCalculator geoCalc = GeodeticCalculator.create(CommonCRS.WGS84.geographic());

	@SuppressWarnings("unused")
	private Agent agent = null;

	public LinearDrivingBehaviour(ShipState shipState) {
		this.shipState  = shipState;
	}

	@Override
	public void execute(Tick tick) {
		geoCalc.setStartGeographicPoint(shipState.getPoint().getY(), shipState.getPoint().getX());
		LocalTime lt = LocalTime.of(0,0,0,0);
		lt = (LocalTime) tick.getTemporalAdjuster().adjustInto(lt);
		long milliOfDay = lt.toNanoOfDay() / 1_000_000;
		
		double distance_per_s =  (shipState.getSpeed_current_kn() * 1.852 / 3.6 );
		double distance_per_time = distance_per_s  * milliOfDay/1000;
		geoCalc.setStartingAzimuth(shipState.getHeading_current_deg());
		geoCalc.setGeodesicDistance(distance_per_time);
		
		//System.out.println("distance_per_s: " + distance_per_s +" > distance per time: " + distance_per_time);

		shipState.setHeading_current_deg( (geoCalc.getEndingAzimuth() + 540 + 180) % 360 );
		DirectPosition destination = geoCalc.getEndPoint();
		shipState.getPoint().setLocation(new Point2D.Double(destination.getOrdinate(1),destination.getOrdinate(0)));
		
	}

	@Override
	public void setAgent(Agent agent) {
		this.agent  = agent;
	}

}
