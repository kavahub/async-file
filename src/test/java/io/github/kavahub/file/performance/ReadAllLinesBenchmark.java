package io.github.kavahub.file.performance;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import io.github.kavahub.file.reader.AIOFileReader;


@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1)
@Warmup(iterations = 1)
@Fork(1)
public class ReadAllLinesBenchmark {
    private final static Path file = Paths.get("src", "test", "resources", "fileWithmanyOfLine.txt");
    
    public static void main(String[] args) throws Exception {
        ChainedOptionsBuilder opts = new OptionsBuilder().include(ReadAllLinesBenchmark.class.getSimpleName());

        new Runner(opts.threads(1).build()).run();
    }

    @Benchmark
    public String readAllUsingJavaFiles() throws IOException {
        return Files.readString(file);
    }

    @Benchmark
    public String readAllUsingAsyncReader() throws InterruptedException{
        StringBuilder rslt = new StringBuilder();
        AIOFileReader.allLines(file).subscribe((data, error) -> {
            rslt.append(data);
        }).join();
         
        return rslt.toString();
    }

}
