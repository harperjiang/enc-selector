package edu.uchicago.cs.encsel.parquet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;

import edu.uchicago.cs.encsel.model.IntEncoding;
import edu.uchicago.cs.encsel.model.StringEncoding;

public class ParquetWriterHelperTest {

	@Before
	public void deleteFile() throws IOException {
		Files.deleteIfExists(Paths.get("resource/test_col_str.data.DELTAL"));
		Files.deleteIfExists(Paths.get("resource/test_col_str.data.DICT"));
		Files.deleteIfExists(Paths.get("resource/test_col_str.data.PLAIN"));

		Files.deleteIfExists(Paths.get("resource/test_col_int.data.DICT"));
		Files.deleteIfExists(Paths.get("resource/test_col_int.data.BP"));
		Files.deleteIfExists(Paths.get("resource/test_col_int.data.DELTABP"));
		Files.deleteIfExists(Paths.get("resource/test_col_int.data.RLE"));
		Files.deleteIfExists(Paths.get("resource/test_col_int.data.PLAIN"));
	}

	@Test
	public void testWriteStr() throws IOException {

		String file = "resource/test_col_str.data";

		ParquetWriterHelper.singleColumnString(new File(file).toURI(), StringEncoding.DICT);
		ParquetWriterHelper.singleColumnString(new File(file).toURI(), StringEncoding.DELTAL);
		ParquetWriterHelper.singleColumnString(new File(file).toURI(), StringEncoding.PLAIN);
		
		assertTrue(Files.exists(Paths.get("resource/test_col_str.data.DELTAL")));
		assertTrue(Files.exists(Paths.get("resource/test_col_str.data.DICT")));
		assertTrue(Files.exists(Paths.get("resource/test_col_str.data.PLAIN")));
	}

	@Test
	public void testWriteInt() throws IOException {
		String file = "resource/test_col_int.data";

		HardcodedValuesWriterFactory.INSTANCE.setIntBitLength(14);

		ParquetWriterHelper.singleColumnInt(new File(file).toURI(), IntEncoding.DICT);
		ParquetWriterHelper.singleColumnInt(new File(file).toURI(), IntEncoding.BP);
		ParquetWriterHelper.singleColumnInt(new File(file).toURI(), IntEncoding.DELTABP);
		ParquetWriterHelper.singleColumnInt(new File(file).toURI(), IntEncoding.RLE);
		ParquetWriterHelper.singleColumnInt(new File(file).toURI(), IntEncoding.PLAIN);
		

		assertTrue(Files.exists(Paths.get("resource/test_col_int.data.DICT")));
		assertTrue(Files.exists(Paths.get("resource/test_col_int.data.BP")));
		assertTrue(Files.exists(Paths.get("resource/test_col_int.data.DELTABP")));
		assertTrue(Files.exists(Paths.get("resource/test_col_int.data.RLE")));
		assertTrue(Files.exists(Paths.get("resource/test_col_int.data.PLAIN")));
	}
	
	@Test
	public void testDetermineBitLength() {
		assertEquals(13,ParquetWriterHelper.scanIntBitLength(new File("resource/bitlength_test").toURI()));
	}
}
