package org.openlca.umfpack;

public class Umfpack {

    public static native void solve(
        int n,
        int[] columnPointers,
        int[] rowIndices,
        double[] values,
        double[] demand,
        double[] result);

}
