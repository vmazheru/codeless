package cl.ugly.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public final class CollectionUtils {

	private CollectionUtils(){}

	/**
	 * Split a list into chunks of the given size.
	 */
    public static <T> List<List<T>> chopped(List<T> list, int chunkSize) {
    	if(chunkSize <= 0) {
    		throw new IllegalArgumentException("bad chunk size");
    	}
    	
    	if(list == null) {
    		return Collections.emptyList();
    	}

    	int size = list.size();
        List<List<T>> parts = new ArrayList<>();
        for (int i = 0; i < size; i += chunkSize) {
            parts.add(new ArrayList<>(list.subList(i, Math.min(size, i + chunkSize))));
        }
        return parts;
    }
    
    /**
     * Split a list into chunks of size less than or equal to the given chunk size.
     * This method guarantees that similar items defined by a group function will always stay
     * in the same chunk.
     *
     * @param list             List to split into chunks
     * @param chunkSize        chunk size
     * @param sorted           is the input sorted.  If not it will be sorted by using the group function
     * @param groupFunction    function which converts an item into a Comparable object used for comparing items and sorting them
     * @return                 a list of chunks (that is lists).  The overall order of items in chunks
     *                         will be the same as an input list if the input list is sorted, or not otherwise.
     * @throws                 RuntimeException if it finds a group of size greater than chunk size
     */
    public static <T,C extends Comparable<C>> List<List<T>> chopped(List<T> list, int chunkSize, boolean sorted, 
            Function<T, C> groupFunction) {
        if(chunkSize <= 0) {
            throw new IllegalArgumentException("bad chunk size");
        }
        
        if(list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        
        // sort input if not sorted
        if (!sorted) {
            Comparator<T> c = (t1, t2) -> groupFunction.apply(t1).compareTo(groupFunction.apply(t2));
            Collections.sort(list, c);
        }
        
        // split the input into chunks
        List<List<T>> parts = new ArrayList<>();
        List<T> part  = new ArrayList<>();
        List<T> group = new ArrayList<>();

        C groupIdentifier = groupFunction.apply(list.get(0));
        
        for (T t : list) {
            C identifier = groupFunction.apply(t);
            boolean newGroup = identifier.compareTo(groupIdentifier) != 0;
            
            if (newGroup) {
                int groupSize = group.size();
                if (groupSize > chunkSize) {
                    throw new RuntimeException("cannot fit a group of items of size " + groupSize + 
                            " into a chunk of size " + chunkSize + ". Chunk size too small");
                }
                boolean exceededChunkSize = part.size() + groupSize > chunkSize;
                if (exceededChunkSize) {
                    parts.add(part);
                    part = new ArrayList<>();
                }
                part.addAll(group);
                group = new ArrayList<>();
                groupIdentifier = identifier;
            }
            group.add(t);
        }

        // leftovers may be present in the group list and in the part list
        int groupSize = group.size();
        if (groupSize > chunkSize) {
            throw new RuntimeException("cannot fit a group of items of size " + groupSize + 
                    " into a chunk of size " + chunkSize + ". Chunk size too small");
        }
        int partSize = part.size();
        boolean exceededChunkSize = partSize + groupSize > chunkSize;
        if (exceededChunkSize) {
            if (partSize  != 0) parts.add(part);
            if (groupSize != 0) parts.add(group);
        } else {
            if (groupSize != 0) part.addAll(group);
            if (!part.isEmpty()) parts.add(part);
        }

        return parts;
    }
    
}
