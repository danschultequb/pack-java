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
                        jarCreator::getJarCommandArguments);
                });

                runner.test("with no files set", (Test test) ->
                {
                    final JavaJarCreator jarCreator = new JavaJarCreator();

                    final InMemoryFileSystem fileSystem = JarCreatorTests.createFileSystem(test);
                    final Folder baseFolder = fileSystem.getFolder("/base/folder/").await();
                    jarCreator.setBaseFolder(baseFolder);

                    jarCreator.setJarName("hello");

                    test.assertThrows(new PostConditionFailure("result cannot be null."),
                        jarCreator::getJarCommandArguments);
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
                runner.test("with empty files set", (Test test) ->
                {
                    final JavaJarCreator jarCreator = new JavaJarCreator();

                    final Process process = test.getProcess();
                    final ProcessFactory processFactory = process.getProcessFactory();
                    final InMemoryByteStream output = new InMemoryByteStream();
                    final InMemoryByteStream error = new InMemoryByteStream();
                    final VerboseCharacterWriteStream verbose = new VerboseCharacterWriteStream(true, output.asCharacterReadStream());

                    final Folder baseFolder = process.getCurrentFolder().await();
                    jarCreator.setBaseFolder(baseFolder);

                    jarCreator.setJarName("hello");

                    jarCreator.setFiles(Iterable.create());

                    final File jarFile = jarCreator.createJarFile(processFactory, output, error, verbose).await();
                    try
                    {
                        test.assertNotNull(jarFile);
                        test.assertFalse(jarFile.exists().await());
                    }
                    finally
                    {
                        jarFile.delete().catchError(FileNotFoundException.class).await();
                    }

                    final String outputText = output.asCharacterReadStream().getText().await();
                    test.assertContains(outputText, " jar ");
                    test.assertContains(outputText, " cf ");
                    test.assertContains(outputText, "/qub-java-pack/hello.jar");
                    test.assertEqual(
                        Iterable.create(
                            "'c' flag requires manifest or input files to be specified!",
                            "Try `jar --help' for more information."),
                        Strings.getLines(error.asCharacterReadStream().getText().await()));
                });

                runner.test("with manifestFile set and empty files set", (Test test) ->
                {
                    final JavaJarCreator jarCreator = new JavaJarCreator();

                    final Process process = test.getProcess();
                    final ProcessFactory processFactory = process.getProcessFactory();
                    final InMemoryByteStream output = new InMemoryByteStream();
                    final InMemoryByteStream error = new InMemoryByteStream();
                    final VerboseCharacterWriteStream verbose = new VerboseCharacterWriteStream(true, output.asCharacterReadStream());

                    final Folder baseFolder = process.getCurrentFolder().await();
                    jarCreator.setBaseFolder(baseFolder);

                    jarCreator.setJarName("hello");

                    final File manifestFile = baseFolder.createFile("manifest.file").await();
                    try
                    {
                        jarCreator.setManifestFile(manifestFile);

                        jarCreator.setFiles(Iterable.create());

                        final File jarFile = jarCreator.createJarFile(processFactory, output, error, verbose).await();
                        try
                        {
                            test.assertNotNull(jarFile);
                            test.assertTrue(jarFile.exists().await());
                        }
                        finally
                        {
                            jarFile.delete().catchError(FileNotFoundException.class).await();
                        }

                        final String outputText = output.asCharacterReadStream().getText().await();
                        test.assertContains(outputText, " jar ");
                        test.assertContains(outputText, " cfm ");
                        test.assertContains(outputText, jarFile.toString());
                        test.assertContains(outputText, manifestFile.relativeTo(baseFolder).toString());
                        test.assertEqual("", error.asCharacterReadStream().getText().await());
                    }
                    finally
                    {
                        manifestFile.delete().await();
                    }
                });

                runner.test("with non-empty files set", (Test test) ->
                {
                    final JavaJarCreator jarCreator = new JavaJarCreator();

                    final Process process = test.getProcess();
                    final ProcessFactory processFactory = process.getProcessFactory();
                    final InMemoryByteStream output = new InMemoryByteStream();
                    final InMemoryByteStream error = new InMemoryByteStream();
                    final VerboseCharacterWriteStream verbose = new VerboseCharacterWriteStream(true, output.asCharacterReadStream());

                    final Folder baseFolder = process.getCurrentFolder().await();
                    jarCreator.setBaseFolder(baseFolder);

                    jarCreator.setJarName("hello");

                    final File licenseFile = baseFolder.getFile("LICENSE").await();
                    final File projectJsonFile = baseFolder.getFile("project.json").await();
                    final File qubPackJavaFile = baseFolder.getFile("sources/qub/QubPack.java").await();
                    jarCreator.setFiles(Iterable.create(
                        licenseFile,
                        projectJsonFile,
                        qubPackJavaFile
                    ));

                    final File jarFile = jarCreator.createJarFile(processFactory, output, error, verbose).await();
                    try
                    {
                        test.assertNotNull(jarFile);
                        test.assertTrue(jarFile.exists().await());
                    }
                    finally
                    {
                        jarFile.delete().catchError(FileNotFoundException.class).await();
                    }

                    final String outputText = output.asCharacterReadStream().getText().await();
                    test.assertContains(outputText, " jar ");
                    test.assertContains(outputText, " cf ");
                    test.assertContains(outputText, jarFile.toString());
                    test.assertContains(outputText, licenseFile.relativeTo(baseFolder).toString());
                    test.assertContains(outputText, projectJsonFile.relativeTo(baseFolder).toString());
                    test.assertContains(outputText, qubPackJavaFile.relativeTo(baseFolder).toString());
                    test.assertEqual("", error.asCharacterReadStream().getText().await());
                });

                runner.test("with manifestFile set and empty files set", (Test test) ->
                {
                    final JavaJarCreator jarCreator = new JavaJarCreator();

                    final Process process = test.getProcess();
                    final ProcessFactory processFactory = process.getProcessFactory();
                    final InMemoryByteStream output = new InMemoryByteStream();
                    final InMemoryByteStream error = new InMemoryByteStream();
                    final VerboseCharacterWriteStream verbose = new VerboseCharacterWriteStream(true, output.asCharacterReadStream());

                    final Folder baseFolder = process.getCurrentFolder().await();
                    jarCreator.setBaseFolder(baseFolder);

                    jarCreator.setJarName("hello");

                    final File manifestFile = baseFolder.createFile("manifest.file").await();
                    try
                    {
                        jarCreator.setManifestFile(manifestFile);

                        final File licenseFile = baseFolder.getFile("LICENSE").await();
                        final File projectJsonFile = baseFolder.getFile("project.json").await();
                        final File qubPackJavaFile = baseFolder.getFile("sources/qub/QubPack.java").await();
                        jarCreator.setFiles(Iterable.create(
                            licenseFile,
                            projectJsonFile,
                            qubPackJavaFile
                        ));

                        final File jarFile = jarCreator.createJarFile(processFactory, output, error, verbose).await();
                        try
                        {
                            test.assertNotNull(jarFile);
                            test.assertTrue(jarFile.exists().await());
                        }
                        finally
                        {
                            jarFile.delete().catchError(FileNotFoundException.class).await();
                        }

                        final String outputText = output.asCharacterReadStream().getText().await();
                        test.assertContains(outputText, " jar ");
                        test.assertContains(outputText, " cfm ");
                        test.assertContains(outputText, manifestFile.relativeTo(baseFolder).toString());
                        test.assertContains(outputText, jarFile.toString());
                        test.assertContains(outputText, licenseFile.relativeTo(baseFolder).toString());
                        test.assertContains(outputText, projectJsonFile.relativeTo(baseFolder).toString());
                        test.assertContains(outputText, qubPackJavaFile.relativeTo(baseFolder).toString());
                        test.assertEqual("", error.asCharacterReadStream().getText().await());
                    }
                    finally
                    {
                        manifestFile.delete().await();
                    }
                });
            });
        });
    }
}
