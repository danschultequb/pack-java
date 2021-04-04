package qub;

public interface PackJSONTests
{
    static void test(TestRunner runner)
    {
        PreCondition.assertNotNull(runner, "runner");

        runner.testGroup(PackJSON.class, () ->
        {
            runner.test("create()", (Test test) ->
            {
                final PackJSON packJson = PackJSON.create();
                test.assertNotNull(packJson);
                test.assertNull(packJson.getProject());
                test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
            });

            runner.testGroup("create(JSONObject)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> PackJSON.create(null),
                        new PreConditionFailure("json cannot be null."));
                });

                final Action2<JSONObject,PackJSON> createTest = (JSONObject json, PackJSON expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(json), (Test test) ->
                    {
                        test.assertEqual(expected, PackJSON.create(json));
                    });
                };

                createTest.run(
                    JSONObject.create(),
                    PackJSON.create());
                createTest.run(
                    JSONObject.create()
                        .setString("project", "hello"),
                    PackJSON.create()
                        .setProject("hello"));
                createTest.run(
                    JSONObject.create()
                        .setObject("sourceFiles", JSONObject.create()),
                    PackJSON.create()
                        .setSourceFiles(Iterable.create()));
                createTest.run(
                    JSONObject.create()
                        .setObject("sourceFiles", JSONObject.create()
                            .setString("a/b.java", "0001-02-03T00:00Z")),
                    PackJSON.create()
                        .setSourceFiles(Iterable.create(
                            PackJSONFile.create("a/b.java", DateTime.create(1, 2, 3)))));
                createTest.run(
                    JSONObject.create()
                        .setObject("sourceOutputFiles", JSONObject.create()
                            .setString("a/b.java", "0001-02-03T00:00Z")),
                    PackJSON.create()
                        .setSourceOutputFiles(Iterable.create(
                            PackJSONFile.create("a/b.java", DateTime.create(1, 2, 3)))));
                createTest.run(
                    JSONObject.create()
                        .setObject("testOutputFiles", JSONObject.create()
                            .setString("a/b.java", "0001-02-03T00:00Z")),
                    PackJSON.create()
                        .setTestOutputFiles(Iterable.create(
                            PackJSONFile.create("a/b.java", DateTime.create(1, 2, 3)))));
            });

            runner.testGroup("setProject(String)", () ->
            {
                final Action2<String,Throwable> setProjectErrorTest = (String project, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(project), (Test test) ->
                    {
                        final PackJSON packJSON = PackJSON.create();
                        test.assertThrows(() -> packJSON.setProject(project), expected);
                        test.assertNull(packJSON.getProject());
                    });
                };

                setProjectErrorTest.run(null, new PreConditionFailure("project cannot be null."));
                setProjectErrorTest.run("", new PreConditionFailure("project cannot be empty."));

                final Action1<String> setProjectTest = (String project) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(project), (Test test) ->
                    {
                        final PackJSON packJSON = PackJSON.create();
                        final PackJSON setProjectResult = packJSON.setProject(project);
                        test.assertSame(packJSON, setProjectResult);
                        test.assertEqual(project, packJSON.getProject());
                    });
                };

                setProjectTest.run("hello");
            });

            runner.testGroup("setSourceFiles(Iterable<PackJSONFile>)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final PackJSON packJson = PackJSON.create();
                    test.assertThrows(() -> packJson.setSourceFiles(null),
                        new PreConditionFailure("sourceFiles cannot be null."));
                    test.assertEqual(Iterable.create(), packJson.getSourceFiles());
                });

                runner.test("with empty", (Test test) ->
                {
                    final PackJSON packJson = PackJSON.create();
                    final Iterable<PackJSONFile> files = Iterable.create();
                    test.assertSame(packJson, packJson.setSourceFiles(files));
                    test.assertEqual(files, packJson.getSourceFiles());
                });

                runner.test("with non-empty", (Test test) ->
                {
                    final PackJSON packJson = PackJSON.create();
                    final Iterable<PackJSONFile> files = Iterable.create(
                        PackJSONFile.create("hello.java", DateTime.epoch));
                    test.assertSame(packJson, packJson.setSourceFiles(files));
                    test.assertEqual(files, packJson.getSourceFiles());
                });
            });

            runner.testGroup("setSourceOutputFiles(Iterable<PackJSONFile>)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final PackJSON packJson = PackJSON.create();
                    test.assertThrows(() -> packJson.setSourceOutputFiles(null),
                        new PreConditionFailure("sourceOutputFiles cannot be null."));
                    test.assertEqual(Iterable.create(), packJson.getSourceOutputFiles());
                });

                runner.test("with empty", (Test test) ->
                {
                    final PackJSON packJson = PackJSON.create();
                    final Iterable<PackJSONFile> files = Iterable.create();
                    test.assertSame(packJson, packJson.setSourceOutputFiles(files));
                    test.assertEqual(files, packJson.getSourceOutputFiles());
                });

                runner.test("with non-empty", (Test test) ->
                {
                    final PackJSON packJson = PackJSON.create();
                    final Iterable<PackJSONFile> files = Iterable.create(
                        PackJSONFile.create("hello.java", DateTime.epoch));
                    test.assertSame(packJson, packJson.setSourceOutputFiles(files));
                    test.assertEqual(files, packJson.getSourceOutputFiles());
                });
            });

            runner.testGroup("setTestOutputFiles(Iterable<PackJSONFile>)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final PackJSON packJson = PackJSON.create();
                    test.assertThrows(() -> packJson.setTestOutputFiles(null),
                        new PreConditionFailure("testOutputFiles cannot be null."));
                    test.assertEqual(Iterable.create(), packJson.getTestOutputFiles());
                });

                runner.test("with empty", (Test test) ->
                {
                    final PackJSON packJson = PackJSON.create();
                    final Iterable<PackJSONFile> files = Iterable.create();
                    test.assertSame(packJson, packJson.setTestOutputFiles(files));
                    test.assertEqual(files, packJson.getTestOutputFiles());
                });

                runner.test("with non-empty", (Test test) ->
                {
                    final PackJSON packJson = PackJSON.create();
                    final Iterable<PackJSONFile> files = Iterable.create(
                        PackJSONFile.create("hello.java", DateTime.create(1, 2, 3)));
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
                    PackJSON.create(),
                    "{}");
                toStringTest.run(
                    PackJSON.create()
                        .setSourceFiles(Iterable.create()),
                    "{\"sourceFiles\":{}}");
                toStringTest.run(
                    PackJSON.create()
                        .setSourceFiles(Iterable.create(
                            PackJSONFile.create("a/b.java", DateTime.create(1, 2, 3)))),
                    "{\"sourceFiles\":{\"a/b.java\":\"0001-02-03T00:00Z\"}}");
                toStringTest.run(
                    PackJSON.create()
                        .setSourceOutputFiles(Iterable.create()),
                    "{\"sourceOutputFiles\":{}}");
                toStringTest.run(
                    PackJSON.create()
                        .setSourceOutputFiles(Iterable.create(
                            PackJSONFile.create("a/b.java", DateTime.create(1, 2, 3)))),
                    "{\"sourceOutputFiles\":{\"a/b.java\":\"0001-02-03T00:00Z\"}}");
                toStringTest.run(
                    PackJSON.create()
                        .setTestOutputFiles(Iterable.create()),
                    "{\"testOutputFiles\":{}}");
                toStringTest.run(
                    PackJSON.create()
                        .setTestOutputFiles(Iterable.create(
                            PackJSONFile.create("a/b.java", DateTime.create(1, 2, 3)))),
                    "{\"testOutputFiles\":{\"a/b.java\":\"0001-02-03T00:00Z\"}}");
                toStringTest.run(
                    PackJSON.create()
                        .setSourceFiles(Iterable.create(
                            PackJSONFile.create("a/b.java", DateTime.create(1, 2, 3))))
                        .setSourceOutputFiles(Iterable.create(
                            PackJSONFile.create("c/d/e.class", DateTime.create(4, 5, 6))))
                        .setTestOutputFiles(Iterable.create(
                            PackJSONFile.create("f.class", DateTime.create(7, 8, 9)))),
                    "{\"sourceFiles\":{\"a/b.java\":\"0001-02-03T00:00Z\"},\"sourceOutputFiles\":{\"c/d/e.class\":\"0004-05-06T00:00Z\"},\"testOutputFiles\":{\"f.class\":\"0007-08-09T00:00Z\"}}");
            });
        });
    }
}
