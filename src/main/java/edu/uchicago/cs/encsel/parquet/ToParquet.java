package edu.uchicago.cs.encsel.parquet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.fs.Path;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.PrimitiveType;
import org.apache.parquet.schema.PrimitiveType.PrimitiveTypeName;
import org.apache.parquet.schema.Type.Repetition;

public class ToParquet {

	public static void singleColumnCSV(File input, File output, boolean useDictionary, int encoderType)
			throws IOException {
		if (output.exists())
			output.delete();
		BufferedReader reader = new BufferedReader(new FileReader(input));

		MessageType schema = new MessageType("record",
				new PrimitiveType(Repetition.REQUIRED, PrimitiveTypeName.INT32, "value"));

		HardcodedValuesWriterFactory.INSTANCE.setIntEncoderType(encoderType);
		ParquetWriter<List<String>> writer = CSVParquetWriterBuilder.buildDefault(new Path(output.toURI()), schema,
				useDictionary);

		String line = null;
		List<String> holder = new ArrayList<String>();
		while ((line = reader.readLine()) != null) {
			holder.add(line.trim());
			writer.write(holder);
			holder.clear();
		}

		reader.close();
		writer.close();
	}
}
