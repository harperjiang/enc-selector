package edu.uchicago.cs.encsel.parquet;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.column.ParquetProperties;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.ParquetWriter.Builder;
import org.apache.parquet.hadoop.api.WriteSupport;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.apache.parquet.schema.MessageType;

public class CSVParquetWriterBuilder extends Builder<List<String>, CSVParquetWriterBuilder> {

	private WriteSupport<List<String>> writeSupport = null;

	private Field field = null;

	public CSVParquetWriterBuilder(Path file, MessageType schema) {
		super(file);
		writeSupport = new CSVWriteSupport(schema);
		try {
			field = Builder.class.getDeclaredField("encodingPropsBuilder");
			field.setAccessible(true);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		getEncodingPropertiesBuilder().withValuesWriterFactory(HardcodedValuesWriterFactory.INSTANCE);
	}

	@Override
	protected CSVParquetWriterBuilder self() {
		return this;
	}

	@Override
	protected WriteSupport<List<String>> getWriteSupport(Configuration conf) {
		return writeSupport;
	}

	protected ParquetProperties.Builder getEncodingPropertiesBuilder() {
		try {
			return (ParquetProperties.Builder) field.get(this);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static ParquetWriter<List<String>> buildDefault(Path file, MessageType schema, boolean useDictionary)
			throws IOException {
		CSVParquetWriterBuilder builder = new CSVParquetWriterBuilder(file, schema);

		return builder.withValidation(false).withCompressionCodec(CompressionCodecName.UNCOMPRESSED)
				.withDictionaryEncoding(useDictionary).withRowGroupSize(ParquetWriter.DEFAULT_BLOCK_SIZE)
				.withDictionaryPageSize(ParquetWriter.DEFAULT_PAGE_SIZE).build();
	}
}
