# What to do if the LadybugTests fail? 
### How does it work

The LadybugTests check if for an defined incoming request, the same response is created.
Sometimes there are changes that make that this is wanted behaviour, so the validation will fail.
Ladybug compares the result of Open Zaakbrug with the expected results, theset tests are stored in the src/test/resources/ladybug/*.xml files

### Error message

When such a test fails we typically this will be an error like:

```
org.opentest4j.AssertionFailedError: expected: <<Report><Checkpoint Name="....
```

### Perform by hand

It is possible to perform the test also from within Open Zaakbrug bij navigating to the url: http://localhost:8080/debug
Then the following steps have to be performed:

1. Goto the "Test"-tab
2. Press the "Run"-button
3. Keep pressing the "Refresh"-button untill all the tests are done (green=ok, red=differences)
4. Press the "Compare"-button to look in the file to detect the differences (in red)

### Updating the test

By pressing the "Replace"-button, the file at the location src/test/resources/ladybug/ will be replaced with the results that were received from Open Zaakbrug, this is also the way to change tests.

### Ignoring certain values

Not all values will be compared in the check, certain values are ignored. 
In the "Compare"-tab they can be recognized by the "IGNORED" value.
They are used for values that vary, for example the time/unique-numers/etc.
To add a new value that has to be ignored, you have to change the file: src/main/resources/transform-ladybug-report.xslt