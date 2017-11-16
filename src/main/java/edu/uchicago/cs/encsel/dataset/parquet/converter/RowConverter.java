package edu.uchicago.cs.encsel.dataset.parquet.converter;

import org.apache.parquet.io.api.Converter;
import org.apache.parquet.io.api.GroupConverter;
import org.apache.parquet.schema.GroupType;
import org.apache.parquet.schema.Type;

import java.util.ArrayList;
import java.util.List;

public class RowConverter extends GroupConverter {
    protected Row current;
    private Converter[] converters;

    private List<Row> records = new ArrayList<>();

    public RowConverter(GroupType schema) {

        converters = new Converter[schema.getFieldCount()];

        for (int i = 0; i < converters.length; i++) {
            final Type type = schema.getType(i);
            if (type.isPrimitive()) {
                converters[i] = new RowFieldPrimitiveConverter(this, i, schema.getType(i).asPrimitiveType());
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

    @Override
    public void end() {
    }

    public Row getCurrentRecord() {
        return current;
    }
}
