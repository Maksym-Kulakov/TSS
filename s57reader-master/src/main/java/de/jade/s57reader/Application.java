package de.jade.s57reader;

import java.io.FileInputStream;
import java.io.IOException;

import s57.S57dec;
import s57.S57map;

public class Application {
	public static void main(String[] args) {
		FileInputStream in;
		try {
			in = new FileInputStream("D:\\U37IL137\\U37IL137.000");
			S57map map = new S57map(true);
		 	S57dec.decodeChart(in, map);

			System.out.println(map);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
