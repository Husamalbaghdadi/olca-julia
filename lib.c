#include <stdio.h>
#include <jni.h>

// from https://github.com/PetterS/SuiteSparse/blob/master/UMFPACK/Include/umfpack.h
#define UMFPACK_A	(0) /* Ax=b    */

extern int umfpack_di_symbolic
(
    int n_row,
    int n_col,
    const int Ap [],
    const int Ai [],
    const double Ax [],
    void **Symbolic,
    double *Control,
    double *Info
);

extern int umfpack_di_numeric
(
    const int Ap [],
    const int Ai [],
    const double Ax [],
    void *Symbolic,
    void **Numeric,
    double *Control,
    double *Info
) ;

int umfpack_di_solve
(
    int sys,
    const int Ap [],
    const int Ai [],
    const double Ax [],
    double X [],
    const double B [],
    void *Numeric,
     double *Control,
    double *Info
) ;

extern void umfpack_di_free_symbolic(void **Symbolic);

extern void umfpack_di_free_numeric(void **Numeric);


 JNIEXPORT void JNICALL Java_org_openlca_umfpack_Umfpack_solve(
            JNIEnv *env, jclass jclazz, 
            jint n,
            jintArray columnPointers,
            jintArray rowIndices,
            jdoubleArray values,
            jdoubleArray demand,
            jdoubleArray result) {

        jint *columnPointersPtr = env->GetIntArrayElements(columnPointers, NULL);
        jint *rowIndicesPtr = env->GetIntArrayElements(rowIndices, NULL);
        jdouble *valuesPtr = env->GetDoubleArrayElements(values, NULL);
        jdouble *demandPtr = env->GetDoubleArrayElements(demand, NULL);
        jdouble *resultPtr = env->GetDoubleArrayElements(result, NULL);

        double *null = (double *) NULL;
        void *Symbolic, *Numeric;

        umfpack_di_symbolic(n, n, columnPointersPtr, rowIndicesPtr, valuesPtr, &Symbolic, null, null) ;
        umfpack_di_numeric (columnPointersPtr, rowIndicesPtr, valuesPtr, Symbolic, &Numeric, null, null) ;
        umfpack_di_free_symbolic (&Symbolic) ;
        umfpack_di_solve (UMFPACK_A, columnPointersPtr, rowIndicesPtr, valuesPtr, resultPtr, demandPtr, Numeric, null, null) ;
        umfpack_di_free_numeric (&Numeric) ;

        env->ReleaseIntArrayElements(columnPointers, columnPointersPtr, 0);
        env->ReleaseIntArrayElements(rowIndices, rowIndicesPtr, 0);
        env->ReleaseDoubleArrayElements(values, valuesPtr, 0);
        env->ReleaseDoubleArrayElements(demand, demandPtr, 0);
        env->ReleaseDoubleArrayElements(result, resultPtr, 0);
}
