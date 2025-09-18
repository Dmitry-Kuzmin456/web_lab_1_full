package server.loaders;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class FileLoader implements Loader {
    private final String base_path;

    public FileLoader(String type) {
        this.base_path = "/data/" + type + "/";
    }

    @Override
    public String load(String fileName) {
        String path = base_path + fileName;
        try{
            InputStream is = getClass().getResourceAsStream(path);
            if (is == null) return null;

            Scanner scanner = new Scanner(is, StandardCharsets.UTF_8);
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : null;
        }
        catch (Exception e) {
            return null;
        }
    }

}
