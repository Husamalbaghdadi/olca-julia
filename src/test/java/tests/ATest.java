package tests;

import org.junit.Test;
import org.openlca.umfpack.Umfpack;

public class ATest {

    @Test
    public void test() {
        System.load("C:\\Users\\Besitzer\\Projects\\dev\\olca-umfpack\\libs\\olca-umfpack.dll");
        double[] x = new double[5];
        Umfpack.solve(
            5,
            new int[]{0, 2, 5, 9, 10, 12},
            new int[]{0, 1, 0, 2, 4, 1, 2, 3, 4, 2, 1, 4},
            new double[]{2., 3., 3., -1., 4., 4., -3., 1., 2., 2., 6., 1.},
            new double[]{8., 45., -3., 3., 19.},
            x);
        
        System.out.println("Works!");
        for (int i = 0; i < x.length; i++) {
            System.out.println(x[i]);
        }
    }
}
