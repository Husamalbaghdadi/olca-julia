#include <cstdlib>
#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

// from https://github.com/PetterS/SuiteSparse/blob/master/UMFPACK/Include/umfpack.h
#define UMFPACK_A	(0) /* Ax=b    */

extern int umfpack_di_symbolic
(
    jint n_row,
    jint n_col,
    jint *Ap,
    jint *Ai,
    jdouble *Ax,
    void **Symbolic,
    double *Control,
    double *Info
);

extern int umfpack_di_numeric
(
    jint *Ap,
    jint *Ai,
    jdouble *Ax,
    void *Symbolic,
    void **Numeric,
    double *Control,
    double *Info
) ;

int umfpack_di_solve
(
    int sys,
    jint *Ap,
    jint *Ai,
    jdouble *Ax,
    jdouble *X,
    jdouble *B,
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
        umfpack_di_numeric(columnPointersPtr, rowIndicesPtr, valuesPtr, Symbolic, &Numeric, null, null) ;
        umfpack_di_free_symbolic(&Symbolic) ;
        umfpack_di_solve(UMFPACK_A, columnPointersPtr, rowIndicesPtr, valuesPtr, resultPtr, demandPtr, Numeric, null, null) ;
        umfpack_di_free_numeric(&Numeric);

        env->ReleaseIntArrayElements(columnPointers, columnPointersPtr, 0);
        env->ReleaseIntArrayElements(rowIndices, rowIndicesPtr, 0);
        env->ReleaseDoubleArrayElements(values, valuesPtr, 0);
        env->ReleaseDoubleArrayElements(demand, demandPtr, 0);
        env->ReleaseDoubleArrayElements(result, resultPtr, 0);
}

struct FactorizedMatrix {

    jintArray *columnPointers;
    jint *columnPointersPtr;

    jintArray *rowIndices;
    jint *rowIndicesPtr;

    jdoubleArray *values;
    jdouble *valuesPtr;

    void *Numeric;
};

JNIEXPORT void JNICALL Java_org_openlca_umfpack_Umfpack_factorize(
            JNIEnv *env, jclass jclazz, 
            jint n,
            jintArray columnPointers,
            jintArray rowIndices,
            jdoubleArray values) {

        struct FactorizedMatrix* fm = (FactorizedMatrix*)malloc(sizeof(struct FactorizedMatrix));
        fm->columnPointers = &columnPointers;
        fm->columnPointersPtr = env->GetIntArrayElements(columnPointers, NULL);

        fm->rowIndices = &rowIndices;
        fm->rowIndicesPtr = env->GetIntArrayElements(rowIndices, NULL);

        fm->values = &values; 
        fm->valuesPtr = env->GetDoubleArrayElements(values, NULL);

        double *null = (double *) NULL;
        void *Symbolic, *Numeric;

        umfpack_di_symbolic(
            n,
            n, 
            fm->columnPointersPtr,
            fm->rowIndicesPtr,
            fm->valuesPtr,
            &Symbolic,
            null, null);
        
        umfpack_di_numeric(
            fm->columnPointersPtr,
            fm->rowIndicesPtr,
            fm->valuesPtr,
            Symbolic,
            &Numeric,
            null, null);            
        umfpack_di_free_symbolic(&Symbolic);

        fm->Numeric = Numeric;
}

JNIEXPORT void JNICALL Java_org_openlca_umfpack_Umfpack_solveFactorized(
            JNIEnv *env, jclass jclazz, jlong pointer,
            jdoubleArray demand, jdoubleArray result) {
    
    jdouble *demandPtr = env->GetDoubleArrayElements(demand, NULL);
    jdouble *resultPtr = env->GetDoubleArrayElements(result, NULL);

    struct FactorizedMatrix* fm = (FactorizedMatrix*) pointer;

    double *null = (double *) NULL;
    umfpack_di_solve(
        UMFPACK_A,
        fm->columnPointersPtr,
        fm->rowIndicesPtr,
        fm->valuesPtr,
        resultPtr,
        demandPtr,
        fm->Numeric,
        null, null) ;

    env->ReleaseDoubleArrayElements(demand, demandPtr, 0);
    env->ReleaseDoubleArrayElements(result, resultPtr, 0);
}


JNIEXPORT void JNICALL Java_org_openlca_umfpack_Umfpack_dispose(
            JNIEnv *env, jclass jclazz, jlong pointer) {
    struct FactorizedMatrix* fm = (FactorizedMatrix*) pointer;
    env->ReleaseIntArrayElements(*(fm->columnPointers), fm->columnPointersPtr, 0);
    env->ReleaseIntArrayElements(*(fm->rowIndices), fm->rowIndicesPtr, 0);
    env->ReleaseDoubleArrayElements(*(fm->values), fm->valuesPtr, 0);
    umfpack_di_free_numeric(&(fm->Numeric));
    free(fm);
}

#ifdef __cplusplus
}
#endif