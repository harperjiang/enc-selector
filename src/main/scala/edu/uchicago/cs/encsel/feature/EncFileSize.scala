package edu.uchicago.cs.encsel.feature

import java.io.File

import scala.Iterable

import edu.uchicago.cs.encsel.model.Column
import edu.uchicago.cs.encsel.model.DataType
import edu.uchicago.cs.encsel.model.FloatEncoding
import edu.uchicago.cs.encsel.model.IntEncoding
import edu.uchicago.cs.encsel.model.StringEncoding
import edu.uchicago.cs.encsel.parquet.ParquetWriterHelper

class EncFileSize(f: File, enc: String) extends Feature {
  var size = f.length
  var encName = "%s_file_size".format(enc)

  def name = encName
  def value = size
}

object EncFileSize extends FeatureExtractor {

  def extract(col: Column): Iterable[Feature] = {
    col.dataType match {
      case DataType.STRING => {
        StringEncoding.values().map { e =>
          {
            var f = ParquetWriterHelper.singleColumnString(col.colFile, e)
            new EncFileSize(new File(f), e.name())
          }
        }
      }
      case DataType.LONG => {
        IntEncoding.values().map { e =>
          {
            var f = ParquetWriterHelper.singleColumnLong(col.colFile, e)
            new EncFileSize(new File(f), e.name())
          }
        }
      }
      case DataType.INTEGER => {
        IntEncoding.values().map { e =>
          {
            var f = ParquetWriterHelper.singleColumnInt(col.colFile, e)
            new EncFileSize(new File(f), e.name())
          }
        }
      }
      case DataType.FLOAT => {
        FloatEncoding.values().map { e =>
          {
            var f = ParquetWriterHelper.singleColumnFloat(col.colFile, e)
            new EncFileSize(new File(f), e.name())
          }
        }
      }
      case DataType.DOUBLE => {
        FloatEncoding.values().map { e =>
          {
            var f = ParquetWriterHelper.singleColumnDouble(col.colFile, e)
            new EncFileSize(new File(f), e.name())
          }
        }
      }
      case DataType.BOOLEAN => Iterable[Feature]() // Ignore BOOLEAN type
    }
  }

}