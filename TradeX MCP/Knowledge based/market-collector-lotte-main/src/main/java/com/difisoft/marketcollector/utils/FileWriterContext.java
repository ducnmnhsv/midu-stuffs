package com.difisoft.marketcollector.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.FileWriter;
import java.io.IOException;

@Slf4j
public class FileWriterContext {
    private final String fileName;
    private final FileWriter fileWriter;

    public FileWriterContext(String fileName) {
        FileWriter tmp;
        this.fileName = fileName;
        try {
            tmp = new FileWriter(fileName);
        } catch (IOException e) {
            tmp = null;
            log.error("fail to open file {}", fileName, e);
        }
        this.fileWriter = tmp;
    }

    public void writeLine(String line) {
        if (this.fileWriter != null) {
            try {
                fileWriter.write(line);
                this.writeNewLine();
            } catch (IOException e) {
                log.error("fail to write '{}' to file {}", line, fileName, e);
            }
        }
    }

    public void writeNewLine() {
        if (this.fileWriter != null) {
            try {
                fileWriter.write(System.lineSeparator());
            } catch (IOException e) {
            }
        }
    }

    public void close() {
        try {
            this.fileWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            this.fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
