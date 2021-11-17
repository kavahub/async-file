# async-file

[![Maven central](https://img.shields.io/maven-central/v/io.github.kavahub/kavahub-async-file.svg)](https://search.maven.org/artifact/io.github.kavahub/kavahub-async-file)
[![License](https://img.shields.io/github/license/kavahub/async-file.svg)](https://github.com/kavahub/async-file/blob/main/LICENSE)
[![GitHub stars](https://img.shields.io/github/stars/kavahub/async-file.svg)](https://github.com/kavahub/async-file/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/kavahub/async-file.svg)](https://github.com/kavahub/async-file/network/members)
[![GitHub release](https://img.shields.io/github/release/kavahub/async-file.svg?color=blu)](https://github.com/kavahub/async-file/releases)


#### 介绍
框架提供Java异步读写文件工具，使用Java NIO 文件读写库。Java应用程序引入框架可以简单的，异步和非阻塞的读写文件。框架包含三个工具类：


- [`AIOFileReader`](src/main/java/io/github/kavahub/file/reader/AIOFileReader.java) 异步读取文件，使用Java NIO库 [`AsynchronousFileChannel`](https://docs.oracle.com/javase/10/docs/api/java/nio/channels/AsynchronousFileChannel.html) 和 [`CompletionHandler`](https://docs.oracle.com/javase/10/docs/api/java/nio/channels/CompletionHandler.html) 实现。

- [`AIOFileWriter`](src/main/java/io/github/kavahub/file/writer/AIOFileWriter.java) 异步写入文件，使用Java NIO库 [`AsynchronousFileChannel`](https://docs.oracle.com/javase/10/docs/api/java/nio/channels/AsynchronousFileChannel.html) 和 [`CompletionHandler`](https://docs.oracle.com/javase/10/docs/api/java/nio/channels/CompletionHandler.html) 实现。

- [`NIOFileLineReader`](src/main/java/io/github/kavahub/file/reader/NIOFileLineReader.java) 非阻塞读取文件，使用 [`ForkJoinPool`](https://docs.oracle.com/javase/10/docs/api/java/util/concurrent/ForkJoinPool.html) 和 [`BufferedReader`](https://docs.oracle.com/javase/10/docs/api/java/io/BufferedReader.html) 实现

Java提供的 [`Files`](https://docs.oracle.com/javase/10/docs/api/java/nio/file/Files.html) 文件读取功能是阻塞的。

#### 安装教程

首先，如果项目使用Maven工具，在项目的pom.xml文件中添加依赖

```xml
<dependency> 
  <groupId>io.github.kavahub</groupId>
  <artifactId>kava-async-file</artifactId>
  <version>1.0.0.RELEASE</version>
</dependency>
```

如果是Gradle项目，需要添加依赖：

```groovy
implementation 'net.kava:kava-async-file:1.0.0'
```

#### 使用说明

[`AIOFileReader`](src/main/java/io/github/kavahub/file/reader/AIOFileReader.java)方法列表:

- `Query<byte[]> bytes(Path file)` : 读取文件，返回文件数据字节数组，读取的大小有默认缓冲区决定。

- `Query<byte[]> allBytes(Path file)` : 读取文件，返回文件所有数据字节数组。每次按默认缓冲区读取文件，完成后合并。

- `Query<String> line(Path file)` : 读取文件，返回文件行字符串。每次按默认缓冲区读取文件数据字节数组，按换行符分割字节数组。

- `Query<String> allLines(Path file)` : 读取文件，返回文件所有数据字符串。每次按默认缓冲区读取文件数据字节数组，合并后转换成字符串。

默认缓冲区大小定义：

```java
public static final int BUFFER_SIZE = 4096 * 4;
```

示例：

```java
        // 按行读取文件，并输出到控制台
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
```

示例：

```java
        // 统计文件中单词个数，并找出次数最多的单词
        final Path FILE = Paths.get("src", "test", "resources", "fileToCount.txt");

        final int MIN = 5;
        final int MAX = 10;

        ConcurrentHashMap<String, Integer> words = new ConcurrentHashMap<>();
        AIOFileReader.line(FILE)
                // 过滤掉前14行
                .filter(line -> !line.trim().isEmpty()).skip(14)
                // 使用空格分隔
                .flatMapMerge(line -> Query.of(line.split(" ")))
                // 过滤单词
                .filter(word -> word.length() > MIN && word.length() < MAX)
                // 统计单词次数
                .onNext((w, err) -> words.merge(w, 1, Integer::sum))
                // 阻塞，直到文件统计完毕
                .blockingSubscribe();

        Map.Entry<String, ? extends Number> common = Collections.max(words.entrySet(),
                Comparator.comparingInt(e -> e.getValue().intValue()));
        assertEquals("Hokosa", common.getKey());
        assertEquals(183, common.getValue().intValue());
```

示例：

```java
        // 统计“*** END OF ”行之前所有单词的数量
        // 当读取到"*** END OF "行时，读线程会取消读操作，避免继续读取不需要处理的数据

        final Path FILE = Paths.get("src", "test", "resources", "fileToCount.txt");

        int[] count = { 0 };
        AIOFileReader.line(FILE)
                // 过滤空行
                .filter(line -> !line.trim().isEmpty())
                // 忽略前14行
                .skip(14)
                // 忽略掉‘*** END OF ’以后的行
                .takeWhile(line -> !line.contains("*** END OF "))
                // 行按空格切割成单词
                .flatMapMerge(line -> Query.of(line.split("\\W+")))
                // 去重
                .distinct()
                // 统计数量
                .onNext((word, err) -> {
                    if (err == null)
                        count[0]++;
                })
                // 显示处理中的异常
                .onNext((word, err) -> {
                    if (err != null)
                        err.printStackTrace();
                })
                // 阻塞，知道文件读取完成
                .blockingSubscribe();
        assertEquals(5206, count[0]);
```

示例：

```java
        // 详细演示takeWhile的功能:
        // 1. 控制台输出前部文件内容，框架日志提示[Cancel file reading. [16384 bytes] has been readed]，读取操作取消，不在读取文件数据。
        // 2. [16384 bytes] 信息中，16384是框架默认读取缓冲区大小，由此可以判断：文件只读取了一次
        final Path FILE = Paths.get("src", "test", "resources", "fileWithmanyOfLine.txt");

        int[] count = { 0 };
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
                // 终止文件读操纵。
                .takeWhile(line -> false)
                .onNext((data, err) -> {
                    if (err != null) {
                        err.printStackTrace();
                    }

                    if (data != null) {
                        System.out.println("after:" +data);
                    }
                }).blockingSubscribe();

        assertEquals(0, count[0]);
```

示例：

```java
        // 也可以使用cancel方法中断读文件操作

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
```        

示例：

```java
        // 显示读文件线程的名称
        final Path FILE = Paths.get("src", "test", "resources", "fileWithmanyOfLine.txt");

        AIOFileReader.bytes(FILE).subscribe((data, err) -> {
            if (err != null) {
                err.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName());
        }).join();
```

输出结果如下：

```text
Thread-8
Thread-7
Thread-8
Thread-7
Thread-8
Thread-7
Thread-8
Thread-7
Thread-8
Thread-7
...
```

其结果表明：有两个线程读取文件，线程交替读取以保证读取文件数据的顺序，这是 [`AsynchronousFileChannel`](https://docs.oracle.com/javase/10/docs/api/java/nio/channels/AsynchronousFileChannel.html) 实现的


[`AIOFileWriter`](src/main/java/io/github/kavahub/file/writer/AIOFileWriter.java)方法列表:

- `CompletableFuture<Integer> write(Path file, byte[] bytes)` : 字节数组数据写入文件。

- `CompletableFuture<Integer> write(Path file, String line)` : 字符串数据写入文件。

- `CompletableFuture<Integer> write(Path file, Query<String> lines)` : 字符串流数据写入文件。

- `CompletableFuture<Integer> write(Path file, Iterable<String> lines)` : 字符串集合数据写入文件。

示例：

```java
        // 写入字符串
        AIOFileWriter.write(Paths.get(FILE_TO_WRITE), "This is file content：你好").join();
```

示例：

```java
        // 分割字符串写入
        final String content = "This is file content：你好";

        AIOFileWriter.write(Paths.get(FILE_TO_WRITE), String.join(System.lineSeparator(), content.split(" "))).join();
```

示例：

```java
        // 字符流写入
        Query<String>  data = Query.of("This is file content：你好")
            .flatMapMerge(line -> Query.of(line.split(" ")))
            .map((line) -> line + System.lineSeparator());
        AIOFileWriter.write(Paths.get(FILE_TO_WRITE), data).join();
```

示例：

```java
        // 字符流转换后写入
        Query<String>  data = Query.of("This is file content：你好")
            .flatMapMerge(line -> Query.of(line.split(" ")))
            .map((line) -> line + System.lineSeparator());
        AIOFileWriter.write(Paths.get(FILE_TO_WRITE), data).join();
```

示例：

```java
        // 边读边写
        final Path FILE = Paths.get("src", "test", "resources", "fileWithmanyOfLine.txt");

        Query<String> reader = AIOFileReader.line(FILE)
            // 忽略前2行
            .skip(2)
            // 过滤掉空行
            .filter(line -> !line.isBlank())
            // 转换成大写
            .map(String::toUpperCase)
            // 加入换行符
            .map((line) -> line + System.lineSeparator());
        AIOFileWriter.write(Paths.get(FILE_TO_WRITE), reader).join();
```

[`NIOFileLineReader`](src/main/java/io/github/kavahub/file/reader/NIOFileLineReader.java) 方法列表：

`Query<String> read(Path file)` : 读取文件行。

```java
        // 读取文件行并过滤
        final Path FILE = Paths.get("src", "test", "resources", "fileWithmanyOfLine.txt");
        NIOFileLineReader.read(FILE).filter(line -> !line.trim().isEmpty()).onNext((data, err) -> {
            System.out.println(data);
        }).blockingSubscribe();
```


关于使用的建议：

- 文件的异步读写，并不是为了提高文件的读取性能，而是提高文件读取的吞吐量（读取更多的文件，并保持性能，使JVM可以稳定运行）。
- 在大多数情况下，使用Jdk提供的[`Files`](https://docs.oracle.com/javase/10/docs/api/java/nio/file/Files.html)或许更合适。
- 不要为了异步而异步，找到问题所在，也许解决问题的关键不是异步。

建议使用优先级： `Java NIO Files` > `NIOFileLineReader` > `AIOFileReader`

#### 性能

性能测试，参考 [`ReadLineBenchmark`](src/test/java/io/github/kavahub/file/performance/ReadLineBenchmark.java) 。 其他开源项目文件读写的性能测试 [`ReadFileBenchmark`](https://gitee.com/yangyunjiao/learn-java/blob/master/core-java/core-java-io/src/main/java/net/learnjava/ReadFileBenchmark.java)


#### 构建项目

克隆代码到本地，然后运行mvn命令, 执行编译，测试，打包项目：

```text
mvn clean install
```

#### 发布项目

首先，确保项目可以正确构建。然后执行下面的命令(发布的文件要以release结尾，如：kavahub-async-file-1.0.0.RELEASE.jar)：

```text
mvn release:prepare -Prelease
mvn release:perform -Prelease
```

上面操作将包上传到了Staging Repository，需要转入Release Repository，执行命令：

```text
mvn nexus-staging:release
```

以上操作全部成功，发布完成。

发布SNAPSHOT包到仓库，命令如下：

```text
mvn clean deploy -Prelease
```

取消Staging Repository中的包，命令如下：

```text
mvn nexus-staging:drop 
```

#### 其他开源项目

- [RxIo](https://github.com/javasync/RxIo) : Asynchronous non-blocking File Reader and Writer library for Java

#### 参考文档

- [vsCode利用git连接github](https://www.jianshu.com/p/f836da434e18)
- [如何将自己的代码发布到Maven中央仓库](https://www.cnblogs.com/songyz/p/11387978.html)
- [Deploying to OSSRH with Apache Maven - Introduction](https://central.sonatype.org/publish/publish-maven/)



