package edu.uchicago.cs.encsel.parquet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.fs.Path;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.PrimitiveType;
import org.apache.parquet.schema.PrimitiveType.PrimitiveTypeName;
import org.apache.parquet.schema.Type.Repetition;

import edu.uchicago.cs.encsel.model.FloatEncoding;
import edu.uchicago.cs.encsel.model.IntEncoding;
import edu.uchicago.cs.encsel.model.StringEncoding;
import edu.uchicago.cs.encsel.parquet.AdaptiveValuesWriterFactory.EncodingSetting;

public class ParquetWriterHelper {

	protected static File genOutput(URI input, String suffix) {
		try {
			if (input.getPath().endsWith("\\.data")) {
				return new File(new URI(input.toString().replaceFirst("data$", suffix)));
			}
			return new File(new URI(input.toString() + "." + suffix));
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Scan the file containing integer/long and determine the bit length
	 * 
	 * @param input
	 * @return
	 */
	public static int scanIntBitLength(URI input) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(input)));
			int maxBitLength = 0;
			String line = null;
			while ((line = br.readLine()) != null) {
				if (line.isEmpty())
					continue;
				Long number = Long.parseLong(line);
				int bitLength = 64 - Long.numberOfLeadingZeros(number);
				if (bitLength > maxBitLength)
					maxBitLength = bitLength;
			}
			br.close();
			return maxBitLength;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static URI singleColumnInt(URI input, IntEncoding encoding) throws IOException {
		File output = genOutput(input, encoding.name());
		if (output.exists())
			output.delete();
		BufferedReader reader = new BufferedReader(new FileReader(new File(input)));

		MessageType schema = new MessageType("record",
				new PrimitiveType(Repetition.OPTIONAL, PrimitiveTypeName.INT32, "value"));

		EncodingSetting es = AdaptiveValuesWriterFactory.encodingSetting.get();
		es.intEncoding = encoding;
		es.intBitLength = scanIntBitLength(input);
		ParquetWriter<List<String>> writer = ParquetWriterBuilder.buildDefault(new Path(output.toURI()), schema,
				encoding == IntEncoding.DICT);

		String line = null;
		List<String> holder = new ArrayList<String>();
		while ((line = reader.readLine()) != null) {
			holder.add(line.trim());
			writer.write(holder);
			holder.clear();
		}

		reader.close();
		writer.close();

		return output.toURI();
	}

	public static URI singleColumnLong(URI input, IntEncoding encoding) throws IOException {
		File output = genOutput(input, encoding.name());
		if (output.exists())
			output.delete();
		BufferedReader reader = new BufferedReader(new FileReader(new File(input)));

		MessageType schema = new MessageType("record",
				new PrimitiveType(Repetition.OPTIONAL, PrimitiveTypeName.INT64, "value"));

		EncodingSetting es = AdaptiveValuesWriterFactory.encodingSetting.get();
		es.intEncoding = encoding;
		es.intBitLength = scanIntBitLength(input);
		ParquetWriter<List<String>> writer = ParquetWriterBuilder.buildDefault(new Path(output.toURI()), schema,
				encoding == IntEncoding.DICT);

		String line = null;
		List<String> holder = new ArrayList<String>();
		while ((line = reader.readLine()) != null) {
			holder.add(line.trim());
			writer.write(holder);
			holder.clear();
		}

		reader.close();
		writer.close();

		return output.toURI();
	}

	public static URI singleColumnString(URI input, StringEncoding encoding) throws IOException {
		File output = genOutput(input, encoding.name());
		if (output.exists())
			output.delete();
		BufferedReader reader = new BufferedReader(new FileReader(new File(input)));

		MessageType schema = new MessageType("record",
				new PrimitiveType(Repetition.OPTIONAL, PrimitiveTypeName.BINARY, "value"));

		EncodingSetting es = AdaptiveValuesWriterFactory.encodingSetting.get();
		es.stringEncoding = encoding;
		ParquetWriter<List<String>> writer = ParquetWriterBuilder.buildDefault(new Path(output.toURI()), schema,
				encoding == StringEncoding.DICT);

		String line = null;
		List<String> holder = new ArrayList<String>();
		while ((line = reader.readLine()) != null) {
			holder.add(line.trim());
			writer.write(holder);
			holder.clear();
		}

		reader.close();
		writer.close();

		return output.toURI();
	}

	public static URI singleColumnDouble(URI input, FloatEncoding encoding) throws IOException {
		File output = genOutput(input, encoding.name());
		if (output.exists())
			output.delete();
		BufferedReader reader = new BufferedReader(new FileReader(new File(input)));

		MessageType schema = new MessageType("record",
				new PrimitiveType(Repetition.OPTIONAL, PrimitiveTypeName.DOUBLE, "value"));

		EncodingSetting es = AdaptiveValuesWriterFactory.encodingSetting.get();
		es.floatEncoding = encoding;
		ParquetWriter<List<String>> writer = ParquetWriterBuilder.buildDefault(new Path(output.toURI()), schema,
				encoding == FloatEncoding.DICT);

		String line = null;
		List<String> holder = new ArrayList<String>();
		while ((line = reader.readLine()) != null) {
			holder.add(line.trim());
			writer.write(holder);
			holder.clear();
		}

		reader.close();
		writer.close();

		return output.toURI();
	}

	public static URI singleColumnFloat(URI input, FloatEncoding encoding) throws IOException {
		File output = genOutput(input, encoding.name());
		if (output.exists())
			output.delete();
		BufferedReader reader = new BufferedReader(new FileReader(new File(input)));

		MessageType schema = new MessageType("record",
				new PrimitiveType(Repetition.OPTIONAL, PrimitiveTypeName.FLOAT, "value"));

		EncodingSetting es = AdaptiveValuesWriterFactory.encodingSetting.get();
		es.floatEncoding = encoding;
		ParquetWriter<List<String>> writer = ParquetWriterBuilder.buildDefault(new Path(output.toURI()), schema,
				encoding == FloatEncoding.DICT);

		String line = null;
		List<String> holder = new ArrayList<String>();
		while ((line = reader.readLine()) != null) {
			holder.add(line.trim());
			writer.write(holder);
			holder.clear();
		}

		reader.close();
		writer.close();

		return output.toURI();
	}
}
