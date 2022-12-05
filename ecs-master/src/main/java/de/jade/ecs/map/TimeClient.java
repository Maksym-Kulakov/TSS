package de.jade.ecs.map;

import de.jade.ecs.map.xychart.DynamicSimulation;
import dk.dma.ais.binary.SixbitException;
import dk.dma.ais.message.AisMessage;
import dk.dma.ais.message.AisMessage1;
import dk.dma.ais.message.AisMessage5;
import dk.dma.ais.message.AisMessageException;
import dk.dma.ais.sentence.SentenceException;
import dk.dma.ais.sentence.Vdm;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * This program is a socket client application that connects to a time server to
 * get the current date time.
 *
 * @author www.codejava.net
 */
public class TimeClient implements Runnable {
	static int i;
	double shiplat;
	double shiplong;
	int mmsi;
	double hdg;
	double speed;
	ChartViewer chartViewer;
	public static int inner = 0;

	public TimeClient(ChartViewer chartViewer) {
		this.chartViewer = chartViewer;
	}

	@Override
	public void run() {
		String hostname = "139.13.30.142";
		int port = 22240;

		try (Socket socket = new Socket(hostname, port)) {

			InputStream input = socket.getInputStream();
			InputStreamReader reader = new InputStreamReader(input);

			int character;
			StringBuilder data = new StringBuilder();

//
//            char[] buf = new char[1];
//            reader.read(buf);
			while (true) {
				while ((character = reader.read()) != '*') {
					data.append((char) character);
//                System.out.println(data);
				}
				data.append((char) character);
				character = reader.read();
				data.append((char) character);
				character = reader.read();
				data.append((char) character);

//				System.out.println(data);
				Vdm vdm = new Vdm();
				try {
					vdm.parse(data.toString());
					AisMessage msg = AisMessage.getInstance(vdm);

					String dest = null;
					if (msg instanceof AisMessage5) {
						AisMessage5 AISMSG5 = (AisMessage5) msg;
						dest = AISMSG5.getDest();
					}

					if(msg instanceof AisMessage1) {
//						System.out.println(msg.toString());
						AisMessage1 AISMSG1 = (AisMessage1) msg;

//						System.out.println("Position: " + AISMSG1.getPos().getLatitudeDouble() + ", "+ AISMSG1.getPos().getLongitudeDouble());

						shiplat = AISMSG1.getPos().getLatitudeDouble();
						shiplong = AISMSG1.getPos().getLongitudeDouble();
						if (shiplat > 53.5 && shiplat < 55.0 && shiplong > 6.0 && shiplong < 8.5) {
						mmsi = AISMSG1.getUserId();
						hdg = AISMSG1.getCog() / 10.0;
						speed = AISMSG1.getSog() / 10.0;
					/*	String dest = AISMSG5.getDest();*/
// 						System.out.println(speed);
						ShipAis shipAis = new ShipAis(mmsi, shiplat, shiplong, hdg, speed, dest);
						}
//						shipAis.addShipsOnMap(chartViewer, ShipAis.shipsAisHashMap);

					}



					//for test only
				 		if (!DynamicSimulation.shipStatements.isEmpty()
								&& DynamicSimulation.count > inner) {
							ShipAis shipAis1
									= new ShipAis(11111111,
									DynamicSimulation.shipStatements.get(11111111).getPoint().getY(),
									DynamicSimulation.shipStatements.get(11111111).getPoint().getX(),
									DynamicSimulation.shipStatements.get(11111111).getHeading_commanded_deg(),
									DynamicSimulation.shipStatements.get(11111111).getSpeed_commanded_kn(),
									"WILHELMSHAVEN");

							ShipAis shipAis2
									= new ShipAis(22222222,
									DynamicSimulation.shipStatements.get(22222222).getPoint().getY(),
									DynamicSimulation.shipStatements.get(22222222).getPoint().getX(),
									DynamicSimulation.shipStatements.get(22222222).getHeading_commanded_deg(),
									DynamicSimulation.shipStatements.get(22222222).getSpeed_commanded_kn(),
									"HAMBURG");

							ShipAis shipAis3
									= new ShipAis(33333333,
									DynamicSimulation.shipStatements.get(33333333).getPoint().getY(),
									DynamicSimulation.shipStatements.get(33333333).getPoint().getX(),
									DynamicSimulation.shipStatements.get(33333333).getHeading_commanded_deg(),
									DynamicSimulation.shipStatements.get(33333333).getSpeed_commanded_kn(),
									"HAMBURG");
							inner++;
							ShipAis shipAis4
									= new ShipAis(44444444,
									DynamicSimulation.shipStatements.get(44444444).getPoint().getY(),
									DynamicSimulation.shipStatements.get(44444444).getPoint().getX(),
									DynamicSimulation.shipStatements.get(44444444).getHeading_commanded_deg(),
									DynamicSimulation.shipStatements.get(44444444).getSpeed_commanded_kn(),
									"OTHER");
							inner++;
						}



/*					if (i == 15) {
						ShipAis shipAis4
								= new ShipAis(44444444, 53.96585, 7.615865, 146, 10, "HAMBURG");
					}
					if (i == 20) {
						ShipAis shipAis3
								= new ShipAis(33333333, 53.91741, 7.545902, 77, 14, "WILHELMSHAVEN");
					}
					if (i == 25) {
						ShipAis shipAis7
								= new ShipAis(77777777, 53.9049, 7.756226, 328, 7, "OTHER");
					}
					if (i == 25) {
						ShipAis shipAis5
								= new ShipAis(55555555, 53.92841, 7.545902, 77, 18, "HAMBURG");
					}
					i++;*/

				} catch (SentenceException | AisMessageException | SixbitException e) {
					e.printStackTrace();
				}

				data = new StringBuilder();
			}

		} catch (UnknownHostException ex) {

			System.out.println("Server not found: " + ex.getMessage());

		} catch (IOException ex) {

			System.out.println("I/O error: " + ex.getMessage());
		}
	}
}
