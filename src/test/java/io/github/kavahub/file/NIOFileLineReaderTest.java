package io.github.kavahub.file;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.kavahub.file.reader.NIOFileLineReader;

public class NIOFileLineReaderTest {

    @Test
    public void givenFile_whenRead() throws IOException {
        final Path FILE = Paths.get("src", "test", "resources", "fileToRead.txt");

        List<String> actual = new ArrayList<>();
        NIOFileLineReader.read(FILE).subscribe(data -> {
            actual.add(data);
        }, err -> err.printStackTrace()).join();

        assertThat(actual).containsExactly(Files.readAllLines(FILE).toArray(new String[] {}));
    }

}
