package io.github.kavahub.file;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;

import io.github.kavahub.file.writer.AIOFileWriter;
import io.github.kavahub.file.writer.CompletableFileWriter;

public class SystemInToFileExample {
    
    public static void main(String[] arg) throws IOException {
        // 异步写入文件
        CompletableFileWriter writer = AIOFileWriter.of(Paths.get("systemIn.txt"));

        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNext()) {
                String line = scanner.next();
                if ("bye".equals(line)) {
                    break;
                }
                writer.write(line + System.lineSeparator());
            }

            writer.getPosition().whenComplete((size, error) -> {
                if (error != null) {
                    error.printStackTrace();
                }

                System.out.println("输入字节数：" + size);
            });
        }
    }
}
