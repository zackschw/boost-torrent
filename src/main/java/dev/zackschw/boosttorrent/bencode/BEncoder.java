package dev.zackschw.boosttorrent.bencode;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.List;


public class BEncoder {
    @SuppressWarnings("unchecked")
    public static void write(Object o, OutputStream output) throws IOException {
        if (o instanceof Number) {
            //return "i<int>e"
            writeNum((Number) o, output);
        }
        else if (o instanceof String) {
            //return "<len>:<text>"
            writeString(((String) o).getBytes(StandardCharsets.UTF_8), output);
        }
        else if (o instanceof byte[]) {
            //return "<len>:<text>"
            writeString((byte[]) o, output);
        }
        else if (o instanceof List) {
            //return "l<content>e"
            output.write('l');
            for (Object listElem : (List<Object>) o) {
                write(listElem, output);
            }
            output.write('e');
        }
        else if (o instanceof Map) {
            //if object is a dictionary
            //return "d<content>e"
            //iterate over elements of the map and call write for them
            output.write('d');
            for (Map.Entry<String, Object> mapElem : ((Map<String, Object>)o).entrySet()) {
                writeString(mapElem.getKey().getBytes(StandardCharsets.UTF_8), output);
                write(mapElem.getValue(), output);
            }
            output.write('e');
        }
        else {
            //handle error somehow
            throw new RuntimeException("BEncode has received bad input.");
        }
    }

    public static void writeNum(Number i, OutputStream output) throws IOException {
        //write i<int>e
        output.write('i');
        output.write(Long.toString(i.longValue()).getBytes(StandardCharsets.UTF_8));
        output.write('e');
    }

    public static void writeString(byte[] b, OutputStream output) throws IOException {
        //write len:string
        output.write((Integer.toString(b.length)).getBytes(StandardCharsets.UTF_8));
        output.write(':');
        output.write(b);
    }
}