package de.jade.ecs.map;

import java.net.*;
import java.nio.CharBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dk.dma.ais.binary.SixbitException;
import dk.dma.ais.message.AisMessage;
import dk.dma.ais.message.AisMessage1;
import dk.dma.ais.message.AisMessageException;
import dk.dma.ais.sentence.SentenceException;
import dk.dma.ais.sentence.Vdm;

import java.io.*;

/**
 * This program is a socket client application that connects to a time server to
 * get the current date time.
 *
 * @author www.codejava.net
 */
public class TimeClient implements Runnable {

	double shiplat;
	double shiplong;
	int mmsi;
	ChartViewer chartViewer;

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
					
					if(msg instanceof AisMessage1) {
//						System.out.println(msg.toString());
						AisMessage1 AISMSG1 = (AisMessage1) msg;
						
						System.out.println("Position: " + AISMSG1.getPos().getLatitudeDouble() + ", "+ AISMSG1.getPos().getLongitudeDouble());
						
						shiplat = AISMSG1.getPos().getLatitudeDouble();
						shiplong = AISMSG1.getPos().getLongitudeDouble();
						mmsi = AISMSG1.getUserId();

						ShipAis shipAis = new ShipAis(mmsi, shiplat, shiplong);
//						shipAis.addShipsOnMap(chartViewer, ShipAis.shipsAisHashMap);

					}
				} catch (SentenceException e) {
					e.printStackTrace();
				} catch (AisMessageException e) {
					e.printStackTrace();
				} catch (SixbitException e) {
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
