# olca-julia
This is a prototype to test the usage of the high-performance math libraries
of the [Julia](https://julialang.org) distribution packages in
[openLCA](https://github.com/GreenDelta/olca-app). Switching to these libraries
would mean that we do not have to collect, compile, and update optimized
versions of these libraries for all different platforms on our own (we
currently do this with [OpenBLAS](https://www.openblas.net/)) but that we
could just reuse the excellent work that is already done in the Julia project.

The idea would be to write a [JNI](https://en.wikipedia.org/wiki/Java_Native_Interface)
wrapper to call into the respective libraries from Java and distribute it
together with the libraries that have a compatible license directly with
openLCA. Other libraries with licenses that cannot be distributed with openLCA
(mostly because they are GPL licensed; like the current version of
[UMFPACK](https://people.sc.fsu.edu/~jburkardt/cpp_src/umfpack/umfpack.html))
could be copied by the user into the library folder. openLCA could then check
on startup which libraries are available and which methods can be called.

## Usage
Currently this prototype only contains bindings for UMFPACK. It expects that
the Julia libraries are located in the `./libs` folder within the project
directory. The `build.bat` script generates the JNI wrapper on Windows (other
platforms are currently not tested) and requires [MinGW](http://www.mingw.org/)
to be installed. Here is a Java example for using it:

```java
// load native libraries and JNI wrapper from the given folder
Umfpack.loadLibs("path/to/library/folder");

// factorize a technology matrix
UmfMatrix m = UmfMatrix.from(inventory.technologyMatrix);
UmfFactorizedMatrix fm = Umfpack.factorize(m);

// calculate the scaling vector for each process in the system
for (int i = 0; i < inventory.techIndex.size(); i++) {
    double[] demand = new double[techIndex.size()];
	demand[i] = 1.0;
	double[] s = Umfpack.solve(fm, demand);
}

// TODO: disposing the factorized matrix currently crashes the JVM
// fm.dispose();
```

This prototype is already used in a project to calculate the LCI results of
some unit process databases and the results look quite promisingly.
