package FileManager;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/*
This class handles writing to a file, without loading the file itself.
It uses a buffered writer to handle this.

This class is set to AutoClosable and must therefor be arguments to a try/catch statement.
If something fails, the file will be closed safely.
 */
public class FileWriter implements AutoCloseable {

    public enum OverwriteMode {
        OVERWRITE,
        NO_OVERWRITE
    }

    private BufferedWriter writer;
    private boolean isOpen = false;
    private String fileName;

    public void openFile(String fileName, OverwriteMode overwriteMode) throws IOException {
        if (isOpen) {
            throw new IllegalStateException("File '" + fileName + "' is already open!");
        }

        Path path = Paths.get(fileName);

        if (Files.exists(path)) {
            if (overwriteMode == OverwriteMode.NO_OVERWRITE) {
                throw new FileAlreadyExistsException(fileName, null,
                        "File '" + fileName + "' already exists. Use OverwriteMode.OVERWRITE to replace it.");
            }
            //truncate the existing file (clear it)
            this.writer = Files.newBufferedWriter(path, StandardOpenOption.TRUNCATE_EXISTING);
        } else {
            createParentDirectories(path);
            // File does not exist — create it
            this.writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE_NEW);
        }

        this.fileName = fileName;
        this.isOpen = true;
        System.out.println("File opened: " + fileName);
    }

    public void writeToFile(String content) throws IOException {
        if (!isOpen) {
            throw new IllegalStateException("Failed to write to file. Call openFile() before writing.");
        }

        writer.write(content);
        writer.newLine();
    }

    public void closeFile() throws IOException {
        if (!isOpen) {
            throw new IllegalStateException("Failed to close file: No file is currently open.");
        }

        writer.close();
        isOpen = false;
        System.out.println("File closed: " + fileName);
        this.fileName = null;
    }

    //AutoCloseable
    @Override
    public void close() throws IOException {
        if (isOpen) {
            closeFile();
        }
    }

    public boolean isOpen() {
        return isOpen;
    }

    private void createParentDirectories(Path path) throws IOException{
        //create parent directories
        Path parentDir = path.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
            System.out.println("Created directories: " + parentDir);
        }
    }
}
