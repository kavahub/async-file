package io.github.kavahub.file;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import io.github.kavahub.file.reader.AIOFileReader;

public class AIOFileReaderManualTest {
    private SecureRandom random = new SecureRandom();

    @Test
    public void giveFile_whenReadLine_thenPrint() {
        final Path FILE = Paths.get("src", "test", "resources", "fileWithmanyOfLine.txt");
        AIOFileReader.line(FILE).subscribe(data -> {
            // 文件行处理，如输出到控制台
            System.out.println(data);
        }, err -> err.printStackTrace())
                // 等待所有行处理完成
                .join();
    }

    @Test
    public void givenFile_whenLine_thenPrintThread() throws IOException {
        final Path FILE = Paths.get("src", "test", "resources", "fileWithmanyOfLine.txt");

        AIOFileReader.line(FILE).subscribe(data -> {
            System.out.println(Thread.currentThread().getName());
        }, err -> err.printStackTrace()).join();
    }

    @Test
    public void givenFile_whenBytes_thenPrintThread() throws IOException {
        // 显示读文件线程的名称
        final Path FILE = Paths.get("src", "test", "resources", "fileWithmanyOfLine.txt");

        AIOFileReader.bytes(FILE).subscribe(data -> {
            System.out.println(Thread.currentThread().getName());
        }, err -> err.printStackTrace()).join();
    }

    @Test
    public void giveFileAndRead_whenTakeWhile_thenStopRead() {
        // 详细演示takeWhile的功能:
        // 1. 控制台输出前部文件内容，框架日志提示[Cancel file reading. [16384 bytes] has been
        // readed]，读取操作取消，不在读取文件数据。
        // 2. [16384 bytes] 信息中，16384是框架默认读取缓冲区大小，由此可以判断：文件只读取了一次
        final Path FILE = Paths.get("src", "test", "resources", "fileWithmanyOfLine.txt");

        AIOFileReader.line(FILE)
                // 控制台输出
                .onNext((data) -> {
                    System.out.println("before:" + data);
                })
                // 终止文件读操纵
                .takeWhile(line -> false)
                //
                .onNext((data) -> {
                    System.out.println("after:" + data);
                })
                // 异常处理
                .onError(err -> err.printStackTrace()).blockingSubscribe();
    }

    @Test
    public void giveFileAndReadLine_whenCancel_thenCanceled() throws IOException, InterruptedException {
        // 也可以使用cancel方法中断读文件操作
        final Path FILE = Paths.get("src", "test", "resources", "fileWithmanyOfLine.txt");

        CompletableFuture<Void> future = AIOFileReader.line(FILE).subscribe(data -> {
            try {
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName());
        }, err -> err.printStackTrace());

        TimeUnit.MILLISECONDS.sleep(1000);

        future.cancel(false);
    }

    @Test
    public void giveFileAndReadBytes_whenCancel_thenCanceled() throws IOException, InterruptedException {
        final Path FILE = Paths.get("src", "test", "resources", "fileWithmanyOfLine.txt");

        CompletableFuture<Void> future = AIOFileReader.bytes(FILE).subscribe(data -> {
            try {
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName());
        }, err -> err.printStackTrace());

        TimeUnit.MILLISECONDS.sleep(50);

        future.cancel(false);
    }

    @Test
    public void giveFileAndReadLines_whenCancel_thenNotCanceled()
            throws IOException, InterruptedException, ExecutionException {
        final Path FILE = Paths.get("src", "test", "resources", "fileWithmanyOfLine.txt");

        CompletableFuture<Void> future = AIOFileReader.allLines(FILE).subscribe(data -> {
            System.out.println(Thread.currentThread().getName());
        }, err -> err.printStackTrace());

        TimeUnit.MILLISECONDS.sleep(100);
        // cancel无效
        future.cancel(false);
    }

    @Test
    public void giveFileAndReadAllBytes_whenCancel_thenNotCanceled()
            throws IOException, InterruptedException, ExecutionException {
        final Path FILE = Paths.get("src", "test", "resources", "fileWithmanyOfLine.txt");
        CompletableFuture<Void> future = AIOFileReader.allBytes(FILE).subscribe(data -> {
            System.out.println(Thread.currentThread().getName());
        }, err -> err.printStackTrace());

        TimeUnit.MILLISECONDS.sleep(100);
        // cancel无效
        future.cancel(false);
    }

    @Test
    public void giveFile_whenOnNextException_thenReadCancel() throws IOException, InterruptedException {
        final Path FILE = Paths.get("src", "test", "resources", "fileWithmanyOfLine.txt");

        AIOFileReader.line(FILE).onNext(data -> {
            if (random.nextBoolean()) {
                throw new RuntimeException("业务处理异常");
            }

            System.out.println("data :" + data);
        }).blockingSubscribe();
    }

    @Test
    public void giveFile_whenOnNextExceptionAndSubscribe_thenReadCancel() throws IOException, InterruptedException {
        final Path FILE = Paths.get("src", "test", "resources", "fileWithmanyOfLine.txt");
        AIOFileReader.bytes(FILE).onNext(data -> {
            if (random.nextBoolean()) {
                throw new RuntimeException("业务处理异常");
            }

            System.out.println("data :" + data);
        }).subscribe(data -> {
        }, err -> err.printStackTrace()).join();

    }
}
