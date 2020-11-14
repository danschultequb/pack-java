package qub;

public interface PackJSONFileTests
{
    static void test(TestRunner runner)
    {
        PreCondition.assertNotNull(runner, "runner");

        runner.testGroup(PackJSONFile.class, () ->
        {
            runner.testGroup("create(String,DateTime)", () ->
            {
                final Action3<String,DateTime,Throwable> createErrorTest = (String relativePath, DateTime lastModified, Throwable expected) ->
                {
                    runner.test("with " + English.andList(Strings.escapeAndQuote(relativePath), lastModified), (Test test) ->
                    {
                        test.assertThrows(() -> PackJSONFile.create(relativePath, lastModified), expected);
                    });
                };

                createErrorTest.run(null, DateTime.epoch, new PreConditionFailure("relativePath cannot be null."));
                createErrorTest.run("", DateTime.epoch, new PreConditionFailure("relativePath cannot be empty."));
                createErrorTest.run("/rooted/path.txt", DateTime.epoch, new PreConditionFailure("relativePath.isRooted() cannot be true."));
                createErrorTest.run("relative/path.txt", null, new PreConditionFailure("lastModified cannot be null."));

                final Action2<String,DateTime> createTest = (String relativePath, DateTime lastModified) ->
                {
                    runner.test("with " + English.andList(Strings.escapeAndQuote(relativePath), lastModified), (Test test) ->
                    {
                        final PackJSONFile packJsonFile = PackJSONFile.create(relativePath, lastModified);
                        test.assertNotNull(packJsonFile);
                        test.assertEqual(Path.parse(relativePath), packJsonFile.getRelativePath());
                        test.assertEqual(lastModified, packJsonFile.getLastModified());

                        final JSONProperty json = packJsonFile.toJsonProperty();
                        test.assertNotNull(json, "json");
                        test.assertEqual(relativePath, json.getName());
                        test.assertEqual(JSONString.get(lastModified.toString()), json.getValue());
                    });
                };

                createTest.run("relative/path.txt", DateTime.create(1, 2, 3));
            });

            runner.testGroup("create(Path,DateTime)", () ->
            {
                final Action3<Path,DateTime,Throwable> createErrorTest = (Path relativePath, DateTime lastModified, Throwable expected) ->
                {
                    runner.test("with " + English.andList(Strings.escapeAndQuote(relativePath), lastModified), (Test test) ->
                    {
                        test.assertThrows(() -> PackJSONFile.create(relativePath, lastModified), expected);
                    });
                };

                createErrorTest.run(null, DateTime.epoch, new PreConditionFailure("relativePath cannot be null."));
                createErrorTest.run(Path.parse("/rooted/path.txt"), DateTime.epoch, new PreConditionFailure("relativePath.isRooted() cannot be true."));
                createErrorTest.run(Path.parse("relative/path.txt"), null, new PreConditionFailure("lastModified cannot be null."));

                final Action2<Path,DateTime> createTest = (Path relativePath, DateTime lastModified) ->
                {
                    runner.test("with " + English.andList(Strings.escapeAndQuote(relativePath), lastModified), (Test test) ->
                    {
                        final PackJSONFile packJsonFile = PackJSONFile.create(relativePath, lastModified);
                        test.assertNotNull(packJsonFile);
                        test.assertEqual(relativePath, packJsonFile.getRelativePath());
                        test.assertEqual(lastModified, packJsonFile.getLastModified());

                        final JSONProperty json = packJsonFile.toJsonProperty();
                        test.assertNotNull(json, "json");
                        test.assertEqual(relativePath.toString(), json.getName());
                        test.assertEqual(JSONString.get(lastModified.toString()), json.getValue());
                    });
                };

                createTest.run(Path.parse("relative/path.txt"), DateTime.create(1, 2, 3));
            });

            runner.testGroup("equals(Object)", () ->
            {
                final Action3<PackJSONFile,Object,Boolean> equalsTest = (PackJSONFile file, Object rhs, Boolean expected) ->
                {
                    runner.test("with " + file + " and " + rhs, (Test test) ->
                    {
                        test.assertEqual(expected, file.equals(rhs));
                    });
                };

                equalsTest.run(
                    PackJSONFile.create("test", DateTime.epoch),
                    null,
                    false);
                equalsTest.run(
                    PackJSONFile.create("test", DateTime.epoch),
                    "apples",
                    false);
                equalsTest.run(
                    PackJSONFile.create("test", DateTime.epoch),
                    PackJSONFile.create("test", DateTime.epoch),
                    true);
                equalsTest.run(
                    PackJSONFile.create("hello.class", DateTime.epoch),
                    PackJSONFile.create("test", DateTime.epoch),
                    false);
                equalsTest.run(
                    PackJSONFile.create("hello.class", DateTime.epoch),
                    PackJSONFile.create("hello/there.class", DateTime.epoch),
                    false);
                equalsTest.run(
                    PackJSONFile.create("hello/there.class", DateTime.epoch),
                    PackJSONFile.create("hello/there.class", DateTime.epoch),
                    true);
                equalsTest.run(
                    PackJSONFile.create("hello\\there.class", DateTime.epoch),
                    PackJSONFile.create("hello/there.class", DateTime.epoch),
                    true);
                equalsTest.run(
                    PackJSONFile.create("hello/there.class", DateTime.create(1, 2, 3)),
                    PackJSONFile.create("hello/there.class", DateTime.epoch),
                    false);
                equalsTest.run(
                    PackJSONFile.create("hello/there.class", DateTime.create(1, 2, 3)),
                    PackJSONFile.create("hello/there.class", DateTime.create(1, 2, 4)),
                    false);
                equalsTest.run(
                    PackJSONFile.create("hello/there.class", DateTime.create(1, 2, 3)),
                    PackJSONFile.create("hello/there.class", DateTime.create(1, 2, 3)),
                    true);
            });

            runner.testGroup("equals(PackJSONFile)", () ->
            {
                final Action3<PackJSONFile,PackJSONFile,Boolean> equalsTest = (PackJSONFile file, PackJSONFile rhs, Boolean expected) ->
                {
                    runner.test("with " + file + " and " + rhs, (Test test) ->
                    {
                        test.assertEqual(expected, file.equals(rhs));
                    });
                };

                equalsTest.run(
                    PackJSONFile.create("test", DateTime.epoch),
                    null,
                    false);
                equalsTest.run(
                    PackJSONFile.create("test", DateTime.epoch),
                    PackJSONFile.create("test", DateTime.epoch),
                    true);
                equalsTest.run(
                    PackJSONFile.create("hello.class", DateTime.epoch),
                    PackJSONFile.create("test", DateTime.epoch),
                    false);
                equalsTest.run(
                    PackJSONFile.create("hello.class", DateTime.epoch),
                    PackJSONFile.create("hello/there.class", DateTime.epoch),
                    false);
                equalsTest.run(
                    PackJSONFile.create("hello/there.class", DateTime.epoch),
                    PackJSONFile.create("hello/there.class", DateTime.epoch),
                    true);
                equalsTest.run(
                    PackJSONFile.create("hello\\there.class", DateTime.epoch),
                    PackJSONFile.create("hello/there.class", DateTime.epoch),
                    true);
                equalsTest.run(
                    PackJSONFile.create("hello/there.class", DateTime.create(1, 2, 3)),
                    PackJSONFile.create("hello/there.class", DateTime.epoch),
                    false);
                equalsTest.run(
                    PackJSONFile.create("hello/there.class", DateTime.create(1, 2, 3)),
                    PackJSONFile.create("hello/there.class", DateTime.create(1, 2, 4)),
                    false);
                equalsTest.run(
                    PackJSONFile.create("hello/there.class", DateTime.create(1, 2, 3)),
                    PackJSONFile.create("hello/there.class", DateTime.create(1, 2, 3)),
                    true);
            });

            runner.testGroup("toString()", () ->
            {
                final Action2<PackJSONFile,String> toStringTest = (PackJSONFile file, String expected) ->
                {
                    runner.test("with " + file, (Test test) ->
                    {
                        test.assertEqual(expected, file.toString());
                    });
                };

                toStringTest.run(
                    PackJSONFile.create("test", DateTime.epoch),
                    "\"test\":\"1970-01-01T00:00Z\"");
                toStringTest.run(
                    PackJSONFile.create("a/b/c.java", DateTime.epoch),
                    "\"a/b/c.java\":\"1970-01-01T00:00Z\"");
                toStringTest.run(
                    PackJSONFile.create("test", DateTime.create(1, 2, 3)),
                    "\"test\":\"0001-02-03T00:00Z\"");
                toStringTest.run(
                    PackJSONFile.create("grapes.java", DateTime.create(1, 2, 3)),
                    "\"grapes.java\":\"0001-02-03T00:00Z\"");
            });

            runner.test("toJsonProperty()", (Test test) ->
            {
                final PackJSONFile file = PackJSONFile.create("a/b.java", DateTime.create(1, 2, 3));
                test.assertEqual(
                    JSONProperty.create("a/b.java", "0001-02-03T00:00Z"),
                    file.toJsonProperty());
            });

            runner.testGroup("parse(JSONProperty)", () ->
            {
                final Action2<JSONProperty,Throwable> parseErrorTest = (JSONProperty jsonProperty, Throwable expected) ->
                {
                    runner.test("with " + jsonProperty, (Test test) ->
                    {
                        test.assertThrows(() -> PackJSONFile.parse(jsonProperty).await(), expected);
                    });
                };

                parseErrorTest.run(null, new PreConditionFailure("json cannot be null."));
                parseErrorTest.run(JSONProperty.create("a", JSONNull.segment), new WrongTypeException("Expected the property named \"a\" to be a JSONString, but was a JSONNull instead."));
                parseErrorTest.run(JSONProperty.create("a", "b"), new java.time.format.DateTimeParseException("Text 'b' could not be parsed at index 0", "b", 0));

                final Action2<JSONProperty,PackJSONFile> parseTest = (JSONProperty jsonProperty, PackJSONFile expected) ->
                {
                    runner.test("with " + jsonProperty, (Test test) ->
                    {
                        test.assertEqual(expected, PackJSONFile.parse(jsonProperty).await());
                    });
                };

                parseTest.run(
                    JSONProperty.create("a", "0001-02-03T00:00Z"),
                    PackJSONFile.create("a", DateTime.create(1, 2, 3)));
            });
        });
    }
}
