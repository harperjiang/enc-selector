package edu.uchicago.cs.encsel.app;

import java.io.File;

import edu.uchicago.cs.encsel.datacol.DataCollector;

public class CollectData {

	public static void main(String[] args) throws Exception {
		File f = new File(args[0]);
		new DataCollector().collect(f.toURI());

	}

}
