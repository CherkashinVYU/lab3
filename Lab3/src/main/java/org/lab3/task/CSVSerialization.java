package org.lab3.task;

import java.io.*;
import java.util.ArrayList;
import java.util.Objects;

class CSVSerialization {
    private static final String CSV_SEPARATOR = ";";
    private final String filename;

    CSVSerialization(String filename) {
        this.filename = filename;
    }

    public void write(ArrayList<Series> series) throws IOException {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "UTF-8"));
        for (Series serie : series) {
            StringBuilder oneLine = new StringBuilder();
            if (serie instanceof Linear obj) {
                oneLine.append(obj.getA0());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(obj.getD());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(obj.name);
                oneLine.append(CSV_SEPARATOR);
                oneLine.append("linear");
            } else if (serie instanceof Exponential obj) {
                oneLine.append(obj.getA0());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(obj.getD());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(obj.name);
                oneLine.append(CSV_SEPARATOR);
                oneLine.append("expo");
            }
            bw.write(oneLine.toString());
            bw.newLine();
        }
        bw.flush();
        bw.close();
    }

    public ArrayList<Series> read() throws IOException, NumberFormatException {
        ArrayList<Series> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(CSV_SEPARATOR);
                try {
                    if (Objects.equals(values[3], "linear")) {
                        try{
                            Linear obj = new Linear(Integer.parseInt(values[0]),
                                    Integer.parseInt(values[1]),
                                    values[2]);
                            records.add(obj);
                        }catch(ArrayIndexOutOfBoundsException | NumberFormatException e){
                        }
                    } else if (Objects.equals(values[3], "expo")) {
                        try{
                            Exponential obj = new Exponential(Integer.parseInt(values[0]),
                                    Integer.parseInt(values[1]),
                                    values[2]);
                            records.add(obj);
                        }catch(ArrayIndexOutOfBoundsException | NumberFormatException e){
                        }
                    }else{
                    }
                }catch (ArrayIndexOutOfBoundsException ex){
                }
            }
        }
        return records;
    }
}
