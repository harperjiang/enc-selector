package edu.uchicago.cs.encsel.dataset.parquet;

import org.apache.parquet.column.Dictionary;
import org.apache.parquet.io.api.Binary;
import org.apache.parquet.io.api.PrimitiveConverter;
import org.apache.parquet.schema.PrimitiveType;

public class RowFieldPrimitiveConverter extends PrimitiveConverter {

    private int index;

    private Dictionary dictionary;

    public RowFieldPrimitiveConverter(int index) {
        this.index = index;
    }

    @Override
    public boolean hasDictionarySupport() {
        return true;
    }

    @Override
    public void setDictionary(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    @Override
    public void addValueFromDictionary(int dictionaryId) {
    }

    @Override
    public void addBinary(Binary value) {
    }

    @Override
    public void addBoolean(boolean value) {
    }

    @Override
    public void addDouble(double value) {
    }

    @Override
    public void addFloat(float value) {
    }

    @Override
    public void addInt(int value) {
    }

    @Override
    public void addLong(long value) {
    }
}
