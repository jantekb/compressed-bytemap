compressed-bytemap
==================

In-memory n×m map of bytes which is designed for modeling huge matrices that have many repeating numbers and thus can be effectively compressed using simple RLE algorithm.

It was designed and created for a particular Android application that was working on matrices of size 10^6.

Internally it uses an `int[][]` to store data. Each row in the matrix is an `int[]` itself. Rows consist of blocks represented by a single `int` in the array. The least significant 8 bits store the value of the block, the most significant 24 bits of the `int` store the length of the homogeneous block.

Example:

Assume the following row: `0000011111`. It is represented as `{ { 1280, 1281 } }` because we have only one row and that the value 1280 decimal is equal to `10100000000` binary, where the least significant 8 bits store the 0 value, and the most significant 3 (actually padded to 24 bits with zeros on the left) store the length of 5.

Example usage is:

    Bytemap world = new Bytemap(20, 10);
	System.out.println(world.toString()); // will print character representation and estimated memory usage
	world.setRow(0, "00000111112222233333");  
	System.out.println(world.toString());
	world.setValue(5, 0, 10, 7);
	System.out.println(world.toString()); // first row should be like 00000777777777733333
	
The map supports setting the value explicitly on a row (`setValue(..)`) and (`addValue(..)`) which can increment or decrement numbers in a row. I hope my unit tests will make it obvious.