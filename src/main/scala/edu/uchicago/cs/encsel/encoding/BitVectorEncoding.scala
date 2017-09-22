/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 * Contributors:
 *     Hao Jiang - initial API and implementation
 */

package edu.uchicago.cs.encsel.encoding

import java.io.{File, RandomAccessFile}
import java.net.URI
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel.MapMode

import com.google.gson.{Gson, JsonObject}
import edu.uchicago.cs.encsel.dataset.column.Column

import scala.collection.mutable
import scala.io.Source

class BitVectorEncoding extends Encoding {

  override def encode(input: Column, output: URI): Unit = {
    val source = Source.fromFile(input.colFile)

    try {
      val dict = new mutable.HashMap[String, Int]();
      var counter = 0L;
      // First pass, generate dict and count length
      source.getLines().foreach({
        counter += 1;
        dict.getOrElseUpdate(_, dict.size)
      })
      // Store dict as json object
      var jsondict = new JsonObject;
      dict.foreach(f => {
        jsondict.addProperty(f._1, f._2);
      })
      var dictstr = new Gson().toJson(jsondict)


      // Compute file size, allocate space
      val bitvecSize = Math.ceil(counter.toDouble / 8).toInt
      val bitmapSize = bitvecSize * dict.size
      val outputFile = new RandomAccessFile(new File(output), "rw")

      // 64 bit for dictionary offset
      // bitmap
      // dictionary
      val fileSize = 8 + bitmapSize + dictstr.length
      outputFile.setLength(fileSize)

      // Second pass, write bit vectors
      var pos = 0L;
      val size = 1024 * 1024;
      var buffer = outputFile.getChannel.map(MapMode.READ_WRITE, pos, size);
      buffer.load();


      var offset = 0l
      source.getLines().foreach(line => {
        val idx = dict.getOrElse(line, -1)

        val byteOffset = idx * bitvecSize + offset / 8
        val bitOffset = offset % 8

        if (byteOffset < pos || byteOffset >= pos + size) {
          // Write back
          buffer.force()
          // Load new buffer
          pos = (byteOffset / size) * size
          buffer = outputFile.getChannel.map(MapMode.READ_WRITE, pos, size)
          buffer.load()
        }

        val bufferOffset = byteOffset % size
        val byte = buffer.get(bufferOffset.toInt)
        buffer.put(bufferOffset.toInt, (byte | (1 << bitOffset)).toByte)

        offset += 1
      })

      outputFile.close();

    } finally {
      source.close();
    }
  }
}
