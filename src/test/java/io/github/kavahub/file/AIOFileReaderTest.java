package io.github.kavahub.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import io.github.kavahub.file.query.Query;
import io.github.kavahub.file.reader.AIOFileReader;

public class AIOFileReaderTest {
    @Test
    public void givenFile_whenLine() throws IOException {
        final Path FILE = Paths.get("src", "test", "resources", "fileToRead.txt");
        List<String> actual = new ArrayList<>();
        AIOFileReader.line(FILE).subscribe(data-> {
            actual.add(data);
        }, err -> err.printStackTrace()).join();

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
        AIOFileReader.line(FILE).subscribe(data -> {
            actual.add(data);
        }, err -> err.printStackTrace()).join();

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
        AIOFileReader.line(FILE).subscribe(data -> {
            actual.add(data);
        }, err -> err.printStackTrace()).join();

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
        AIOFileReader.line(FILE).subscribe(data -> {
            actual.add(data);
        }, err -> err.printStackTrace()).join();

        assertThat(actual).containsExactly(Files.readAllLines(FILE).toArray(new String[0]));
    }

    @Test
    public void givenFileWithManyLine_whenLineAndLittleBufferSize() throws IOException {
        final Path FILE = Paths.get("src", "test", "resources", "fileWithmanyOfLine.txt");
        List<String> actual = new ArrayList<>();
        AIOFileReader.line(FILE, 128).subscribe(data -> {
            actual.add(data);
        }, err -> err.printStackTrace()).join();

        assertThat(actual).containsExactly(Files.readAllLines(FILE).toArray(new String[0]));
    }

    @Test
    public void givenFile_whenBytes() throws IOException {
        final Path FILE = Paths.get("src", "test", "resources", "fileToRead.txt");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        AIOFileReader.bytes(FILE).subscribe(data -> {
            try {
                out.write(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, err -> err.printStackTrace()).join();

        assertArrayEquals(out.toByteArray(), Files.readAllBytes(FILE));

        out.close();
    }

    @Test
    public void givenFile_whenAllBytes() throws IOException {
        final Path FILE = Paths.get("src", "test", "resources", "fileWithmanyOfLine.txt");

        AtomicReference<byte[]> actual = new AtomicReference<>();

        AIOFileReader.allBytes(FILE).subscribe(data -> {
            actual.set(data);
        }, err -> err.printStackTrace()).join();

        assertThat(actual.get()).isEqualTo(Files.readAllBytes(FILE));
    }

    @Test
    public void givenFile_whenAllLines() throws IOException {
        final Path FILE = Paths.get("src", "test", "resources", "fileWithmanyOfLine.txt");

        StringBuilder actual = new StringBuilder();

        AIOFileReader.allLines(FILE).subscribe(data -> {
            actual.append(data);
        }, err -> err.printStackTrace()).join();

        assertThat(actual.toString()).isEqualTo(Files.readString(FILE));
    }

    @Test
    public void givenReadLine_whenCountWord() {
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
                .onNext((w) -> words.merge(w, 1, Integer::sum))
                // 异常处理
                .onError(err -> err.printStackTrace())
                // 阻塞，直到文件统计完毕
                .blockingSubscribe();

        Map.Entry<String, ? extends Number> common = Collections.max(words.entrySet(),
                Comparator.comparingInt(e -> e.getValue().intValue()));
        assertEquals("Hokosa", common.getKey());
        assertEquals(183, common.getValue().intValue());
    }

    @Test
    public void givenReadLine_whenTakenWhile_thenReadEnd() {
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
                .onNext((word) -> {
                        count[0]++;
                })
                // 异常处理
                .onError(err -> err.printStackTrace())                
                // 阻塞，知道文件读取完成
                .blockingSubscribe();
        assertEquals(5206, count[0]);
    }

    @Test
    public void giveReadLine_whenTakeWhile_thenStopRead() {
        final Path FILE = Paths.get("src", "test", "resources", "fileToCount.txt");

        int[] count = { 0 };
        AIOFileReader.line(FILE)
                // 控制台输出
                .onNext((data) -> {
                    System.out.println(data);
                })
                // 终止文件读操纵
                .takeWhile(line -> false)
                //
                .onNext((word) -> {
                    count[0]++;
                }).blockingSubscribe();

        assertEquals(0, count[0]);
    }

    @Test
    public void givenFileNotExist_whenRead_thenException() throws IOException {
        final Path FILE = Paths.get("src", "test", "resources", "abc.txt");

        AIOFileReader.line(FILE).subscribe(data -> {
        }, err -> assertThat(err).isInstanceOf(NoSuchFileException.class)).join();
    }

    @Test
    public void giveFile_whenOnNextException_thenException() throws IOException, InterruptedException {
        final Path FILE = Paths.get("src", "test", "resources", "fileWithmanyOfLine.txt");

        AIOFileReader.line(FILE).onNext((data) -> {
            throw new RuntimeException("业务异常");
        }).subscribe((data) -> {
        }, err ->  assertThat(err).isInstanceOf(RuntimeException.class).hasMessageContaining("业务异常")).join();

    }
}
