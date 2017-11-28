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

package edu.uchicago.cs.encsel.dataset.parquet.converter;

import org.apache.parquet.io.api.Binary;
import org.apache.parquet.io.api.Converter;
import org.apache.parquet.io.api.GroupConverter;
import org.apache.parquet.schema.MessageType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ColumnTempTable extends GroupConverter {

    private MessageType[] schemas;

    private ColumnPrimitiveConverter[][] converters;

    private Column[][] columns;

    private List<int[]> indexMap = new ArrayList<>();

    private Map<String[], Integer>[] pathMaps;

    public ColumnTempTable(MessageType... schema) {
        this.schemas = schema;

        converters = new ColumnPrimitiveConverter[schemas.length][];
        columns = new Column[converters.length][];
        pathMaps = new Map[converters.length];
        for (int i = 0; i < converters.length; i++) {
            converters[i] = new ColumnPrimitiveConverter[schemas[i].getColumns().size()];
            columns[i] = new Column[converters[i].length];
            pathMaps[i] = new HashMap<>();
            for (int j = 0; j < converters[i].length; j++) {
                int[] index = new int[]{i, j};
                converters[i][j] = new ColumnPrimitiveConverter(this, index,
                        schemas[i].getType(j).asPrimitiveType());
                columns[i][j] = new Column();
                indexMap.add(index);
                pathMaps[i].put(schema[i].getColumns().get(j).getPath(), j);
            }
        }
    }

    public Converter getConverter(int[] fieldIndex) {
        return converters[fieldIndex[0]][fieldIndex[1]];
    }

    public Converter getConverter(int schemaIndex, String[] path) {
        int colIndex = pathMaps[schemaIndex].get(path);
        return getConverter(new int[]{schemaIndex, colIndex});
    }

    @Override
    public Converter getConverter(int globalIndex) {
        return getConverter(indexMap.get(globalIndex));
    }

    @Override
    public void start() {

    }

    @Override
    public void end() {

    }

    public void add(int[] index, Binary value) {
        columns[index[0]][index[1]].add(value);
    }

    public void add(int[] index, boolean value) {
        columns[index[0]][index[1]].add(value);
    }

    public void add(int[] index, double value) {
        columns[index[0]][index[1]].add(value);
    }

    public void add(int[] index, float value) {
        columns[index[0]][index[1]].add(value);
    }

    public void add(int[] index, int value) {
        columns[index[0]][index[1]].add(value);
    }

    public void add(int[] index, long value) {
        columns[index[0]][index[1]].add(value);
    }
}
