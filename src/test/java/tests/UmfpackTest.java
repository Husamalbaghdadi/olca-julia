package tests;

import static org.junit.Assert.assertArrayEquals;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openlca.core.matrix.format.HashMatrix;
import org.openlca.umfpack.UmfMatrix;
import org.openlca.umfpack.Umfpack;

public class UmfpackTest {

    @BeforeClass
    public static void setUp() {
        Umfpack.load("libs/olca-umfpack.dll");
    }

    @Test
    public void testSolveNative() {
        double[] x = new double[5];
        Umfpack.solve(5, 
            new int[] { 0, 2, 5, 9, 10, 12 }, 
            new int[] { 0, 1, 0, 2, 4, 1, 2, 3, 4, 2, 1, 4 },
            new double[] { 2., 3., 3., -1., 4., 4., -3., 1., 2., 2., 6., 1. },
            new double[] { 8., 45., -3., 3., 19. },
        x);
        assertArrayEquals(
            new double[] {1d, 2d, 3d, 4d, 5d }, x, 1e-8);
    }

    @Test
    public void testSolveMatrix() {
        HashMatrix m = new HashMatrix(new double[][] { 
            { 2.0, 3.0, 0.0, 0.0, 0.0 },
            { 3.0, 0.0, 4.0, 0.0, 6.0 },
            { 0.0, -1.0, -3.0, 2.0, 0.0 },
            { 0.0, 0.0, 1.0, 0.0, 0.0 },
            { 0.0, 4.0, 2.0, 0.0, 1.0 } });
        UmfMatrix uMatrix = UmfMatrix.from(m);
        double[] demand = { 8., 45., -3., 3., 19. };
        double[] x = Umfpack.solve(uMatrix, demand);
        assertArrayEquals(
            new double[] {1d, 2d, 3d, 4d, 5d }, x, 1e-8);
    }
}
