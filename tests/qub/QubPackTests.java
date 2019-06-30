package qub;

public interface QubPackTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(QubPack.class, () ->
        {
            runner.testGroup("setQubTest(QubTest)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final QubPack qubPack = new QubPack();
                    test.assertSame(qubPack, qubPack.setQubTest(null));
                    final QubTest qubTest = qubPack.getQubTest();
                    test.assertNotNull(qubTest);
                    test.assertSame(qubTest, qubPack.getQubTest());
                });

                runner.test("with non-null", (Test test) ->
                {
                    final QubPack qubPack = new QubPack();
                    final QubTest qubTest = qubPack.getQubTest();
                    test.assertSame(qubPack, qubPack.setQubTest(qubTest));
                    test.assertSame(qubTest, qubPack.getQubTest());
                });
            });

            runner.testGroup("setJarCreator(JarCreator)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final QubPack qubPack = new QubPack();
                    test.assertSame(qubPack, qubPack.setJarCreator(null));
                    final JarCreator jarCreator = qubPack.getJarCreator();
                    test.assertNotNull(jarCreator);
                    test.assertTrue(jarCreator instanceof JavaJarCreator);
                    test.assertSame(jarCreator, qubPack.getJarCreator());
                });

                runner.test("with non-null", (Test test) ->
                {
                    final QubPack qubPack = new QubPack();
                    final JarCreator jarCreator = new FakeJarCreator();
                    test.assertSame(qubPack, qubPack.setJarCreator(jarCreator));
                    test.assertSame(jarCreator, qubPack.getJarCreator());
                });
            });

            runner.testGroup("main(String[])", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(new PreConditionFailure("args cannot be null."),
                        () -> QubPack.main((String[])null));
                });
            });

            runner.testGroup("main(Console)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(new PreConditionFailure("console cannot be null."),
                        () -> main((Console)null));
                });

                runner.test("with \"-?\"", (Test test) ->
                {
                    final InMemoryByteStream output = new InMemoryByteStream();
                    final InMemoryByteStream error = new InMemoryByteStream();
                    try (final Console console = new Console(CommandLineArguments.create("-?")))
                    {
                        console.setOutputByteWriteStream(output);
                        console.setErrorByteWriteStream(error);

                        main(console);
                        test.assertEqual(-1, console.getExitCode());
                    }
                    test.assertEqual(
                        Iterable.create(
                            "Usage: qub-pack [[--folder=]<folder-to-pack>] [--verbose] [--profiler] [--help]",
                            "  Used to package source and compiled code in source code projects.",
                            "  --folder: The folder to pack. Defaults to the current folder.",
                            "  --verbose: Whether or not to show verbose logs.",
                            "  --profiler: Whether or not this application should pause before it is run to allow a profiler to be attached.",
                            "  --help(?): Show the help message for this application."),
                        Strings.getLines(output.asCharacterReadStream().getText().await()));
                    test.assertEqual("", error.asCharacterReadStream().getText().await());
                });

                runner.test("with no project.json file", (Test test) ->
                {
                    final InMemoryByteStream output = new InMemoryByteStream();
                    final InMemoryByteStream error = new InMemoryByteStream();
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    try (final Console console = new Console())
                    {
                        console.setOutputByteWriteStream(output);
                        console.setErrorByteWriteStream(error);
                        console.setFileSystem(fileSystem);
                        console.setCurrentFolderPathString("/");

                        main(console);
                        test.assertEqual(1, console.getExitCode());
                    }
                    test.assertEqual(
                        Iterable.create(
                            "Compiling...",
                            "ERROR: The file at \"/project.json\" doesn't exist."),
                        Strings.getLines(output.asCharacterReadStream().getText().await()));
                    test.assertEqual("", error.asCharacterReadStream().getText().await());
                });

                runner.test("with no source files", (Test test) ->
                {
                    final InMemoryByteStream output = new InMemoryByteStream();
                    final InMemoryByteStream error = new InMemoryByteStream();
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final ProjectJSON projectJSON = new ProjectJSON();
                    projectJSON.setProject("my-project");
                    projectJSON.setPublisher("me");
                    projectJSON.setVersion("34");
                    projectJSON.setJava(new ProjectJSONJava());
                    fileSystem.setFileContentAsString("/project.json", JSON.object(projectJSON::write).toString());
                    fileSystem.setFileContentAsString("/outputs/A.class", "there").await();
                    try (final Console console = new Console())
                    {
                        console.setOutputByteWriteStream(output);
                        console.setErrorByteWriteStream(error);
                        console.setFileSystem(fileSystem);
                        console.setCurrentFolderPathString("/");

                        main(console);
                        test.assertEqual(1, console.getExitCode());
                    }
                    test.assertEqual(
                        Iterable.create(
                            "Compiling...",
                            "ERROR: No java source files found in /."),
                        Strings.getLines(output.asCharacterReadStream().getText().await()));
                    test.assertEqual("", error.asCharacterReadStream().getText().await());
                });

                runner.test("with simple success", (Test test) ->
                {
                    final InMemoryByteStream output = new InMemoryByteStream();
                    final InMemoryByteStream error = new InMemoryByteStream();
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final ProjectJSON projectJSON = new ProjectJSON();
                    projectJSON.setProject("my-project");
                    projectJSON.setPublisher("me");
                    projectJSON.setVersion("34");
                    projectJSON.setJava(new ProjectJSONJava());
                    fileSystem.setFileContentAsString("/project.json", JSON.object(projectJSON::write).toString());
                    fileSystem.setFileContentAsString("/sources/A.java", "hello").await();
                    fileSystem.setFileContentAsString("/outputs/A.class", "there").await();
                    try (final Console console = new Console())
                    {
                        console.setOutputByteWriteStream(output);
                        console.setErrorByteWriteStream(error);
                        console.setFileSystem(fileSystem);
                        console.setCurrentFolderPathString("/");

                        main(console);
                        test.assertEqual(0, console.getExitCode());
                    }
                    test.assertEqual(
                        Iterable.create(
                            "Compiling...",
                            "Running tests...",
                            "",
                            "Creating sources jar file...",
                            "Creating compiled sources jar file..."),
                        Strings.getLines(output.asCharacterReadStream().getText().await()));
                    test.assertEqual("", error.asCharacterReadStream().getText().await());
                    test.assertEqual(
                        Iterable.create(
                            "Files:",
                            "A.java",
                            ""),
                        Strings.getLines(fileSystem.getFileContentAsString("/outputs/my-project.sources.jar").await()));
                    test.assertEqual(
                        Iterable.create(
                            "Files:",
                            "A.class",
                            ""),
                        Strings.getLines(fileSystem.getFileContentAsString("/outputs/my-project.jar").await()));
                });

                runner.test("with inner class in source file", (Test test) ->
                {
                    final InMemoryByteStream output = new InMemoryByteStream();
                    final InMemoryByteStream error = new InMemoryByteStream();
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final ProjectJSON projectJSON = new ProjectJSON();
                    projectJSON.setProject("my-project");
                    projectJSON.setPublisher("me");
                    projectJSON.setVersion("34");
                    projectJSON.setJava(new ProjectJSONJava());
                    fileSystem.setFileContentAsString("/project.json", JSON.object(projectJSON::write).toString());
                    fileSystem.setFileContentAsString("/sources/A.java", "hello").await();
                    fileSystem.setFileContentAsString("/outputs/A.class", "there").await();
                    fileSystem.setFileContentAsString("/outputs/A$B.class", "there").await();
                    try (final Console console = new Console())
                    {
                        console.setOutputByteWriteStream(output);
                        console.setErrorByteWriteStream(error);
                        console.setFileSystem(fileSystem);
                        console.setCurrentFolderPathString("/");

                        main(console);
                        test.assertEqual(0, console.getExitCode());
                    }
                    test.assertEqual(
                        Iterable.create(
                            "Compiling...",
                            "Running tests...",
                            "",
                            "Creating sources jar file...",
                            "Creating compiled sources jar file..."),
                        Strings.getLines(output.asCharacterReadStream().getText().await()));
                    test.assertEqual("", error.asCharacterReadStream().getText().await());
                    test.assertEqual(
                        Iterable.create(
                            "Files:",
                            "A.java",
                            ""),
                        Strings.getLines(fileSystem.getFileContentAsString("/outputs/my-project.sources.jar").await()));
                    test.assertEqual(
                        Iterable.create(
                            "Files:",
                            "A$B.class",
                            "A.class",
                            ""),
                        Strings.getLines(fileSystem.getFileContentAsString("/outputs/my-project.jar").await()));
                });

                runner.test("with anonymous classes in source file", (Test test) ->
                {
                    final InMemoryByteStream output = new InMemoryByteStream();
                    final InMemoryByteStream error = new InMemoryByteStream();
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final ProjectJSON projectJSON = new ProjectJSON();
                    projectJSON.setProject("my-project");
                    projectJSON.setPublisher("me");
                    projectJSON.setVersion("34");
                    projectJSON.setJava(new ProjectJSONJava());
                    fileSystem.setFileContentAsString("/project.json", JSON.object(projectJSON::write).toString());
                    fileSystem.setFileContentAsString("/sources/A.java", "hello").await();
                    fileSystem.setFileContentAsString("/outputs/A.class", "there").await();
                    fileSystem.setFileContentAsString("/outputs/A$1.class", "again").await();
                    fileSystem.setFileContentAsString("/outputs/A$2.class", "you").await();
                    try (final Console console = new Console())
                    {
                        console.setOutputByteWriteStream(output);
                        console.setErrorByteWriteStream(error);
                        console.setFileSystem(fileSystem);
                        console.setCurrentFolderPathString("/");

                        main(console);
                        test.assertEqual(0, console.getExitCode());
                    }
                    test.assertEqual(
                        Iterable.create(
                            "Compiling...",
                            "Running tests...",
                            "",
                            "Creating sources jar file...",
                            "Creating compiled sources jar file..."),
                        Strings.getLines(output.asCharacterReadStream().getText().await()));
                    test.assertEqual("", error.asCharacterReadStream().getText().await());
                    test.assertEqual(
                        Iterable.create(
                            "Files:",
                            "A.java",
                            ""),
                        Strings.getLines(fileSystem.getFileContentAsString("/outputs/my-project.sources.jar").await()));
                    test.assertEqual(
                        Iterable.create(
                            "Files:",
                            "A$1.class",
                            "A$2.class",
                            "A.class",
                            ""),
                        Strings.getLines(fileSystem.getFileContentAsString("/outputs/my-project.jar").await()));
                });

                runner.test("with main class in project.json", (Test test) ->
                {
                    final InMemoryByteStream output = new InMemoryByteStream();
                    final InMemoryByteStream error = new InMemoryByteStream();
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final ProjectJSON projectJSON = new ProjectJSON();
                    projectJSON.setProject("my-project");
                    projectJSON.setPublisher("me");
                    projectJSON.setVersion("34");
                    projectJSON.setJava(new ProjectJSONJava()
                        .setMainClass("A"));
                    fileSystem.setFileContentAsString("/project.json", JSON.object(projectJSON::write).toString());
                    fileSystem.setFileContentAsString("/sources/A.java", "hello").await();
                    fileSystem.setFileContentAsString("/outputs/A.class", "there").await();
                    try (final Console console = new Console())
                    {
                        console.setOutputByteWriteStream(output);
                        console.setErrorByteWriteStream(error);
                        console.setFileSystem(fileSystem);
                        console.setCurrentFolderPathString("/");

                        main(console);
                        test.assertEqual(0, console.getExitCode());
                    }
                    test.assertEqual(
                        Iterable.create(
                            "Compiling...",
                            "Running tests...",
                            "",
                            "Creating sources jar file...",
                            "Creating compiled sources jar file..."),
                        Strings.getLines(output.asCharacterReadStream().getText().await()));
                    test.assertEqual("", error.asCharacterReadStream().getText().await());
                    test.assertEqual(
                        Iterable.create(
                            "Files:",
                            "A.java",
                            ""),
                        Strings.getLines(fileSystem.getFileContentAsString("/outputs/my-project.sources.jar").await()));
                    test.assertEqual(
                        Iterable.create(
                            "Manifest file:",
                            "META-INF/MANIFEST.MF",
                            "",
                            "Files:",
                            "A.class",
                            ""),
                        Strings.getLines(fileSystem.getFileContentAsString("/outputs/my-project.jar").await()));
                    test.assertEqual(
                        Iterable.create(
                            "Manifest-Version: 1.0",
                            "Main-Class: A"),
                        Strings.getLines(fileSystem.getFileContentAsString("/outputs/META-INF/MANIFEST.MF").await()));
                });

                runner.test("with simple success and -verbose", (Test test) ->
                {
                    final InMemoryByteStream output = new InMemoryByteStream();
                    final InMemoryByteStream error = new InMemoryByteStream();
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final ProjectJSON projectJSON = new ProjectJSON()
                        .setProject("my-project")
                        .setPublisher("me")
                        .setVersion("34")
                        .setJava(new ProjectJSONJava());
                    fileSystem.setFileContentAsString("/project.json", JSON.object(projectJSON::write).toString());
                    fileSystem.setFileContentAsString("/sources/A.java", "hello").await();
                    fileSystem.setFileContentAsString("/outputs/A.class", "there").await();
                    try (final Console console = new Console(CommandLineArguments.create("-verbose")))
                    {
                        console.setOutputByteWriteStream(output);
                        console.setErrorByteWriteStream(error);
                        console.setFileSystem(fileSystem);
                        console.setCurrentFolderPathString("/");

                        main(console);
                        test.assertEqual(0, console.getExitCode());
                    }

                    test.assertEqual(
                        Iterable.create(
                            "Compiling...",
                            "VERBOSE: Parsing project.json...",
                            "VERBOSE: Parsing outputs/parse.json...",
                            "VERBOSE: Updating outputs/parse.json...",
                            "VERBOSE: Setting project.json...",
                            "VERBOSE: Setting source files...",
                            "VERBOSE: Writing parse.json file...",
                            "VERBOSE: Done writing parse.json file...",
                            "VERBOSE: Detecting java source files to compile...",
                            "VERBOSE: Compiling all source files.",
                            "VERBOSE: Starting compilation...",
                            "VERBOSE: Running javac -d /outputs -Xlint:unchecked -Xlint:deprecation -classpath /outputs sources/A.java...",
                            "VERBOSE: Compilation finished.",
                            "Running tests...",
                            "VERBOSE: java.exe -classpath /outputs qub.ConsoleTestRunner A",
                            "",
                            "Creating sources jar file...",
                            "VERBOSE: Created /outputs/my-project.sources.jar.",
                            "Creating compiled sources jar file...",
                            "VERBOSE: Created /outputs/my-project.jar."),
                        Strings.getLines(output.asCharacterReadStream().getText().await()));
                    test.assertEqual("", error.asCharacterReadStream().getText().await());
                    test.assertEqual(
                        Iterable.create(
                            "Files:",
                            "A.java",
                            ""),
                        Strings.getLines(fileSystem.getFileContentAsString("/outputs/my-project.sources.jar").await()));
                    test.assertEqual(
                        Iterable.create(
                            "Files:",
                            "A.class",
                            ""),
                        Strings.getLines(fileSystem.getFileContentAsString("/outputs/my-project.jar").await()));
                });
            });
        });
    }

    static void main(Console console)
    {
        final QubBuild build = new QubBuild();
        build.setJavaCompiler(new FakeJavaCompiler());

        final QubTest test = new QubTest();
        test.setJavaRunner(new FakeJavaRunner());
        test.setQubBuild(build);

        final QubPack pack = new QubPack();
        pack.setQubTest(test);
        pack.setJarCreator(new FakeJarCreator());
        pack.setShowTotalDuration(false);

        pack.main(console);
    }
}
