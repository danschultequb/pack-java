package qub;

public interface JavaJarCreatorTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(JavaJarCreator.class, () ->
        {
            JarCreatorTests.test(runner, JavaJarCreator::new);

            runner.testGroup("getJarCommandArguments()", () ->
            {
                runner.test("with no baseFolder set", (Test test) ->
                {
                    final JavaJarCreator jarCreator = new JavaJarCreator();

                    test.assertThrows(new PostConditionFailure("result cannot be null."),
                        () -> jarCreator.getJarCommandArguments());
                });

                runner.test("with no jarName set", (Test test) ->
                {
                    final JavaJarCreator jarCreator = new JavaJarCreator();

                    final InMemoryFileSystem fileSystem = JarCreatorTests.createFileSystem(test);
                    final Folder baseFolder = fileSystem.getFolder("/base/folder/").await();
                    jarCreator.setBaseFolder(baseFolder);

                    test.assertThrows(new PostConditionFailure("result cannot be null."),
                        () -> jarCreator.getJarCommandArguments());
                });

                runner.test("with no files set", (Test test) ->
                {
                    final JavaJarCreator jarCreator = new JavaJarCreator();

                    final InMemoryFileSystem fileSystem = JarCreatorTests.createFileSystem(test);
                    final Folder baseFolder = fileSystem.getFolder("/base/folder/").await();
                    jarCreator.setBaseFolder(baseFolder);

                    jarCreator.setJarName("hello");

                    test.assertThrows(new PostConditionFailure("result cannot be null."),
                        () -> jarCreator.getJarCommandArguments());
                });

                runner.test("with empty files set", (Test test) ->
                {
                    final JavaJarCreator jarCreator = new JavaJarCreator();

                    final InMemoryFileSystem fileSystem = JarCreatorTests.createFileSystem(test);
                    final Folder baseFolder = fileSystem.getFolder("/base/folder/").await();
                    jarCreator.setBaseFolder(baseFolder);

                    jarCreator.setJarName("hello");

                    jarCreator.setFiles(Iterable.create());

                    test.assertEqual(
                        Iterable.create(
                            "cf",
                            "/base/folder/hello.jar"),
                        jarCreator.getJarCommandArguments());
                });

                runner.test("with manifestFile set and empty files set", (Test test) ->
                {
                    final JavaJarCreator jarCreator = new JavaJarCreator();

                    final InMemoryFileSystem fileSystem = JarCreatorTests.createFileSystem(test);
                    final Folder baseFolder = fileSystem.getFolder("/base/folder/").await();
                    jarCreator.setBaseFolder(baseFolder);

                    jarCreator.setJarName("hello");

                    final File manifestFile = baseFolder.getFile("manifest.file").await();
                    jarCreator.setManifestFile(manifestFile);

                    jarCreator.setFiles(Iterable.create());

                    test.assertEqual(
                        Iterable.create(
                            "cfm",
                            "/base/folder/hello.jar",
                            "manifest.file"),
                        jarCreator.getJarCommandArguments());
                });

                runner.test("with non-empty files set", (Test test) ->
                {
                    final JavaJarCreator jarCreator = new JavaJarCreator();

                    final InMemoryFileSystem fileSystem = JarCreatorTests.createFileSystem(test);
                    final Folder baseFolder = fileSystem.getFolder("/base/folder/").await();
                    jarCreator.setBaseFolder(baseFolder);

                    jarCreator.setJarName("hello");

                    jarCreator.setFiles(Iterable.create(
                        baseFolder.getFile("src/code.java").await(),
                        baseFolder.getFile("src/otherCode.java").await()
                    ));

                    test.assertEqual(
                        Iterable.create(
                            "cf",
                            "/base/folder/hello.jar",
                            "src/code.java",
                            "src/otherCode.java"),
                        jarCreator.getJarCommandArguments());
                });

                runner.test("with manifestFile set and non-empty files set", (Test test) ->
                {
                    final JavaJarCreator jarCreator = new JavaJarCreator();

                    final InMemoryFileSystem fileSystem = JarCreatorTests.createFileSystem(test);
                    final Folder baseFolder = fileSystem.getFolder("/base/folder/").await();
                    jarCreator.setBaseFolder(baseFolder);

                    jarCreator.setJarName("hello");

                    final File manifestFile = baseFolder.getFile("manifest.file").await();
                    jarCreator.setManifestFile(manifestFile);

                    jarCreator.setFiles(Iterable.create(
                        baseFolder.getFile("src/code.java").await(),
                        baseFolder.getFile("src/otherCode.java").await()
                    ));

                    test.assertEqual(
                        Iterable.create(
                            "cfm",
                            "/base/folder/hello.jar",
                            "manifest.file",
                            "src/code.java",
                            "src/otherCode.java"),
                        jarCreator.getJarCommandArguments());
                });
            });

            runner.testGroup("createJarFile(Process,boolean)", () ->
            {
//                runner.test("with empty files set", (Test test) ->
//                {
//                    final JavaJarCreator jarCreator = new JavaJarCreator();
//
//                    final InMemoryFileSystem fileSystem = JarCreatorTests.createFileSystem(test);
//                    final Folder baseFolder = fileSystem.getFolder("/base/folder/").await();
//                    jarCreator.setBaseFolder(baseFolder);
//
//                    jarCreator.setJarName("hello");
//
//                    jarCreator.setFiles(Iterable.create());
//
//                    final File jarFile = jarCreator.createJarFile(test.getProcess(), false).await();
//                    test.assertNotNull(jarFile);
//                    test.assertTrue(jarFile.exists().await());
//                    test.assertEqual(0, jarFile.getContents().await().length);
//                });
//
//                runner.test("with manifestFile set and empty files set", (Test test) ->
//                {
//                    final JavaJarCreator jarCreator = new JavaJarCreator();
//
//                    final InMemoryFileSystem fileSystem = JarCreatorTests.createFileSystem(test);
//                    final Folder baseFolder = fileSystem.getFolder("/base/folder/").await();
//                    jarCreator.setBaseFolder(baseFolder);
//
//                    jarCreator.setJarName("hello");
//
//                    jarCreator.setManifestFile(baseFolder.getFile("manifest.file").await());
//
//                    jarCreator.setFiles(Iterable.create());
//
//                    final File jarFile = jarCreator.createJarFile(test.getProcess(), false).await();
//                    test.assertNotNull(jarFile);
//                    test.assertTrue(jarFile.exists().await());
//                    test.assertEqual(
//                        Iterable.create(
//                            "Manifest file:",
//                            "manifest.file",
//                            ""
//                        ),
//                        Strings.getLines(jarFile.getContentsAsString().await()));
//                });
//
//                runner.test("with non-empty files set", (Test test) ->
//                {
//                    final JavaJarCreator jarCreator = new JavaJarCreator();
//
//                    final InMemoryFileSystem fileSystem = JarCreatorTests.createFileSystem(test);
//                    final Folder baseFolder = fileSystem.getFolder("/base/folder/").await();
//                    jarCreator.setBaseFolder(baseFolder);
//
//                    jarCreator.setJarName("hello");
//
//                    jarCreator.setFiles(Iterable.create(
//                        baseFolder.getFile("src/code.java").await(),
//                        baseFolder.getFile("src/otherCode.java").await()
//                    ));
//
//                    final File jarFile = jarCreator.createJarFile(test.getProcess(), false).await();
//                    test.assertNotNull(jarFile);
//                    test.assertTrue(jarFile.exists().await());
//                    test.assertEqual(
//                        Iterable.create(
//                            "Files:",
//                            "src/code.java",
//                            "src/otherCode.java",
//                            ""
//                        ),
//                        Strings.getLines(jarFile.getContentsAsString().await()));
//                });
//
//                runner.test("with manifestFile set and empty files set", (Test test) ->
//                {
//                    final JavaJarCreator jarCreator = new JavaJarCreator();
//
//                    final InMemoryFileSystem fileSystem = JarCreatorTests.createFileSystem(test);
//                    final Folder baseFolder = fileSystem.getFolder("/base/folder/").await();
//                    jarCreator.setBaseFolder(baseFolder);
//
//                    jarCreator.setJarName("hello");
//
//                    jarCreator.setManifestFile(baseFolder.getFile("manifest.file").await());
//
//                    jarCreator.setFiles(Iterable.create(
//                        baseFolder.getFile("src/code.java").await(),
//                        baseFolder.getFile("src/otherCode.java").await()
//                    ));
//
//                    final File jarFile = jarCreator.createJarFile(test.getProcess(), false).await();
//                    test.assertNotNull(jarFile);
//                    test.assertTrue(jarFile.exists().await());
//                    test.assertEqual(
//                        Iterable.create(
//                            "Manifest file:",
//                            "manifest.file",
//                            "",
//                            "Files:",
//                            "src/code.java",
//                            "src/otherCode.java",
//                            ""
//                        ),
//                        Strings.getLines(jarFile.getContentsAsString().await()));
//                });
            });
        });
    }
}
