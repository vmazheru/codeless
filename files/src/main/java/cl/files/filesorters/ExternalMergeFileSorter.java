package cl.files.filesorters;

import static cl.core.decorator.exception.ExceptionDecorators.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import cl.core.ds.Counter;
import cl.core.util.FileUtils;
import cl.files.serializers.Serializer;
import cl.files.serializers.iterators.ObjectIterator;
import cl.files.serializers.writers.ObjectWriter;

/**
 * Implementation of {@code FileSorter} interface which sorts data by splitting it into
 * multiple intermediary files of manageable size, sorting them one at a time in memory, and them
 * merging sorted files into one result file.
 */
public class ExternalMergeFileSorter<T> extends FileSorterSupport<T> {
    
    public ExternalMergeFileSorter(Serializer<T,T> serializer) {
        super(serializer);
    }

    @Override
    protected void sort(ObjectIterator<T> iterator, ObjectWriter<T> writer) throws IOException {
        List<File> inprocessFiles = null;
        try {
            inprocessFiles = splitIntoSortedFiles(iterator);
            if (!inprocessFiles.isEmpty()) {
                mergeFiles(inprocessFiles, writer);
            }
        } finally {
            if (inprocessFiles != null) {
                inprocessFiles.forEach(File::delete);
            }
        }
    }
    
    private List<File> splitIntoSortedFiles(ObjectIterator<T> iterator) {
        Path inprocessDir = getInprocessDir();
        int numObjectsPerFile = get(FileSorter.numObjectsPerFile);
        List<File> inprocessFiles = new ArrayList<>(); 
        Counter c = new Counter();
        iterator.forEachBatch(numObjectsPerFile, batch -> {
            uncheck(() ->
                inprocessFiles.add(writeNextInprocessFile(batch, inprocessDir, c.getAndIncrement()))
            );
        });
        return inprocessFiles;
    }
    
    private File writeNextInprocessFile(List<T> objects, Path inprocessDir, int fileNum) throws IOException {
        Collections.sort(objects, getComparator());
        File file = getNextInprocessFile(inprocessDir, fileNum).toFile();
        try (ObjectWriter<T> writer = getSerializer().getWriter().clone(file)) {
            writer.write(objects);
        }
        return file;
    }
    
    private static Path getInprocessDir() {
        return uncheck(() -> Files.createTempDirectory("fs_"));
    }
    
    private static Path getNextInprocessFile(Path inprocessDir, int fileNum) throws IOException {
        return Files.createTempFile(inprocessDir, "fs_", "_" + fileNum);
    }
    
    private void mergeFiles(List<File> inprocessFiles, ObjectWriter<T> writer) {
        @SuppressWarnings("unchecked")
        T[] objects = (T[])new Object[inprocessFiles.size()];
        
        @SuppressWarnings("unchecked")
        ObjectIterator<T>[] iterators = inprocessFiles.stream()
                .map(f -> getSerializer().getIterator().clone(f)).toArray(ObjectIterator[]::new);
        
        Comparator<T> comparator = getComparator();
        boolean removeDuplicates = get(FileSorter.removeDuplicates);
        
        T prev = null;
        
        try {
            while (Stream.of(iterators).anyMatch(Objects::nonNull)) {
                uncheck(() -> {
                    for (int i = 0; i < iterators.length; i++) {
                        ObjectIterator<T> iter = iterators[i];
                        if (iter != null && objects[i] == null) {
                            if (iter.hasNext()) {
                                objects[i] = iter.next();
                            } else {
                                iterators[i].close();
                                iterators[i] = null;
                            }
                        }
                    }
                });
    
                T min = null;
                int minIndex = -1;
                for(int i = 0; i < objects.length; i++) {
                    T obj = objects[i];
                    if(obj != null) {
                        if(min == null || comparator.compare(obj, min) < 0) {
                            min = obj;
                            minIndex = i;
                        }
                    }
                }
                
                if(min != null) {
                    objects[minIndex] = null;
                    if(removeDuplicates) {
                        if(prev == null || comparator.compare(min, prev) != 0) {
                            prev = min;
                            writer.write(min);
                        }
                    } else {
                        writer.write(min);
                    }
                }
            }
        } finally {
            FileUtils.close(iterators);
        }
    }

}
