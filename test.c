#include <stdio.h>

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


int main() {
    printf("umfpack_di_symbolic!\n");


    int n = 5;
    int Ap [ ] = {0, 2, 5, 9, 10, 12};
    int Ai [ ] = { 0, 1, 0, 2, 4, 1, 2, 3, 4, 2, 1, 4};
    double Ax [ ] = {2., 3., 3., -1., 4., 4., -3., 1., 2., 2., 6., 1.};
    double b [ ] = {8., 45., -3., 3., 19.};
    double x [5];


    double *null = (double *) NULL;
    void *Symbolic, *Numeric;

    printf("umfpack_di_symbolic!\n");

    umfpack_di_symbolic(n, n, Ap, Ai, Ax, &Symbolic, null, null) ;
    umfpack_di_numeric (Ap, Ai, Ax, Symbolic, &Numeric, null, null) ;
    umfpack_di_free_symbolic (&Symbolic) ;
    umfpack_di_solve (UMFPACK_A, Ap, Ai, Ax, x, b, Numeric, null, null) ;
    umfpack_di_free_numeric (&Numeric) ;

for (int i = 0 ; i < n ; i++) printf ("x [%d] = %g\n", i, x [i]) ;

    printf("Works!\n");
    return 0;
}
