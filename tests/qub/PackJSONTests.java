package qub;

public interface PackJSONTests
{
    static void test(TestRunner runner)
    {
        PreCondition.assertNotNull(runner, "runner");

        runner.testGroup(PackJSON.class, () ->
        {
            runner.test("constructor()", (Test test) ->
            {
                final PackJSON packJson = new PackJSON();
                test.assertNull(packJson.getSourceFiles());
                test.assertNull(packJson.getSourceOutputFiles());
                test.assertNull(packJson.getTestOutputFiles());
            });

            runner.testGroup("setSourceFiles(Iterable<PackJSONFile>)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final PackJSON packJson = new PackJSON();
                    test.assertThrows(() -> packJson.setSourceFiles(null),
                        new PreConditionFailure("sourceFiles cannot be null."));
                    test.assertNull(packJson.getSourceFiles());
                });

                runner.test("with empty", (Test test) ->
                {
                    final PackJSON packJson = new PackJSON();
                    final Iterable<PackJSONFile> files = Iterable.create();
                    test.assertSame(packJson, packJson.setSourceFiles(files));
                    test.assertEqual(files, packJson.getSourceFiles());
                });

                runner.test("with non-empty", (Test test) ->
                {
                    final PackJSON packJson = new PackJSON();
                    final Iterable<PackJSONFile> files = Iterable.create(
                        new PackJSONFile()
                            .setRelativePath("hello.java"));
                    test.assertSame(packJson, packJson.setSourceFiles(files));
                    test.assertEqual(files, packJson.getSourceFiles());
                });
            });

            runner.testGroup("setSourceOutputFiles(Iterable<PackJSONFile>)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final PackJSON packJson = new PackJSON();
                    test.assertThrows(() -> packJson.setSourceOutputFiles(null),
                        new PreConditionFailure("sourceOutputFiles cannot be null."));
                    test.assertNull(packJson.getSourceOutputFiles());
                });

                runner.test("with empty", (Test test) ->
                {
                    final PackJSON packJson = new PackJSON();
                    final Iterable<PackJSONFile> files = Iterable.create();
                    test.assertSame(packJson, packJson.setSourceOutputFiles(files));
                    test.assertEqual(files, packJson.getSourceOutputFiles());
                });

                runner.test("with non-empty", (Test test) ->
                {
                    final PackJSON packJson = new PackJSON();
                    final Iterable<PackJSONFile> files = Iterable.create(
                        new PackJSONFile()
                            .setRelativePath("hello.java"));
                    test.assertSame(packJson, packJson.setSourceOutputFiles(files));
                    test.assertEqual(files, packJson.getSourceOutputFiles());
                });
            });

            runner.testGroup("setTestOutputFiles(Iterable<PackJSONFile>)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final PackJSON packJson = new PackJSON();
                    test.assertThrows(() -> packJson.setTestOutputFiles(null),
                        new PreConditionFailure("testOutputFiles cannot be null."));
                    test.assertNull(packJson.getTestOutputFiles());
                });

                runner.test("with empty", (Test test) ->
                {
                    final PackJSON packJson = new PackJSON();
                    final Iterable<PackJSONFile> files = Iterable.create();
                    test.assertSame(packJson, packJson.setTestOutputFiles(files));
                    test.assertEqual(files, packJson.getTestOutputFiles());
                });

                runner.test("with non-empty", (Test test) ->
                {
                    final PackJSON packJson = new PackJSON();
                    final Iterable<PackJSONFile> files = Iterable.create(
                        new PackJSONFile()
                            .setRelativePath("hello.java"));
                    test.assertSame(packJson, packJson.setTestOutputFiles(files));
                    test.assertEqual(files, packJson.getTestOutputFiles());
                });
            });

            runner.testGroup("toString()", () ->
            {
                final Action2<PackJSON,String> toStringTest = (PackJSON packJson, String expected) ->
                {
                    runner.test("with " + packJson, (Test test) ->
                    {
                        test.assertEqual(expected, packJson.toString());
                    });
                };

                toStringTest.run(
                    new PackJSON(),
                    "{}");
                toStringTest.run(
                    new PackJSON()
                        .setSourceFiles(Iterable.create()),
                    "{}");
                toStringTest.run(
                    new PackJSON()
                        .setSourceFiles(Iterable.create(
                            new PackJSONFile()
                                .setRelativePath("a/b.java")
                                .setLastModified(DateTime.create(1, 2, 3)))),
                    "{\"sourceFiles\":{\"a/b.java\":\"0001-02-03T00:00Z\"}}");
                toStringTest.run(
                    new PackJSON()
                        .setSourceOutputFiles(Iterable.create()),
                    "{}");
                toStringTest.run(
                    new PackJSON()
                        .setSourceOutputFiles(Iterable.create(
                            new PackJSONFile()
                                .setRelativePath("a/b.java")
                                .setLastModified(DateTime.create(1, 2, 3)))),
                    "{\"sourceOutputFiles\":{\"a/b.java\":\"0001-02-03T00:00Z\"}}");
                toStringTest.run(
                    new PackJSON()
                        .setTestOutputFiles(Iterable.create()),
                    "{}");
                toStringTest.run(
                    new PackJSON()
                        .setTestOutputFiles(Iterable.create(
                            new PackJSONFile()
                                .setRelativePath("a/b.java")
                                .setLastModified(DateTime.create(1, 2, 3)))),
                    "{\"testOutputFiles\":{\"a/b.java\":\"0001-02-03T00:00Z\"}}");
                toStringTest.run(
                    new PackJSON()
                        .setSourceFiles(Iterable.create(
                            new PackJSONFile()
                                .setRelativePath("a/b.java")
                                .setLastModified(DateTime.create(1, 2, 3))))
                        .setSourceOutputFiles(Iterable.create(
                            new PackJSONFile()
                                .setRelativePath("c/d/e.class")
                                .setLastModified(DateTime.create(4, 5, 6))))
                        .setTestOutputFiles(Iterable.create(
                            new PackJSONFile()
                                .setRelativePath("f.class")
                                .setLastModified(DateTime.create(7, 8, 9)))),
                    "{\"sourceFiles\":{\"a/b.java\":\"0001-02-03T00:00Z\"},\"sourceOutputFiles\":{\"c/d/e.class\":\"0004-05-06T00:00Z\"},\"testOutputFiles\":{\"f.class\":\"0007-08-09T00:00Z\"}}");
            });

            runner.testGroup("parse(JSONObject)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> PackJSON.parse(null),
                        new PreConditionFailure("json cannot be null."));
                });

                final Action2<JSONObject,PackJSON> parseTest = (JSONObject json, PackJSON expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(json), (Test test) ->
                    {
                        test.assertEqual(expected, PackJSON.parse(json).await());
                    });
                };

                parseTest.run(
                    JSONObject.create(),
                    new PackJSON());
                parseTest.run(
                    JSONObject.create()
                        .setObject("sourceFiles", JSONObject.create()),
                    new PackJSON()
                        .setSourceFiles(Iterable.create()));
                parseTest.run(
                    JSONObject.create()
                        .setObject("sourceFiles", JSONObject.create()
                            .setString("a/b.java", "hello")),
                    new PackJSON()
                        .setSourceFiles(Iterable.create()));
                parseTest.run(
                    JSONObject.create()
                        .setObject("sourceFiles", JSONObject.create()
                            .setString("a/b.java", "0001-02-03T00:00Z")),
                    new PackJSON()
                        .setSourceFiles(Iterable.create(
                            new PackJSONFile()
                                .setRelativePath("a/b.java")
                                .setLastModified(DateTime.create(1, 2, 3)))));
                parseTest.run(
                    JSONObject.create()
                        .setObject("sourceOutputFiles", JSONObject.create()
                            .setString("a/b.java", "0001-02-03T00:00Z")),
                    new PackJSON()
                        .setSourceOutputFiles(Iterable.create(
                            new PackJSONFile()
                                .setRelativePath("a/b.java")
                                .setLastModified(DateTime.create(1, 2, 3)))));
                parseTest.run(
                    JSONObject.create()
                        .setObject("testOutputFiles", JSONObject.create()
                            .setString("a/b.java", "0001-02-03T00:00Z")),
                    new PackJSON()
                        .setTestOutputFiles(Iterable.create(
                            new PackJSONFile()
                                .setRelativePath("a/b.java")
                                .setLastModified(DateTime.create(1, 2, 3)))));
            });
        });
    }
}
