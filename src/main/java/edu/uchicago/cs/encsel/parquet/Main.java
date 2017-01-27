package edu.uchicago.cs.encsel.parquet;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

import edu.uchicago.cs.encsel.model.StringEncoding;

public class Main {

	public static void main(String[] args) throws IOException {

		for (int i = 1; i <= 4; i++) {
			String file = MessageFormat.format("/home/harper/enc_workspace/crime_data/crime_{0}.data", i);

			HardcodedValuesWriterFactory.INSTANCE.setIntBitLength(14);

			ParquetWriterHelper.singleColumnString(new File(file), true, StringEncoding.DICT);
			ParquetWriterHelper.singleColumnString(new File(file), false, StringEncoding.DELTAL);
			ParquetWriterHelper.singleColumnString(new File(file), false, StringEncoding.PLAIN);
		}
	}
}
