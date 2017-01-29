package edu.uchicago.cs.encsel.parquet;

import java.io.File;
import java.io.IOException;

import edu.uchicago.cs.encsel.model.StringEncoding;

public class ParquetWriterHelperTest {

	public void testWrite() throws IOException {

		String file = "resource/test.data";

		HardcodedValuesWriterFactory.INSTANCE.setIntBitLength(14);

		ParquetWriterHelper.singleColumnString(new File(file), StringEncoding.DICT);
		ParquetWriterHelper.singleColumnString(new File(file), StringEncoding.DELTAL);
		ParquetWriterHelper.singleColumnString(new File(file), StringEncoding.PLAIN);
	}
}
