# Colloquy - Digital Humanities Group
A visualization of Tolstoy's intellectual world

This is currently a collection of java files that is a bit disorganized but working nevertheless.

If you are curious you may load that project in IntellJ or another IDE of your choice. All dependencies are in pom.xml.
There are sample files from tolstoy.ru that are necessary to demonstrate how the system works.
If you want to load this resources into your own elastic cluster or to further modify this program to transform data in the way that fits your need please load EPUB files that you need from the source tolstoy.ru following their instructions and guidelines.

Start with TestLetterParser and follow code to better understand the logic and the challenges that we faced in converting underlying xml structures in EPUB files to JSON format suitable for storage, indexing, and, eventually, visualization.

You wold need to run decompressEPUBFiles() method (make sure to modify the path to your directory structure).
Then you can run parseDocuments() method.




