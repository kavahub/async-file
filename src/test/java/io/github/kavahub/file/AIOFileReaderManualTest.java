package io.github.kavahub.file;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import io.github.kavahub.file.reader.AIOFileReader;

public class AIOFileReaderManualTest {
    @Test
    public void giveFile_whenReadLine_thenPrint() {
        final Path FILE = Paths.get("src", "test", "resources", "fileWithmanyOfLine.txt");
        AIOFileReader.line(FILE).subscribe((data, err) -> {
            if (err != null) {
                // 处理异常，如记录日志
                err.printStackTrace();
            }

            if (data != null) {
                // 文件行处理，如输出到控制台
                System.out.println(data);
            }
        })
        // 等待所有行处理完成
        .join();
    }
    
    @Test
    public void givenFile_whenLine_thenPrintThread() throws IOException {
        final Path FILE = Paths.get("src", "test", "resources", "fileWithmanyOfLine.txt");

        AIOFileReader.line(FILE).subscribe((data, err) -> {
            if (err != null) {
                err.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName());
        }).join();
    }

    @Test
    public void givenFile_whenBytes_thenPrintThread() throws IOException {
        final Path FILE = Paths.get("src", "test", "resources", "fileWithmanyOfLine.txt");

        AIOFileReader.bytes(FILE).subscribe((data, err) -> {
            if (err != null) {
                err.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName());
        }).join();
    }

    @Test
    public void giveFileAndRead_whenTakeWhile_thenStopRead() {
        final Path FILE = Paths.get("src", "test", "resources", "fileWithmanyOfLine.txt");

        AIOFileReader.line(FILE)
                // 控制台输出
                .onNext((data, err) -> {
                    if (err != null) {
                        err.printStackTrace();
                    }

                    if (data != null) {
                        System.out.println("before:" + data);
                    }
                })
                // 终止文件读操纵
                .takeWhile(line -> false)
                //
                .onNext((data, err) -> {
                    if (err != null) {
                        err.printStackTrace();
                    }

                    if (data != null) {
                        System.out.println("after:" +data);
                    }
                }).blockingSubscribe();
    }

    @Test
    public void giveFileAndReadLine_whenCancel_thenCanceled() throws IOException, InterruptedException {
        final Path FILE = Paths.get("src", "test", "resources", "fileWithmanyOfLine.txt");

        CompletableFuture<Void> future = AIOFileReader.line(FILE).subscribe((data, err) -> {
            if (err != null) {
                System.out.println("error:" + err.getMessage());
            }
            try {
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName());
        });

        TimeUnit.MILLISECONDS.sleep(1000);

        future.cancel(false);
    }

    @Test
    public void giveFileAndReadBytes_whenCancel_thenCanceled() throws IOException, InterruptedException {
        final Path FILE = Paths.get("src", "test", "resources", "fileWithmanyOfLine.txt");

        CompletableFuture<Void> future = AIOFileReader.bytes(FILE).subscribe((data, err) -> {
            if (err != null) {
                System.out.println("error:" + err.getMessage());
            }
            try {
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName());
        });

        TimeUnit.MILLISECONDS.sleep(500);

        future.cancel(false);
    }

    @Test
    public void giveFileAndReadLines_whenCancel_thenNotCanceled()
            throws IOException, InterruptedException, ExecutionException {
        final Path FILE = Paths.get("src", "test", "resources", "fileWithmanyOfLine.txt");
        CompletableFuture<Void> future = AIOFileReader.allLines(FILE).subscribe((data, err) -> {
            if (err != null) {
                err.printStackTrace();
            }

            System.out.println(Thread.currentThread().getName());
        });

        TimeUnit.MILLISECONDS.sleep(100);

        future.cancel(false);
    }

    @Test
    public void giveFileAndReadAllBytes_whenCancel_thenNotCanceled()
            throws IOException, InterruptedException, ExecutionException {
        final Path FILE = Paths.get("src", "test", "resources", "fileWithmanyOfLine.txt");
        CompletableFuture<Void> future = AIOFileReader.allBytes(FILE).subscribe((data, err) -> {
            if (err != null) {
                err.printStackTrace();
            }

            System.out.println(Thread.currentThread().getName());
        });

        TimeUnit.MILLISECONDS.sleep(1000);

        future.cancel(false);
    }
}
