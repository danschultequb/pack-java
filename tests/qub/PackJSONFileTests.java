package qub;

public interface PackJSONFileTests
{
    static void test(TestRunner runner)
    {
        PreCondition.assertNotNull(runner, "runner");

        runner.testGroup(PackJSONFile.class, () ->
        {
            runner.test("constructor()", (Test test) ->
            {
                final PackJSONFile file = new PackJSONFile();
                test.assertNull(file.getRelativePath());
                test.assertNull(file.getLastModified());
            });

            runner.testGroup("setRelativePath(String)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final PackJSONFile file = new PackJSONFile();
                    test.assertThrows(() -> file.setRelativePath((String)null),
                        new PreConditionFailure("relativePath cannot be null."));
                    test.assertNull(file.getRelativePath());
                });

                runner.test("with empty", (Test test) ->
                {
                    final PackJSONFile file = new PackJSONFile();
                    test.assertThrows(() -> file.setRelativePath(""),
                        new PreConditionFailure("relativePath cannot be empty."));
                    test.assertNull(file.getRelativePath());
                });

                runner.test("with rooted path", (Test test) ->
                {
                    final PackJSONFile file = new PackJSONFile();
                    test.assertThrows(() -> file.setRelativePath("/hello/there.class"),
                        new PreConditionFailure("relativePath.isRooted() cannot be true."));
                    test.assertNull(file.getRelativePath());
                });

                runner.test("with relative path", (Test test) ->
                {
                    final PackJSONFile file = new PackJSONFile();
                    test.assertSame(file, file.setRelativePath("hello/there.class"));
                    test.assertEqual(Path.parse("hello/there.class"), file.getRelativePath());
                });
            });

            runner.testGroup("setRelativePath(Path)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final PackJSONFile file = new PackJSONFile();
                    test.assertThrows(() -> file.setRelativePath((Path)null),
                        new PreConditionFailure("relativePath cannot be null."));
                    test.assertNull(file.getRelativePath());
                });

                runner.test("with rooted path", (Test test) ->
                {
                    final PackJSONFile file = new PackJSONFile();
                    test.assertThrows(() -> file.setRelativePath(Path.parse("/hello/there.class")),
                        new PreConditionFailure("relativePath.isRooted() cannot be true."));
                    test.assertNull(file.getRelativePath());
                });

                runner.test("with relative path", (Test test) ->
                {
                    final PackJSONFile file = new PackJSONFile();
                    test.assertSame(file, file.setRelativePath(Path.parse("hello/there.class")));
                    test.assertEqual(Path.parse("hello/there.class"), file.getRelativePath());
                });
            });

            runner.testGroup("setLastModified(DateTime)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final PackJSONFile file = new PackJSONFile();
                    test.assertThrows(() -> file.setLastModified(null),
                        new PreConditionFailure("lastModified cannot be null."));
                    test.assertNull(file.getLastModified());
                });

                runner.test("with non-null", (Test test) ->
                {
                    final PackJSONFile file = new PackJSONFile();
                    final DateTime now = test.getClock().getCurrentDateTime();
                    test.assertSame(file, file.setLastModified(now));
                    test.assertEqual(now, file.getLastModified());
                });
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
                    new PackJSONFile(),
                    null,
                    false);
                equalsTest.run(
                    new PackJSONFile(),
                    "apples",
                    false);
                equalsTest.run(
                    new PackJSONFile(),
                    new PackJSONFile(),
                    true);
                equalsTest.run(
                    new PackJSONFile()
                        .setRelativePath("hello.class"),
                    new PackJSONFile(),
                    false);
                equalsTest.run(
                    new PackJSONFile()
                        .setRelativePath("hello.class"),
                    new PackJSONFile()
                        .setRelativePath("hello/there.class"),
                    false);
                equalsTest.run(
                    new PackJSONFile()
                        .setRelativePath("hello/there.class"),
                    new PackJSONFile()
                        .setRelativePath("hello/there.class"),
                    true);
                equalsTest.run(
                    new PackJSONFile()
                        .setRelativePath("hello\\there.class"),
                    new PackJSONFile()
                        .setRelativePath("hello/there.class"),
                    true);
                equalsTest.run(
                    new PackJSONFile()
                        .setRelativePath("hello/there.class")
                        .setLastModified(DateTime.create(1, 2, 3)),
                    new PackJSONFile()
                        .setRelativePath("hello/there.class"),
                    false);
                equalsTest.run(
                    new PackJSONFile()
                        .setRelativePath("hello/there.class")
                        .setLastModified(DateTime.create(1, 2, 3)),
                    new PackJSONFile()
                        .setRelativePath("hello/there.class")
                        .setLastModified(DateTime.create(1, 2, 4)),
                    false);
                equalsTest.run(
                    new PackJSONFile()
                        .setRelativePath("hello/there.class")
                        .setLastModified(DateTime.create(1, 2, 3)),
                    new PackJSONFile()
                        .setRelativePath("hello/there.class")
                        .setLastModified(DateTime.create(1, 2, 3)),
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
                    new PackJSONFile(),
                    null,
                    false);
                equalsTest.run(
                    new PackJSONFile(),
                    new PackJSONFile(),
                    true);
                equalsTest.run(
                    new PackJSONFile()
                        .setRelativePath("hello.class"),
                    new PackJSONFile(),
                    false);
                equalsTest.run(
                    new PackJSONFile()
                        .setRelativePath("hello.class"),
                    new PackJSONFile()
                        .setRelativePath("hello/there.class"),
                    false);
                equalsTest.run(
                    new PackJSONFile()
                        .setRelativePath("hello/there.class"),
                    new PackJSONFile()
                        .setRelativePath("hello/there.class"),
                    true);
                equalsTest.run(
                    new PackJSONFile()
                        .setRelativePath("hello\\there.class"),
                    new PackJSONFile()
                        .setRelativePath("hello/there.class"),
                    true);
                equalsTest.run(
                    new PackJSONFile()
                        .setRelativePath("hello/there.class")
                        .setLastModified(DateTime.create(1, 2, 3)),
                    new PackJSONFile()
                        .setRelativePath("hello/there.class"),
                    false);
                equalsTest.run(
                    new PackJSONFile()
                        .setRelativePath("hello/there.class")
                        .setLastModified(DateTime.create(1, 2, 3)),
                    new PackJSONFile()
                        .setRelativePath("hello/there.class")
                        .setLastModified(DateTime.create(1, 2, 4)),
                    false);
                equalsTest.run(
                    new PackJSONFile()
                        .setRelativePath("hello/there.class")
                        .setLastModified(DateTime.create(1, 2, 3)),
                    new PackJSONFile()
                        .setRelativePath("hello/there.class")
                        .setLastModified(DateTime.create(1, 2, 3)),
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
                    new PackJSONFile(),
                    "null:null");
                toStringTest.run(
                    new PackJSONFile()
                        .setRelativePath("a/b/c.java"),
                    "\"a/b/c.java\":null");
                toStringTest.run(
                    new PackJSONFile()
                        .setLastModified(DateTime.create(1, 2, 3)),
                    "null:\"0001-02-03T00:00Z\"");
                toStringTest.run(
                    new PackJSONFile()
                        .setRelativePath("grapes.java")
                        .setLastModified(DateTime.create(1, 2, 3)),
                    "\"grapes.java\":\"0001-02-03T00:00Z\"");
            });

            runner.testGroup("toJsonProperty()", () ->
            {
                runner.test("with null relative path", (Test test) ->
                {
                    final PackJSONFile file = new PackJSONFile();
                    test.assertThrows(file::toJsonProperty,
                        new PreConditionFailure("this.getRelativePath() cannot be null."));
                });

                runner.test("with null last modified", (Test test) ->
                {
                    final PackJSONFile file = new PackJSONFile()
                        .setRelativePath("a/b.java");
                    test.assertEqual(
                        JSONProperty.create("a/b.java", JSONNull.segment),
                        file.toJsonProperty());
                });

                runner.test("with all properties", (Test test) ->
                {
                    final PackJSONFile file = new PackJSONFile()
                        .setRelativePath("a/b.java")
                        .setLastModified(DateTime.create(1, 2, 3));
                    test.assertEqual(
                        JSONProperty.create("a/b.java", "0001-02-03T00:00Z"),
                        file.toJsonProperty());
                });
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

                parseErrorTest.run(null, new PreConditionFailure("property cannot be null."));
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
                    new PackJSONFile()
                        .setRelativePath("a")
                        .setLastModified(DateTime.create(1, 2, 3)));
            });
        });
    }
}
