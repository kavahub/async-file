package io.github.kavahub.file;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import io.github.kavahub.file.reader.AIOFileReader;

public class AsyncFileDocExample {

    /**
     * 读取文件，按行订阅数据
     * 
     * <p>
     * 这种适用广泛，我们经常读取文本文件，统计相关信息，如：单词统计等，只需要 编写 <code>doSomethingData</code> 代码。编写
     * <code>doSomethingError</code>代码，处理 读或者业务处理中的异常，我们建议将异常输出到日志
     * 
     * <p>
     * <code>doSomethingData</code> 业务中的异常，会导致读文件中断，整个文件操作终止。异常 信息可以在
     * <code>doSomethingError</code> 中处理
     */
    @Test
    public void givenFileReadLine_whenDoSomething_thenWait() {
        // 按行读取文件，并输出到控制台
        final Path FILE = Paths.get("src", "test", "resources", "fileWithmanyOfLine.txt");

        AIOFileReader.line(FILE)
                // 订阅行数据
                .subscribe(data -> {
                    // 文件行处理，如输出到控制台
                    System.out.println(data);
                    // doSomethingData(data)
                }, err -> {
                    // 异常处理
                    err.printStackTrace();
                    // doSomethingError(err)
                })
                // 等待文件读取完成
                .join();

        // 也可以这样写
        AIOFileReader.line(FILE)
                // 订阅数据
                .onNext(data -> {
                    // 文件行处理，如输出到控制台
                    System.out.println(data);
                    // doSomethingData(data)
                })
                // 订阅异常
                .onError(err -> {
                    // 异常处理
                    err.printStackTrace();
                    // doSomethingError(err)
                })
                // 等待文件读取完成
                .blockingSubscribe();
    }

    /**
     * {@link AIOFileReader} 读文件是异步的，所以可以处理写其他的业务
     * 
     * @throws InterruptedException
     */
    @Test
    public void givenFileReadLine_whenDoSomething_thenDoOtherthing() throws InterruptedException {
        // 按行读取文件，并输出到控制台
        final Path FILE = Paths.get("src", "test", "resources", "fileWithmanyOfLine.txt");
        CompletableFuture<Void> future = AIOFileReader.line(FILE)
                // 订阅行数据
                .subscribe(data -> {
                    // 文件行处理，如输出到控制台
                    System.out.println(data);
                    // doSomethingData(data)
                }, err -> {
                    // 异常处理
                    err.printStackTrace();
                    // doSomethingError(err)
                });

        // 处理其他业务逻辑
        TimeUnit.SECONDS.sleep(2); // 模拟业务

        // 循环，直到文件处理完成
        while (!future.isDone()) {
        }

        System.out.println("文件处理完成");
    }
}
