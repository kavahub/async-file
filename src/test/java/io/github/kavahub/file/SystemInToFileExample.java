package io.github.kavahub.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import io.github.kavahub.file.writer.AIOFileWriter;
import io.github.kavahub.file.writer.CompletableFileWriter;

/**
 * 控制台输入信息存入文件
 * 
 */
public class SystemInToFileExample {

    public static void main(String[] arg) throws IOException {
        Path file = Paths.get("target", "SystemInToFileExample.txt");
        Files.deleteIfExists(file);
        
        // 异步写入文件
        try (CompletableFileWriter writer = AIOFileWriter.of(file);
                Scanner scanner = new Scanner(System.in);) {
            
            // 空行也写入
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if ("bye".equals(line)) {
                    break;
                }
                writer.write(line + System.lineSeparator());
            }

            // 写入完成后
            writer.getPosition().whenComplete((size, error) -> {
                if (error != null) {
                    error.printStackTrace();
                    
                }

                System.out.println("输入字节数：" + size);
            });
        }
    }
}
