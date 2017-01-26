package edu.uchicago.cs.encsel.parquet;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

public class Main {

	public static void main(String[] args) throws IOException {

		String file = "/home/harper/enc_workspace/crime_data/crime_11";
		String input = MessageFormat.format("{0}.data", file);

		HardcodedValuesWriterFactory.INSTANCE.setIntBitLength(14);
		
		ToParquet.singleColumnCSV(new File(input), new File(file + ".pid"), true, 0);
		ToParquet.singleColumnCSV(new File(input), new File(file + ".rle"), false, 0);
		ToParquet.singleColumnCSV(new File(input), new File(file + ".bp"), false, 1);
		ToParquet.singleColumnCSV(new File(input), new File(file + ".dbpi"), false, 2);
		ToParquet.singleColumnCSV(new File(input), new File(file + ".pln"), false, 3);
	}
}
