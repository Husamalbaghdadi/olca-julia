package org.openlca.umfpack;

public class UmfFactorizedMatrix {
    final long pointer;

    UmfFactorizedMatrix(long pointer) {
        this.pointer = pointer;
    }

    public void dispose() {
        Umfpack.dispose(pointer);
    }

}