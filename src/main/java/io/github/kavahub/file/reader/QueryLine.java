package io.github.kavahub.file.reader;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import io.github.kavahub.file.Consts;
import io.github.kavahub.file.query.Query;

public class QueryLine extends Query<String> {
    private final Query<byte[]> query;
    private byte[] remainingByte = new byte[0];

    public QueryLine(Query<byte[]> query) {
        this.query = query;
    }

    @Override
    public CompletableFuture<Void> subscribe(Consumer<? super String> onNext, Consumer<? super Throwable> onError) {
        CompletableFuture<Void> future = query.subscribe(data -> {
                // 需要识别换行符，按行输出
                List<String> lines = produceLines(data);
                lines.forEach(line -> onNext.accept(line));
        }, onError);
        
        future.whenComplete((data, error) -> {
            // 处理最后一行没有换行的问题
            if (remainingByte.length > 0) {
                onNext.accept(new String(remainingByte, StandardCharsets.UTF_8));
                remainingByte = new byte[0];
            }
        });

        return future;
    }

    // Windows中：回车符为 ‘\r’，回到行首；换行符为 ‘\n’，换到当前位置的下一行；‘\r\n’
    // Unix系统中：觉得每行结尾加两个字符没有必要，故结尾只有换行符，即‘\n’;
    // Mac系统中：每行结尾只有回车符，即‘\r’。

    /**
     * 
     * @param bytes
     * @return
     */
    private List<String> produceLines(final byte[] bytes) {
        List<String> lines = new ArrayList<>();

        byte[] data = mergeRemainingByte(bytes);

        int length = data.length;
        int read = 0;
        int start = 0;
        int end = 0;
        while (read < length) {
            if (data[read] == Consts.LF) { // '\n'
                if (read > 0 && data[read - 1] == Consts.CR)
                    end--;

                lines.add(getLine(data, start, end));
                start = read + 1;
                end = read + 1;
            } else {
                end++;
            }
            read++;
        }

        if (end > start) {
            final int newSize = end - start;
            remainingByte = new byte[newSize];
            System.arraycopy(data, start, remainingByte, 0, newSize);
        }

        return lines;
    }

    /**
     * 
     * @param data
     * @param start
     * @param end
     * @return
     */
    private String getLine(byte[] data, int start, int end) {
        return new String(data, start, end - start, StandardCharsets.UTF_8);
    }

    /**
     * 
     * @param data
     * @return
     */
    private byte[] mergeRemainingByte(byte[] data) {
        final int totalLength = remainingByte.length + data.length;
        ByteBuffer rslt = ByteBuffer.allocate(totalLength);
        rslt.put(remainingByte);
        rslt.put(data);

        // 重新初始化
        remainingByte = new byte[0];

        return rslt.array();
    }
}
