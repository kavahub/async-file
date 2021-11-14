package io.github.kavahub.file;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import io.github.kavahub.file.reader.NIOFileLineReader;

public class NIOFileLineReaderExample {
    
    
    @Test
    public void givenFile_whenRead_thenThreadInfo() throws IOException {
        final Path FILE = Paths.get("src", "test", "resources", "fileWithmanyOfLine.txt");
        NIOFileLineReader.read(FILE).subscribe((data, err) -> {
            System.out.println(Thread.currentThread().getName());
        }).join();
    }

    @Test
    public void givenFile_whenRead_thenPrint() throws IOException {
        final Path FILE = Paths.get("src", "test", "resources", "fileWithmanyOfLine.txt");
        NIOFileLineReader.read(FILE).subscribe((data, err) -> {
            System.out.println(data);
        }).join();
    }

    @Test
    public void givenFile_whenStringOperate_thenPrint() throws IOException {
        final Path FILE = Paths.get("src", "test", "resources", "fileWithmanyOfLine.txt");
        NIOFileLineReader.read(FILE).filter(line -> !line.trim().isEmpty()).onNext((data, err) -> {
            System.out.println(data);
        }).blockingSubscribe();
    }

    @Test
    public void givenFile_whenRead_thenCancel() throws IOException {
        final Path FILE = Paths.get("src", "test", "resources", "fileWithmanyOfLine.txt");
        CompletableFuture<Void> future = NIOFileLineReader.read(FILE).subscribe((data, err) -> {
            try {
                TimeUnit.MILLISECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            };
            System.out.println(Thread.currentThread().getName());
        });

        try {
            TimeUnit.MILLISECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        };

        future.cancel(false);
    }
}
