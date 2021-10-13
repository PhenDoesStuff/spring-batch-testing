package com.montague.springbatchtesting.reader;

import org.springframework.batch.item.ItemReader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SimpleItemReader implements ItemReader<String> {

    private final List<String> dataSet = new ArrayList<>();

    private final Iterator<String> iterator;

    public SimpleItemReader() {
        dataSet.add("1");
        dataSet.add("2");
        dataSet.add("3");
        dataSet.add("4");
        dataSet.add("5");
        iterator = dataSet.iterator();
    }

    @Override
    public String read() throws Exception {
        return iterator.hasNext() ? iterator.next() : null;
    }
}
