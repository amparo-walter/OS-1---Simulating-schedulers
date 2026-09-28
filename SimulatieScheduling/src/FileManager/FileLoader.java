package FileManager;

import javax.xml.stream.*;
import java.io.*;

import Components.Process;

/*
This class handles loading process-files in XML format and parses the data automatically to "process" objects.
The file is never fully loaded into memory, only buffered.
Therefor, many simulations don't hog up memory.
If the file is housed on a hard-disk, this may impact performance.

This class is set to AutoClosable and must therefor be arguments to a try/catch statement.
If something fails, the file will be closed safely.
 */
public class FileLoader implements AutoCloseable {

    private XMLStreamReader reader;
    private InputStream inputStream;
    private String fileName;

    public void loadFile(String filePath) throws Exception {
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            inputStream = new BufferedInputStream(new FileInputStream(filePath));
            reader = factory.createXMLStreamReader(inputStream);
            fileName = filePath;
            System.out.println("File loaded: " + filePath);
        } catch (Exception e) {
            unloadFile();
            throw new Exception("Failed to load XML file '" + filePath + "'.", e);
        }
    }

    public Process getNextProcess() throws Exception {
        if (reader == null) {
            throw new IllegalStateException("File not loaded. Call loadFile() first.");
        }

        try {
            while (reader.hasNext()) {
                int event = reader.next();

                if (event == XMLStreamConstants.START_ELEMENT &&
                        reader.getLocalName().equals("process")) {

                    long pid = -1;
                    long arrivalTime = -1;
                    long serviceTime = -1;

                    while (reader.hasNext()) {
                        event = reader.next();

                        if (event == XMLStreamConstants.START_ELEMENT) {
                            String tag = reader.getLocalName();

                            switch (tag) {
                                case "pid" -> pid = Long.parseLong(reader.getElementText());
                                case "arrivaltime" -> arrivalTime = Long.parseLong(reader.getElementText());
                                case "servicetime" -> serviceTime = Long.parseLong(reader.getElementText());
                            }
                        }

                        if (event == XMLStreamConstants.END_ELEMENT &&
                                reader.getLocalName().equals("process")) {
                            break;
                        }
                    }

                    if (pid != -1 && arrivalTime != -1 && serviceTime != -1) {
                        return new Process(pid, arrivalTime, serviceTime);
                    }
                }
            }
            return null; // no more processes
        } catch (Exception e) {
            throw new Exception("Error while reading next process", e);
        }
    }

    public void unloadFile() {
        try {
            if (reader != null) {
                reader.close();
                reader = null;
            }
            if (inputStream != null) {
                inputStream.close();
                inputStream = null;
            }
            System.out.println("File unloaded: " + fileName);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void close() {
        unloadFile();
    }
}