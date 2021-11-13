package com.github.kavahub.file.performance;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.github.kavahub.file.reader.AIOFileReader;
import com.github.kavahub.file.reader.NIOFileLineReader;

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


@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1)
@Warmup(iterations = 1)
@Fork(1)
public class ReadLineBenchmark {
    private final static Path file = Paths.get("src", "test", "resources", "fileWithmanyOfLine.txt");
    
    public static void main(String[] args) throws Exception {
        ChainedOptionsBuilder opts = new OptionsBuilder().include(ReadLineBenchmark.class.getSimpleName());

        new Runner(opts.threads(16).build()).run();
    }

    @Benchmark
    public List<String> readLineUsingJavaFiles() throws IOException {
        return Files.readAllLines(file);
    }

    @Benchmark
    public List<String> readLineUsingAsyncReader(){
        List<String> result = new ArrayList<>();
        AIOFileReader.line(file).subscribe((data, err) -> result.add(data)).join();

        return result;
    }

    @Benchmark
    public List<String> readLineUsingFileLineReader(){
        List<String> result = new ArrayList<>();
        NIOFileLineReader.read(file).subscribe((data, err) -> result.add(data)).join();
        return result;
    }
}
