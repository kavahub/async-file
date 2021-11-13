package com.github.kavahub.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import com.github.kavahub.file.query.Query;
import com.github.kavahub.file.reader.AIOFileReader;

import org.junit.jupiter.api.Test;

public class AIOFileReaderTest {
    @Test
    public void givenFile_whenLine() throws IOException {
        final Path FILE = Paths.get("src", "test", "resources", "fileToRead.txt");
        List<String> actual = new ArrayList<>();
        AIOFileReader.line(FILE).subscribe((data, err) -> {
            actual.add(data);
        }).join();

        assertThat(actual.size()).isEqualTo(6);
        assertThat(actual.get(0)).isEqualTo("super");
        assertThat(actual.get(4)).isEqualTo("中文");
        assertThat(actual.get(5)).isEqualTo("massive");
    }

    @Test
    public void givenFileLastLineNoEnter_whenLine() throws IOException {
        // 最后一行没有回车
        final Path FILE = Paths.get("src", "test", "resources", "fileToReadLastLineNoEnter.txt");

        List<String> actual = new ArrayList<>();
        AIOFileReader.line(FILE).subscribe((data, err) -> {
            actual.add(data);
        }).join();

        assertThat(actual.size()).isEqualTo(6);
        assertThat(actual.get(0)).isEqualTo("super");
        assertThat(actual.get(4)).isEqualTo("中文");
        assertThat(actual.get(5)).isEqualTo("massive");
    }

    @Test
    public void givenFileEmptyLine_whenLine() throws IOException {
        // 前后有空行
        final Path FILE = Paths.get("src", "test", "resources", "fileToReadEmptyLine.txt");

        List<String> actual = new ArrayList<>();
        AIOFileReader.line(FILE).subscribe((data, err) -> {
            if (err != null) {
                err.printStackTrace();
            }

            actual.add(data);
        }).join();

        assertThat(actual.size()).isEqualTo(6);
        assertThat(actual.get(0)).isEqualTo(" ");
        assertThat(actual.get(1)).isEqualTo("super");
        assertThat(actual.get(2)).isEqualTo("");
        assertThat(actual.get(4)).isEqualTo("massive");
        assertThat(actual.get(5)).isEqualTo("");
    }

    @Test
    public void givenFileWithManyLine_whenLine() throws IOException {
        final Path FILE = Paths.get("src", "test", "resources", "fileWithmanyOfLine.txt");
        List<String> actual = new ArrayList<>();
        AIOFileReader.line(FILE).subscribe((data, err) -> {
            if (err != null) {
                err.printStackTrace();
            }
            actual.add(data);
        }).join();

        assertThat(actual).containsExactly(Files.readAllLines(FILE).toArray(new String[0]));
    }

    @Test
    public void givenFileWithManyLine_whenLineAndLittleBufferSize() throws IOException {
        final Path FILE = Paths.get("src", "test", "resources", "fileWithmanyOfLine.txt");
        List<String> actual = new ArrayList<>();
        AIOFileReader.line(FILE, 128).subscribe((data, err) -> {
            if (err != null) {
                err.printStackTrace();
            }
            actual.add(data);
        }).join();

        assertThat(actual).containsExactly(Files.readAllLines(FILE).toArray(new String[0]));
    }

    @Test
    public void givenFile_whenBytes() throws IOException {
        final Path FILE = Paths.get("src", "test", "resources", "fileToRead.txt");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        AIOFileReader.bytes(FILE).subscribe((data, err) -> {
            if (err != null) {
                err.printStackTrace();
            }

            try {
                out.write(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).join();

        assertArrayEquals(out.toByteArray(), Files.readAllBytes(FILE));

        out.close();
    }
    
    @Test
    public void givenFile_whenAllBytes() throws IOException {
        final Path FILE = Paths.get("src", "test", "resources", "fileWithmanyOfLine.txt");

        AtomicReference<byte[]> actual = new AtomicReference<>();

        AIOFileReader.allBytes(FILE).subscribe((data, err) -> {
            if (err != null) {
                err.printStackTrace();
            }
            actual.set(data);
        }).join();

        assertThat(actual.get()).isEqualTo(Files.readAllBytes(FILE));
    }

    @Test
    public void givenFile_whenAllLines() throws IOException {
        final Path FILE = Paths.get("src", "test", "resources", "fileWithmanyOfLine.txt");

        StringBuilder actual = new StringBuilder();

        AIOFileReader.allLines(FILE).subscribe((data, err) -> {
            if (err != null) {
                err.printStackTrace();
            }
            actual.append(data);
        }).join();

        assertThat(actual.toString()).isEqualTo(Files.readString(FILE));
    }

    @Test
    public void givenReadLine_whenSkipFlatMapMergeFilterOnNext() {
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
    }

    @Test
    public void givenReadLine_whenTakenWhile() {
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
    }

    @Test
    public void giveFileAndRead_whenTakeWhile_thenStopRead() {
        final Path FILE = Paths.get("src", "test", "resources", "fileToCount.txt");

        int[] count = { 0 };
        AIOFileReader.line(FILE)
                // 控制台输出
                .onNext((data, err) -> {
                    if (err != null) {
                        err.printStackTrace();
                    }
                })
                // 终止文件读操纵
                .takeWhile(line -> false)
                //
                .onNext((word, err) -> {
                    count[0]++;
                }).blockingSubscribe();

        assertEquals(0, count[0]);
    }
}
