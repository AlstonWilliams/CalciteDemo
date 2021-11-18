package com.demo;

import org.apache.calcite.linq4j.Enumerator;

import java.io.*;
import java.util.List;

public class DemoEnumerator <E> implements Enumerator<E> {

    private E current;
    private BufferedReader br;

    public DemoEnumerator(String dataFile) {
        try {
            this.br = new BufferedReader(new FileReader(dataFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public E current() {
        return current;
    }

    @Override
    public boolean moveNext() {
        try {
            String line = br.readLine();

            if (line == null) {
                return false;
            }

            current = (E) line.split(",");

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public void reset() {

    }

    @Override
    public void close() {

    }
}
