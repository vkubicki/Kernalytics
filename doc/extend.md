# How to extend Kernalytics

This chapter contains links to the various locations that must be modified to add a new data type or a new kernel.

## How to add a new data type

Adding a new data type is a bit involved, as it requires added a data container, but also a parser and a few other operations.

### Pattern-matching of the kernel name

Add its name as a case in the pattern matching of [parseIndividualVar](/src/main/scala/io/ReadVar.scala). Then call the proper parser to transform the raw data in `Array[String]` to a `ParsedVar` which is a named [DataRoot](/src/main/scala/rkhs/DataRoot.scala).

### DataRoot subtyping

Since the data type is new, it might be necessary to store it in a new type of container. The current containers used are Breeze container (such as `DenseMatrix[Real]`s), but anything could be used. Each variable is contained in an instance of a subclass of [DataRoot](/src/main/scala/rkhs/DataRoot.scala). The modification is to subtype [DataRoot](/src/main/scala/rkhs/DataRoot.scala). 

### Parser

Add the implementation of the parsing in a specific function in io.ReadVar, or in an external file, like [ParseVectorReal](/src/main/scala/io/ParseVectorReal.scala).

The parser must transform an `Array[String]` to a [DataRoot](/src/main/scala/rkhs/DataRoot.scala).

### KernelGenerator and algebraic system

In the file [KernelGenerator](/src/main/scala/rkhs/KernelGenerator.scala), have the pattern-matching point to the correct implementation in [AlgebraImplementation](/src/main/scala/rkhs/AlgebraImplementation.scala).

If the data type has inner product / norm / distance, implement them in [AlgebraImplementation](/src/main/scala/rkhs/AlgebraImplementation.scala), as this will allow families of kernels to be quickly generated for this kernel.

## How to add a new kernel on an existing data type

A kernel simply is a two arguments function from a couple of the X data type to Real. Its integration in Kernalytics in not very hard.

There are two ways to implement this function:
1. Directly as a (X, X) => Real function directly in [AlgebraImplementation](/src/main/scala/rkhs/AlgebraImplementation.scala), for a specific data type.
2. Indirectly in an algebraic object that can be used for any data type that implement this algebraic object. The implementation must then be made in [AlgebraAbstract](/src/main/scala/rkhs/AlgebraAbstract.scala). The algebraic system is discussed in more details in the [overview](overview.md).

## How to add a new numerical method

First, the new numerical method must be detected in the input files, so the right code is called. This is done in [callAlgo for Learn](/src/main/scala/exec/Learn.scala) and [callAlgo for Predict](/src/main/scala/exec/Predict.scala).

Then, the parameter parsing and launch code must be written in the [learn](/src/main/scala/exec/learn) and [predict](/src/main/scala/exec/predict) directories.

Finally, the code algorithm must be written in the [algo](/src/main/scala/algo) directory. This part must use the data and parameters that have been parsed in the preceding steps. It is the code of the numerical method.

## Notes

### Unit testing

Kernalytics implements unit testing via the ScalaTest library. Developers are strongly advised to add / run unit tests as often as possible. They are located in the [test](/src/test/scala) directory.
