package cl.ugly.ds;

import cl.ugly.util.StringUtils2;

public class SimpleBitSet {
    
    private final int[] arr;
    private final int size;

    public SimpleBitSet(int size) {
        if(size <= 0) throw new IllegalArgumentException("bit set size must be positive");
        int numElems = size / Integer.SIZE;
        if(size % Integer.SIZE > 0) numElems++;
        this.arr = new int[numElems];
        this.size = size;
    }
    
    public void setBit(int index) {
        int arrElem = getArrElem(index);
        arr[arrElem] = arr[arrElem] | (1 << getShiftValue(index));
    }
    
    public void unsetBit(int index) {
        int arrElem = getArrElem(index);
        arr[arrElem] = arr[arrElem] & ~(1 << getShiftValue(index));
    }
    
    public int getBit(int index) {
        int arrElem = getArrElem(index);
        return 1 & (arr[arrElem] >> getShiftValue(index));
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(int i : arr) {
            sb.append(StringUtils2.toBinaryString(i, Integer.SIZE));
        }
        return sb.toString().substring(0, size);
    }
    
    private static int getArrElem(int index) {
        if(index < 0) throw new IllegalArgumentException("negative array index");
        return index / Integer.SIZE;
    }
    
    private static int getShiftValue(int index) {
        return Integer.SIZE - 1 - index % Integer.SIZE; 
    }
}
