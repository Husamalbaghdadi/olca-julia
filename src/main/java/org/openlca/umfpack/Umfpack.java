package org.openlca.umfpack;

import java.io.File;

public class Umfpack {

    private static boolean loaded;

    public static native void solve(
        int n,
        int[] columnPointers,
        int[] rowIndices,
        double[] values,
        double[] demand,
        double[] result);

    public static double[] solve(UmfMatrix m, double[] demand) {
        double[] result = new double[demand.length];
        solve(m.rowCount, 
            m.columnPointers,
            m.rowIndices,
            m.values,
            demand,
            result);
        return result;
    }

    public static native long factorize(
        int n,
        int[] columnPointers,
        int[] rowIndices,
        double[] values
    );

    public static UmfFactorizedMatrix factorize(UmfMatrix m) {
        long pointer = factorize(
            m.rowCount, 
            m.columnPointers,
            m.rowIndices,
            m.values);
        return new UmfFactorizedMatrix(pointer);
    }

    public static native void dispose(long pointer);

    public static native long solveFactorized(
        long pointer, double[] demand, double[] result);
    
    public static double[] solve(UmfFactorizedMatrix m, double[] demand) {
        double[] result = new double[demand.length];
        solveFactorized(m.pointer, demand, result);
        return result;
    }
    
    /**
     * Loads the native library from the given path. Does nothing when the
     * library was already loaded.
     */
    public static void load(String pathToLib) {
        if (loaded)
            return;
        File f = new File(pathToLib);
        System.load(f.getAbsolutePath());
        // TODO: test it
        loaded = true;
    }
}
