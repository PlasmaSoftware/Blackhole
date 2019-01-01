# Blackhole
A decorator framework for the JVM. Note: This is currently more of a proof-of-concept than production-ready library.

## Basic Overview
Blackhole is inspired by [Python decorators](https://en.wikipedia.org/wiki/Python_syntax_and_semantics#Decorators)
but for Java. This framework allows users to define annotations which can be used to modify the behavior of
classes and methods. It takes advantage of annotation processing at compile-time in order to reduce runtime
overhead as much as possible.

## How it works
First, create a decorator driver. These must either extend `ClassDecoratorDriver` or `MethodDecoratorDriver` 
(and optionally, a compile-time hook extending the `CompileTimeHook` class). Next, create your annotation.
These can only be used to annotated classes or methods and they must be annotated with either `@ClassDecorator`
or `@MethodDecorator` (depending on the driver type you've implemented). 

Now, on compilation, the Blackhole annotation processor will scan for decorators and, if found, generates
a new annotation processor class for each corresponding decorator which extends `AbstractDecoratorImplProcessor`.

Finally, when a user uses your decorator, the newly generated processor scans for your annotation and generates
a new class which extends the original class with a buttload of hooks as well as registering the new type in an
internal registry. Now, to access the decorated class you just use either `Blackhole.decorate(<class>)` or 
`Blackhole.constructors(<class>)`.

## Limitations
* Annotation processing can slow down the compilation process.
* Annotation processing prevents the direct access of your classes by our processors so Blackhole employs a
primitive java compiler to bootstrap an instance into the classpath in order to call it. This means that the
use of too many not-yet-compiled classes can lead to compilation speed degradation or simple compilation errors.
* Blackhole's technique does not modify the original class. This can lead to potential unintended side effects

## TODO

