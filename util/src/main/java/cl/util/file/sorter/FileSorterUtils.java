package cl.util.file.sorter;

import static cl.core.decorator.exception.ExceptionDecorators.*;

import java.io.File;
import java.util.function.BiConsumer;

import cl.core.util.FileUtils;

class FileSorterUtils {

    static void withTempFile(File original, BiConsumer<File, File> f) {
        File sortedFile = uncheck(() -> File.createTempFile("tmp", "_sorted"));
        f.accept(original, sortedFile);
        FileUtils.moveFile(sortedFile, original);
    }
    
}
