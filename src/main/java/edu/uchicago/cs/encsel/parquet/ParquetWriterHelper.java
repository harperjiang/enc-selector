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

	public static URI singleColumnInt(URI input, IntEncoding encoding) throws IOException {
		File output = genOutput(input, encoding.name());
		if (output.exists())
			output.delete();
		BufferedReader reader = new BufferedReader(new FileReader(new File(input)));

		MessageType schema = new MessageType("record",
				new PrimitiveType(Repetition.REQUIRED, PrimitiveTypeName.INT32, "value"));

		HardcodedValuesWriterFactory.INSTANCE.setIntEncoding(encoding);
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
				new PrimitiveType(Repetition.REQUIRED, PrimitiveTypeName.INT64, "value"));

		HardcodedValuesWriterFactory.INSTANCE.setIntEncoding(encoding);
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
				new PrimitiveType(Repetition.REQUIRED, PrimitiveTypeName.BINARY, "value"));

		HardcodedValuesWriterFactory.INSTANCE.setStringEncoding(encoding);
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
				new PrimitiveType(Repetition.REQUIRED, PrimitiveTypeName.DOUBLE, "value"));

		HardcodedValuesWriterFactory.INSTANCE.setFloatEncoding(encoding);
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
				new PrimitiveType(Repetition.REQUIRED, PrimitiveTypeName.FLOAT, "value"));

		HardcodedValuesWriterFactory.INSTANCE.setFloatEncoding(encoding);
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
