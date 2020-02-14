package dev.zackschw.boosttorrent.bencode;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.List;
import java.util.TreeMap;


public class BEncoder {
    @SuppressWarnings("unchecked")
    /**
     * Writes o into output based on the data type of o
     * @param o The unit of information to be encoded (eg. number, string, list, dict)
     * @param output the stream to write the encoded data
     * @throws RuntimeException if the unit was not a valid data type
     * @throws IOException if exception occurs in moving bytes to the stream
     */
    public static void write(Object o, OutputStream output) throws IOException {
        if (o instanceof BValue) {
            o = ((BValue) o).getValue();
        }
        if (o instanceof Number) {
            //write "i<int>e"
            writeNum((Number) o, output);
        }
        else if (o instanceof String) {
            //write "<len>:<text>"
            writeString(((String) o).getBytes(StandardCharsets.UTF_8), output);
        }
        else if (o instanceof byte[]) {
            //write "<len>:<text>"
            writeString((byte[]) o, output);
        }
        else if (o instanceof List) {
            //write "l<content>e"
            output.write('l');
            for (Object listElem : (List<Object>) o) {
                write(listElem, output);
            }
            output.write('e');
        }
        else if (o instanceof Map) {
            //write "d<content>e"
            Map<String, Object> sortedDict = new TreeMap<>((Map<String, Object>) o);
            output.write('d');
            for (Map.Entry<String, Object> mapElem : sortedDict.entrySet()) {
                writeString(mapElem.getKey().getBytes(StandardCharsets.UTF_8), output);
                write(mapElem.getValue(), output);
            }
            output.write('e');
        }
        else {
            //if it's a bad type, halt the execution
            throw new RuntimeException("BEncode has received bad input.");
        }
    }

    /**
     * Writes encoded number to the stream as a long value (to handle oversize int inputs)
     * @param i the number to be written to the stream
     * @param output the stream to write the encoded data
     * @throws IOException if error occurs with writing to stream
     */
    public static void writeNum(Number i, OutputStream output) throws IOException {
        //write "i<int>e"
        output.write('i');
        output.write(Long.toString(i.longValue()).getBytes(StandardCharsets.UTF_8));
        output.write('e');
    }

    /**
     * Writes encoded string to the stream as UTF8 bytes
     * @param b the array of bytes to write to the stream
     * @param output the stream to write the encoded data
     * @throws IOException if error occurs with writing to stream
     */
    public static void writeString(byte[] b, OutputStream output) throws IOException {
        //write "<len>:<string>"
        output.write((Integer.toString(b.length)).getBytes(StandardCharsets.UTF_8));
        output.write(':');
        output.write(b);
    }
}