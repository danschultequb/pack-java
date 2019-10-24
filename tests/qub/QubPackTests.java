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
                            "  --verbose(v): Whether or not to show verbose logs.",
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
                    fileSystem.setFileContentAsString("/project.json",
                        new ProjectJSON()
                            .setProject("my-project")
                            .setPublisher("me")
                            .setVersion("34")
                            .setJava(new ProjectJSONJava())
                            .toString());
                    fileSystem.setFileContentAsString("/sources/A.java", "hello").await();
                    fileSystem.setFileContentAsString("/outputs/A.class", "there").await();
                    try (final Console console = new Console())
                    {
                        console.setOutputByteWriteStream(output);
                        console.setErrorByteWriteStream(error);
                        console.setFileSystem(fileSystem);
                        console.setCurrentFolderPathString("/");

                        final Folder currentFolder = console.getCurrentFolder().await();
                        console.setProcessFactory(new FakeProcessFactory(console.getParallelAsyncRunner(), currentFolder)
                            .add(new FakeJavacProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addOutputFolder(currentFolder.getFolder("outputs").await())
                                .addXlintUnchecked()
                                .addXlintDeprecation()
                                .addClasspath("/outputs")
                                .addSourceFilePathStrings("sources/A.java")
                                .setFunctionAutomatically()));

                        main(console);

                        test.assertEqual(
                            Iterable.create(
                                "Compiling 1 file...",
                                "Running tests...",
                                "",
                                "Creating sources jar file...",
                                "Creating compiled sources jar file..."),
                            Strings.getLines(output.asCharacterReadStream().getText().await()));
                        test.assertEqual("", error.asCharacterReadStream().getText().await());

                        test.assertEqual(0, console.getExitCode());
                    }
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
                    fileSystem.setFileContentAsString("/project.json",
                        new ProjectJSON()
                            .setProject("my-project")
                            .setPublisher("me")
                            .setVersion("34")
                            .setJava(new ProjectJSONJava())
                            .toString());
                    fileSystem.setFileContentAsString("/sources/A.java", "hello").await();
                    fileSystem.setFileContentAsString("/outputs/A.class", "there").await();
                    fileSystem.setFileContentAsString("/outputs/A$B.class", "there").await();
                    try (final Console console = new Console())
                    {
                        console.setOutputByteWriteStream(output);
                        console.setErrorByteWriteStream(error);
                        console.setFileSystem(fileSystem);
                        console.setCurrentFolderPathString("/");

                        final Folder currentFolder = console.getCurrentFolder().await();
                        console.setProcessFactory(new FakeProcessFactory(console.getParallelAsyncRunner(), currentFolder)
                            .add(new FakeJavacProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addOutputFolder(currentFolder.getFolder("outputs").await())
                                .addXlintUnchecked()
                                .addXlintDeprecation()
                                .addClasspath("/outputs")
                                .addSourceFilePathStrings("sources/A.java")
                                .setFunctionAutomatically()));

                        main(console);

                        test.assertEqual(
                            Iterable.create(
                                "Compiling 1 file...",
                                "Running tests...",
                                "",
                                "Creating sources jar file...",
                                "Creating compiled sources jar file..."),
                            Strings.getLines(output.asCharacterReadStream().getText().await()));
                        test.assertEqual("", error.asCharacterReadStream().getText().await());

                        test.assertEqual(0, console.getExitCode());
                    }
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
                    fileSystem.setFileContentAsString("/project.json",
                        new ProjectJSON()
                            .setProject("my-project")
                            .setPublisher("me")
                            .setVersion("34")
                            .setJava(new ProjectJSONJava())
                            .toString());
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

                        final Folder currentFolder = console.getCurrentFolder().await();
                        console.setProcessFactory(new FakeProcessFactory(console.getParallelAsyncRunner(), currentFolder)
                            .add(new FakeJavacProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addOutputFolder(currentFolder.getFolder("outputs").await())
                                .addXlintUnchecked()
                                .addXlintDeprecation()
                                .addClasspath("/outputs")
                                .addSourceFilePathStrings("sources/A.java")
                                .setFunctionAutomatically()));

                        main(console);

                        test.assertEqual(
                            Iterable.create(
                                "Compiling 1 file...",
                                "Running tests...",
                                "",
                                "Creating sources jar file...",
                                "Creating compiled sources jar file..."),
                            Strings.getLines(output.asCharacterReadStream().getText().await()));
                        test.assertEqual("", error.asCharacterReadStream().getText().await());

                        test.assertEqual(0, console.getExitCode());
                    }
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
                    final ProjectJSON projectJSON = new ProjectJSON()
                        .setProject("my-project")
                        .setPublisher("me")
                        .setVersion("34")
                        .setJava(new ProjectJSONJava()
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

                        final Folder currentFolder = console.getCurrentFolder().await();
                        console.setProcessFactory(new FakeProcessFactory(console.getParallelAsyncRunner(), currentFolder)
                            .add(new FakeJavacProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addOutputFolder(currentFolder.getFolder("outputs").await())
                                .addXlintUnchecked()
                                .addXlintDeprecation()
                                .addClasspath("/outputs")
                                .addSourceFilePathStrings("sources/A.java")
                                .setFunctionAutomatically()));

                        main(console);

                        test.assertEqual(
                            Iterable.create(
                                "Compiling 1 file...",
                                "Running tests...",
                                "",
                                "Creating sources jar file...",
                                "Creating compiled sources jar file..."),
                            Strings.getLines(output.asCharacterReadStream().getText().await()));
                        test.assertEqual("", error.asCharacterReadStream().getText().await());

                        test.assertEqual(0, console.getExitCode());
                    }
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

                        final Folder currentFolder = console.getCurrentFolder().await();
                        console.setProcessFactory(new FakeProcessFactory(console.getParallelAsyncRunner(), currentFolder)
                            .add(new FakeJavacProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addOutputFolder(currentFolder.getFolder("outputs").await())
                                .addXlintUnchecked()
                                .addXlintDeprecation()
                                .addClasspath("/outputs")
                                .addSourceFilePathStrings("sources/A.java")
                                .setFunctionAutomatically()));

                        main(console);

                        test.assertEqual(
                            Iterable.create(
                                "VERBOSE: Parsing project.json...",
                                "VERBOSE: Parsing outputs/build.json...",
                                "VERBOSE: Updating outputs/build.json...",
                                "VERBOSE: Setting project.json...",
                                "VERBOSE: Setting source files...",
                                "VERBOSE: Detecting java source files to compile...",
                                "VERBOSE: Compiling all source files.",
                                "Compiling 1 file...",
                                "VERBOSE: Running /: javac -d outputs -Xlint:unchecked -Xlint:deprecation -classpath /outputs sources/A.java...",
                                "VERBOSE: Compilation finished.",
                                "VERBOSE: Writing build.json file...",
                                "VERBOSE: Done writing build.json file.",
                                "Running tests...",
                                "VERBOSE: java.exe -classpath /outputs qub.ConsoleTestRunner --profiler=false --testjson=true --output-folder=/outputs --coverage=None A",
                                "",
                                "Creating sources jar file...",
                                "VERBOSE: Created /outputs/my-project.sources.jar.",
                                "Creating compiled sources jar file...",
                                "VERBOSE: Created /outputs/my-project.jar."),
                            Strings.getLines(output.asCharacterReadStream().getText().await()));
                        test.assertEqual("", error.asCharacterReadStream().getText().await());

                        test.assertEqual(0, console.getExitCode());
                    }
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

                runner.test("with test folder", (Test test) ->
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
                    fileSystem.setFileContentAsString("/tests/ATests.java", "hi").await();
                    fileSystem.setFileContentAsString("/outputs/A.class", "there").await();
                    fileSystem.setFileContentAsString("/outputs/ATests.class", "again").await();
                    try (final Console console = new Console())
                    {
                        console.setOutputByteWriteStream(output);
                        console.setErrorByteWriteStream(error);
                        console.setFileSystem(fileSystem);
                        console.setCurrentFolderPathString("/");

                        final Folder currentFolder = console.getCurrentFolder().await();
                        console.setProcessFactory(new FakeProcessFactory(console.getParallelAsyncRunner(), currentFolder)
                            .add(new FakeJavacProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addOutputFolder(currentFolder.getFolder("outputs").await())
                                .addXlintUnchecked()
                                .addXlintDeprecation()
                                .addClasspath("/outputs")
                                .addSourceFilePathStrings("sources/A.java", "tests/ATests.java")
                                .setFunctionAutomatically()));

                        main(console);
                        test.assertEqual(0, console.getExitCode());
                    }
                    test.assertEqual(
                        Iterable.create(
                            "Compiling 2 files...",
                            "Running tests...",
                            "",
                            "Creating sources jar file...",
                            "Creating compiled sources jar file...",
                            "Creating compiled tests jar file..."),
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
                    test.assertEqual(
                        Iterable.create(
                            "Files:",
                            "ATests.class",
                            ""),
                        Strings.getLines(fileSystem.getFileContentAsString("/outputs/my-project.tests.jar").await()));
                });

                runner.test("with test folder with inner class", (Test test) ->
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
                    fileSystem.setFileContentAsString("/tests/ATests.java", "hi").await();
                    fileSystem.setFileContentAsString("/outputs/A.class", "there").await();
                    fileSystem.setFileContentAsString("/outputs/ATests.class", "again").await();
                    fileSystem.setFileContentAsString("/outputs/ATests$Inner.class", "again").await();
                    try (final Console console = new Console())
                    {
                        console.setOutputByteWriteStream(output);
                        console.setErrorByteWriteStream(error);
                        console.setFileSystem(fileSystem);
                        console.setCurrentFolderPathString("/");

                        final Folder currentFolder = console.getCurrentFolder().await();
                        console.setProcessFactory(new FakeProcessFactory(console.getParallelAsyncRunner(), currentFolder)
                            .add(new FakeJavacProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addOutputFolder(currentFolder.getFolder("outputs").await())
                                .addXlintUnchecked()
                                .addXlintDeprecation()
                                .addClasspath("/outputs")
                                .addSourceFilePathStrings("sources/A.java", "tests/ATests.java")
                                .setFunctionAutomatically()));

                        main(console);
                        test.assertEqual(0, console.getExitCode());
                    }
                    test.assertEqual(
                        Iterable.create(
                            "Compiling 2 files...",
                            "Running tests...",
                            "",
                            "Creating sources jar file...",
                            "Creating compiled sources jar file...",
                            "Creating compiled tests jar file..."),
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
                    test.assertEqual(
                        Iterable.create(
                            "Files:",
                            "ATests$Inner.class",
                            "ATests.class",
                            ""),
                        Strings.getLines(fileSystem.getFileContentAsString("/outputs/my-project.tests.jar").await()));
                });

                runner.test("with test folder with anonymous class", (Test test) ->
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
                    fileSystem.setFileContentAsString("/tests/ATests.java", "hi").await();
                    fileSystem.setFileContentAsString("/outputs/A.class", "there").await();
                    fileSystem.setFileContentAsString("/outputs/ATests.class", "again").await();
                    fileSystem.setFileContentAsString("/outputs/ATests$1.class", "again").await();
                    try (final Console console = new Console())
                    {
                        console.setOutputByteWriteStream(output);
                        console.setErrorByteWriteStream(error);
                        console.setFileSystem(fileSystem);
                        console.setCurrentFolderPathString("/");

                        final Folder currentFolder = console.getCurrentFolder().await();
                        console.setProcessFactory(new FakeProcessFactory(console.getParallelAsyncRunner(), currentFolder)
                            .add(new FakeJavacProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addOutputFolder(currentFolder.getFolder("outputs").await())
                                .addXlintUnchecked()
                                .addXlintDeprecation()
                                .addClasspath("/outputs")
                                .addSourceFilePathStrings("sources/A.java", "tests/ATests.java")
                                .setFunctionAutomatically()));

                        main(console);

                        test.assertEqual(
                            Iterable.create(
                                "Compiling 2 files...",
                                "Running tests...",
                                "",
                                "Creating sources jar file...",
                                "Creating compiled sources jar file...",
                                "Creating compiled tests jar file..."),
                            Strings.getLines(output.asCharacterReadStream().getText().await()));

                        test.assertEqual(0, console.getExitCode());
                    }
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
                    test.assertEqual(
                        Iterable.create(
                            "Files:",
                            "ATests$1.class",
                            "ATests.class",
                            ""),
                        Strings.getLines(fileSystem.getFileContentAsString("/outputs/my-project.tests.jar").await()));
                });
            });
        });
    }

    static void main(Console console)
    {
        final QubTest test = new QubTest();
        test.setJavaRunner(new FakeJavaRunner());

        final QubPack pack = new QubPack();
        pack.setQubTest(test);
        pack.setJarCreator(new FakeJarCreator());
        pack.setShowTotalDuration(false);

        pack.main(console);
    }
}
