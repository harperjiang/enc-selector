package edu.uchicago.cs.encsel.dataset.parquet.converter;

import org.apache.parquet.io.api.Converter;
import org.apache.parquet.io.api.GroupConverter;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RowConverter extends GroupConverter {
    protected Row current;
    private Converter[] converters;

    private Map<String[], Integer> index = new HashMap<>();
    private List<Row> records = new ArrayList<>();

    public RowConverter(MessageType schema) {

        converters = new Converter[schema.getFieldCount()];

        for (int i = 0; i < converters.length; i++) {
            final Type type = schema.getType(i);
            if (type.isPrimitive()) {
                converters[i] = new RowFieldPrimitiveConverter(this, i, schema.getType(i).asPrimitiveType());
                index.put(schema.getColumns().get(i).getPath(), i);
            }
        }
    }

    @Override
    public void start() {
        current = new Row(converters.length);
        records.add(current);
    }

    @Override
    public Converter getConverter(int fieldIndex) {
        return converters[fieldIndex];
    }

    public Converter getConverter(String[] path) {
        return converters[index.get(path)];
    }

    @Override
    public void end() {
    }

    public Row getCurrentRecord() {
        return current;
    }

    public void setCurrentRecord(int index) {
        current = records.get(index);
    }

    public List<Row> getRecords() {
        return records;
    }
}
