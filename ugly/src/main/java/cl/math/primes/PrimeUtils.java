package cl.math.primes;

import java.util.ArrayList;
import java.util.List;

public final class PrimeUtils {
    
    
    private PrimeUtils(){}
    
    
    public static boolean isPrime(int n) {
        for (int i = 2; i < n; i++) {
            if (n % i == 0) return false;
        }
        return true;
    }

    
    public static void main(String[] args) {
        int A = 11 * 13 * 17;
        int B = 3 * 5 * 7;
        
        List<Integer> found = new ArrayList<>();
        List<Integer> Ks = new ArrayList<>();
        
        int K = 0;
        while (true) {
            K++;
            int i = K * A + B;
            if (isPrime(i)) {
                found.add(i);
                Ks.add(K);
            }
            if (found.size() >= 10) break;
        }
        
        System.out.println("A is 11 x 13 x 17 = " + A);
        System.out.println("B is 3 x 5 x 7 = " + B);
        System.out.println("K numbers are " + Ks);
        System.out.println("Primes: " + found);
        
    }
}
