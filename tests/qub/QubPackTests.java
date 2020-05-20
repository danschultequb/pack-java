package qub;

public interface QubPackTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(QubPack.class, () ->
        {
            runner.testGroup("main(String[])", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(new PreConditionFailure("args cannot be null."),
                        () -> QubPack.main((String[])null));
                });
            });

            runner.testGroup("main(QubProcess)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(new PreConditionFailure("process cannot be null."),
                        () -> QubPack.main((QubProcess)null));
                });

                runner.test("with \"-?\"", (Test test) ->
                {
                    final InMemoryCharacterToByteStream output = InMemoryCharacterToByteStream.create();
                    final InMemoryCharacterToByteStream error = InMemoryCharacterToByteStream.create();
                    try (final QubProcess process = QubProcess.create("-?"))
                    {
                        process.setOutputWriteStream(output);
                        process.setErrorWriteStream(error);

                        QubPack.main(process);

                        test.assertEqual(-1, process.getExitCode());
                    }
                    test.assertEqual(
                        Iterable.create(
                            "Usage: qub-pack [[--folder=]<folder-to-pack>] [--packjson] [--testjson] [--buildjson] [--warnings=<show|error|hide>] [--verbose] [--profiler] [--help]",
                            "  Used to package source and compiled code in source code projects.",
                            "  --folder: The folder to pack. Defaults to the current folder.",
                            "  --packjson: Whether or not to read and write a pack.json file. Defaults to true.",
                            "  --testjson: Whether or not to write the test results to a test.json file.",
                            "  --buildjson: Whether or not to read and write a build.json file. Defaults to true.",
                            "  --warnings: How to handle build warnings. Can be either \"show\", \"error\", or \"hide\". Defaults to \"show\".",
                            "  --verbose(v): Whether or not to show verbose logs.",
                            "  --profiler: Whether or not this application should pause before it is run to allow a profiler to be attached.",
                            "  --help(?): Show the help message for this application."),
                        Strings.getLines(output.getText().await()));
                    test.assertEqual("", error.getText().await());
                });

                runner.test("with no project.json file", (Test test) ->
                {
                    final InMemoryCharacterToByteStream output = InMemoryCharacterToByteStream.create();
                    final InMemoryCharacterToByteStream error = InMemoryCharacterToByteStream.create();
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    try (final QubProcess process = QubProcess.create())
                    {
                        process.setOutputWriteStream(output);
                        process.setErrorWriteStream(error);
                        process.setFileSystem(fileSystem);
                        process.setCurrentFolderPathString("/");

                        QubPack.main(process);

                        test.assertEqual(1, process.getExitCode());
                    }
                    test.assertEqual(
                        Iterable.create(
                            "ERROR: The file at \"/project.json\" doesn't exist."),
                        Strings.getLines(output.getText().await()).skipLast());
                    test.assertEqual("", error.getText().await());
                });

                runner.test("with no source files", (Test test) ->
                {
                    final InMemoryCharacterToByteStream output = InMemoryCharacterToByteStream.create();
                    final InMemoryCharacterToByteStream error = InMemoryCharacterToByteStream.create();
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final ProjectJSON projectJSON = ProjectJSON.create();
                    projectJSON.setProject("my-project");
                    projectJSON.setPublisher("me");
                    projectJSON.setVersion("34");
                    projectJSON.setJava(ProjectJSONJava.create());
                    fileSystem.setFileContentAsString("/project.json", projectJSON.toString());
                    fileSystem.setFileContentAsString("/outputs/A.class", "there").await();
                    try (final QubProcess process = QubProcess.create())
                    {
                        process.setOutputWriteStream(output);
                        process.setErrorWriteStream(error);
                        process.setFileSystem(fileSystem);
                        process.setCurrentFolderPathString("/");

                        QubPack.main(process);

                        test.assertEqual(1, process.getExitCode());
                    }
                    test.assertEqual(
                        Iterable.create(
                            "ERROR: No java source files found in /."),
                        Strings.getLines(output.getText().await()).skipLast());
                    test.assertEqual("", error.getText().await());
                });

                runner.test("with simple success", (Test test) ->
                {
                    final InMemoryCharacterToByteStream output = InMemoryCharacterToByteStream.create();
                    final InMemoryCharacterToByteStream error = InMemoryCharacterToByteStream.create();
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    fileSystem.setFileContentAsString("/project.json",
                        ProjectJSON.create()
                            .setProject("my-project")
                            .setPublisher("me")
                            .setVersion("34")
                            .setJava(ProjectJSONJava.create())
                            .toString());
                    fileSystem.setFileContentAsString("/sources/A.java", "hello").await();
                    fileSystem.setFileContentAsString("/outputs/A.class", "there").await();
                    try (final QubProcess process = QubProcess.create())
                    {
                        process.setOutputWriteStream(output);
                        process.setErrorWriteStream(error);
                        process.setFileSystem(fileSystem);
                        process.setCurrentFolderPathString("/");

                        final Folder currentFolder = process.getCurrentFolder();
                        process.setJVMClasspath(currentFolder.getFolder("outputs").await().toString());
                        process.setProcessFactory(new FakeProcessFactory(process.getParallelAsyncRunner(), currentFolder)
                            .add(new FakeJavacProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addOutputFolder(currentFolder.getFolder("outputs").await())
                                .addXlintUnchecked()
                                .addXlintDeprecation()
                                .addClasspath(currentFolder.getFolder("outputs").await().toString())
                                .addSourceFilePathStrings("sources/A.java")
                                .setFunctionAutomatically())
                            .add(new FakeConsoleTestRunnerProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addClasspath(currentFolder.getFolder("outputs").await().toString())
                                .addConsoleTestRunnerFullClassName()
                                .addProfiler(false)
                                .addVerbose(false)
                                .addTestJson(true)
                                .addOutputFolder(currentFolder.getFolder("outputs").await())
                                .addCoverage(Coverage.None)
                                .addFullClassNamesToTest(Iterable.create("A")))
                            .add(new FakeJarProcessRun()
                                .setWorkingFolder(currentFolder.getFolder("sources").await())
                                .addCreate()
                                .addJarFile("my-project.sources.jar")
                                .addContentFilePath("A.java")
                                .setFunctionAutomatically())
                            .add(new FakeJarProcessRun()
                                .setWorkingFolder(currentFolder.getFolder("outputs").await())
                                .addCreate()
                                .addJarFile("my-project.jar")
                                .addContentFilePathStrings(Iterable.create("A.class"))
                                .setFunctionAutomatically()));

                        QubPack.main(process);

                        test.assertEqual("", error.getText().await());
                        test.assertEqual(
                            Iterable.create(
                                "Compiling 1 file...",
                                "Running tests...",
                                "",
                                "Creating sources jar file...",
                                "Creating compiled sources jar file..."),
                            Strings.getLines(output.getText().await()).skipLast());

                        test.assertEqual(0, process.getExitCode());
                    }
                    test.assertEqual(
                        Iterable.create(
                            "Content Files:",
                            "A.java"),
                        Strings.getLines(fileSystem.getFileContentAsString("/outputs/my-project.sources.jar").await()));
                    test.assertEqual(
                        Iterable.create(
                            "Content Files:",
                            "A.class"),
                        Strings.getLines(fileSystem.getFileContentAsString("/outputs/my-project.jar").await()));
                });

                runner.test("with inner class in source file", (Test test) ->
                {
                    final InMemoryCharacterToByteStream output = InMemoryCharacterToByteStream.create();
                    final InMemoryCharacterToByteStream error = InMemoryCharacterToByteStream.create();
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    fileSystem.setFileContentAsString("/project.json",
                        ProjectJSON.create()
                            .setProject("my-project")
                            .setPublisher("me")
                            .setVersion("34")
                            .setJava(ProjectJSONJava.create())
                            .toString());
                    fileSystem.setFileContentAsString("/sources/A.java", "hello").await();
                    fileSystem.setFileContentAsString("/outputs/A.class", "there").await();
                    fileSystem.setFileContentAsString("/outputs/A$B.class", "there").await();
                    try (final QubProcess process = QubProcess.create())
                    {
                        process.setOutputWriteStream(output);
                        process.setErrorWriteStream(error);
                        process.setFileSystem(fileSystem);
                        process.setCurrentFolderPathString("/");

                        final Folder currentFolder = process.getCurrentFolder();
                        process.setJVMClasspath(currentFolder.getFolder("outputs").await().toString());
                        process.setProcessFactory(new FakeProcessFactory(process.getParallelAsyncRunner(), currentFolder)
                            .add(new FakeJavacProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addOutputFolder(currentFolder.getFolder("outputs").await())
                                .addXlintUnchecked()
                                .addXlintDeprecation()
                                .addClasspath(currentFolder.getFolder("outputs").await().toString())
                                .addSourceFilePathStrings("sources/A.java")
                                .setFunctionAutomatically())
                            .add(new FakeConsoleTestRunnerProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addClasspath(currentFolder.getFolder("outputs").await().toString())
                                .addConsoleTestRunnerFullClassName()
                                .addProfiler(false)
                                .addVerbose(false)
                                .addTestJson(true)
                                .addOutputFolder(currentFolder.getFolder("outputs").await())
                                .addCoverage(Coverage.None)
                                .addFullClassNamesToTest(Iterable.create("A$B", "A")))
                            .add(new FakeJarProcessRun()
                                .setWorkingFolder(currentFolder.getFolder("sources").await())
                                .addCreate()
                                .addJarFile("my-project.sources.jar")
                                .addContentFilePath("A.java")
                                .setFunctionAutomatically())
                            .add(new FakeJarProcessRun()
                                .setWorkingFolder(currentFolder.getFolder("outputs").await())
                                .addCreate()
                                .addJarFile("my-project.jar")
                                .addContentFilePathStrings(Iterable.create("A$B.class", "A.class"))
                                .setFunctionAutomatically()));

                        QubPack.main(process);

                        test.assertEqual(
                            Iterable.create(
                                "Compiling 1 file...",
                                "Running tests...",
                                "",
                                "Creating sources jar file...",
                                "Creating compiled sources jar file..."),
                            Strings.getLines(output.getText().await()).skipLast());
                        test.assertEqual("", error.getText().await());

                        test.assertEqual(0, process.getExitCode());
                    }
                    test.assertEqual(
                        Iterable.create(
                            "Content Files:",
                            "A.java"),
                        Strings.getLines(fileSystem.getFileContentAsString("/outputs/my-project.sources.jar").await()));
                    test.assertEqual(
                        Iterable.create(
                            "Content Files:",
                            "A$B.class",
                            "A.class"),
                        Strings.getLines(fileSystem.getFileContentAsString("/outputs/my-project.jar").await()));
                });

                runner.test("with anonymous classes in source file", (Test test) ->
                {
                    final InMemoryCharacterToByteStream output = InMemoryCharacterToByteStream.create();
                    final InMemoryCharacterToByteStream error = InMemoryCharacterToByteStream.create();
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    fileSystem.setFileContentAsString("/project.json",
                        ProjectJSON.create()
                            .setProject("my-project")
                            .setPublisher("me")
                            .setVersion("34")
                            .setJava(ProjectJSONJava.create())
                            .toString());
                    fileSystem.setFileContentAsString("/sources/A.java", "hello").await();
                    fileSystem.setFileContentAsString("/outputs/A.class", "there").await();
                    fileSystem.setFileContentAsString("/outputs/A$1.class", "again").await();
                    fileSystem.setFileContentAsString("/outputs/A$2.class", "you").await();
                    try (final QubProcess process = QubProcess.create())
                    {
                        process.setOutputWriteStream(output);
                        process.setErrorWriteStream(error);
                        process.setFileSystem(fileSystem);
                        process.setCurrentFolderPathString("/");

                        final Folder currentFolder = process.getCurrentFolder();
                        process.setJVMClasspath(currentFolder.getFolder("outputs").await().toString());
                        process.setProcessFactory(new FakeProcessFactory(process.getParallelAsyncRunner(), currentFolder)
                            .add(new FakeJavacProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addOutputFolder(currentFolder.getFolder("outputs").await())
                                .addXlintUnchecked()
                                .addXlintDeprecation()
                                .addClasspath(currentFolder.getFolder("outputs").await().toString())
                                .addSourceFilePathStrings("sources/A.java")
                                .setFunctionAutomatically())
                            .add(new FakeConsoleTestRunnerProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addClasspath(currentFolder.getFolder("outputs").await().toString())
                                .addConsoleTestRunnerFullClassName()
                                .addProfiler(false)
                                .addVerbose(false)
                                .addTestJson(true)
                                .addOutputFolder(currentFolder.getFolder("outputs").await())
                                .addCoverage(Coverage.None)
                                .addFullClassNamesToTest(Iterable.create("A$1", "A$2", "A")))
                            .add(new FakeJarProcessRun()
                                .setWorkingFolder(currentFolder.getFolder("sources").await())
                                .addCreate()
                                .addJarFile("my-project.sources.jar")
                                .addContentFilePath("A.java")
                                .setFunctionAutomatically())
                            .add(new FakeJarProcessRun()
                                .setWorkingFolder(currentFolder.getFolder("outputs").await())
                                .addCreate()
                                .addJarFile("my-project.jar")
                                .addContentFilePathStrings(Iterable.create("A$1.class", "A$2.class", "A.class"))
                                .setFunctionAutomatically()));

                        QubPack.main(process);

                        test.assertEqual(
                            Iterable.create(
                                "Compiling 1 file...",
                                "Running tests...",
                                "",
                                "Creating sources jar file...",
                                "Creating compiled sources jar file..."),
                            Strings.getLines(output.getText().await()).skipLast());
                        test.assertEqual("", error.getText().await());

                        test.assertEqual(0, process.getExitCode());
                    }
                    test.assertEqual(
                        Iterable.create(
                            "Content Files:",
                            "A.java"),
                        Strings.getLines(fileSystem.getFileContentAsString("/outputs/my-project.sources.jar").await()));
                    test.assertEqual(
                        Iterable.create(
                            "Content Files:",
                            "A$1.class",
                            "A$2.class",
                            "A.class"),
                        Strings.getLines(fileSystem.getFileContentAsString("/outputs/my-project.jar").await()));
                });

                runner.test("with main class in project.json", (Test test) ->
                {
                    final InMemoryCharacterToByteStream output = InMemoryCharacterToByteStream.create();
                    final InMemoryCharacterToByteStream error = InMemoryCharacterToByteStream.create();
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final ProjectJSON projectJSON = ProjectJSON.create()
                        .setProject("my-project")
                        .setPublisher("me")
                        .setVersion("34")
                        .setJava(ProjectJSONJava.create()
                            .setMainClass("A"));
                    fileSystem.setFileContentAsString("/project.json", projectJSON.toString());
                    fileSystem.setFileContentAsString("/sources/A.java", "hello").await();
                    fileSystem.setFileContentAsString("/outputs/A.class", "there").await();
                    try (final QubProcess process = QubProcess.create())
                    {
                        process.setOutputWriteStream(output);
                        process.setErrorWriteStream(error);
                        process.setFileSystem(fileSystem);
                        process.setCurrentFolderPathString("/");

                        final Folder currentFolder = process.getCurrentFolder();
                        process.setJVMClasspath(currentFolder.getFolder("outputs").await().toString());
                        process.setProcessFactory(new FakeProcessFactory(process.getParallelAsyncRunner(), currentFolder)
                            .add(new FakeJavacProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addOutputFolder(currentFolder.getFolder("outputs").await())
                                .addXlintUnchecked()
                                .addXlintDeprecation()
                                .addClasspath(currentFolder.getFolder("outputs").await().toString())
                                .addSourceFilePathStrings("sources/A.java")
                                .setFunctionAutomatically())
                            .add(new FakeConsoleTestRunnerProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addClasspath(currentFolder.getFolder("outputs").await().toString())
                                .addConsoleTestRunnerFullClassName()
                                .addProfiler(false)
                                .addVerbose(false)
                                .addTestJson(true)
                                .addOutputFolder(currentFolder.getFolder("outputs").await())
                                .addCoverage(Coverage.None)
                                .addFullClassNamesToTest(Iterable.create("A")))
                            .add(new FakeJarProcessRun()
                                .setWorkingFolder(currentFolder.getFolder("sources").await())
                                .addCreate()
                                .addJarFile("my-project.sources.jar")
                                .addContentFilePath("A.java")
                                .setFunctionAutomatically())
                            .add(new FakeJarProcessRun()
                                .setWorkingFolder(currentFolder.getFolder("outputs").await())
                                .addCreate()
                                .addJarFile("my-project.jar")
                                .addManifestFile("/outputs/META-INF/MANIFEST.MF")
                                .addContentFilePath("A.class")
                                .setFunctionAutomatically()));

                        QubPack.main(process);

                        test.assertEqual(
                            Iterable.create(
                                "Compiling 1 file...",
                                "Running tests...",
                                "",
                                "Creating sources jar file...",
                                "Creating compiled sources jar file..."),
                            Strings.getLines(output.getText().await()).skipLast());
                        test.assertEqual("", error.getText().await());

                        test.assertEqual(0, process.getExitCode());
                    }
                    test.assertEqual(
                        Iterable.create(
                            "Content Files:",
                            "A.java"),
                        Strings.getLines(fileSystem.getFileContentAsString("/outputs/my-project.sources.jar").await()));
                    test.assertEqual(
                        Iterable.create(
                            "Manifest File:",
                            "/outputs/META-INF/MANIFEST.MF",
                            "",
                            "Content Files:",
                            "A.class"),
                        Strings.getLines(fileSystem.getFileContentAsString("/outputs/my-project.jar").await()));
                    test.assertEqual(
                        Iterable.create(
                            "Manifest-Version: 1.0",
                            "Main-Class: A"),
                        Strings.getLines(fileSystem.getFileContentAsString("/outputs/META-INF/MANIFEST.MF").await()));
                });

                runner.test("with simple success and -verbose", (Test test) ->
                {
                    final InMemoryCharacterToByteStream output = InMemoryCharacterToByteStream.create();
                    final InMemoryCharacterToByteStream error = InMemoryCharacterToByteStream.create();
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final ProjectJSON projectJSON = ProjectJSON.create()
                        .setProject("my-project")
                        .setPublisher("me")
                        .setVersion("34")
                        .setJava(ProjectJSONJava.create());
                    fileSystem.setFileContentAsString("/project.json", projectJSON.toString());
                    fileSystem.setFileContentAsString("/sources/A.java", "hello").await();
                    fileSystem.setFileContentAsString("/outputs/A.class", "there").await();
                    try (final QubProcess process = QubProcess.create("-verbose"))
                    {
                        process.setOutputWriteStream(output);
                        process.setErrorWriteStream(error);
                        process.setFileSystem(fileSystem);
                        process.setCurrentFolderPathString("/");

                        final Folder currentFolder = process.getCurrentFolder();
                        process.setJVMClasspath(currentFolder.getFolder("outputs").await().toString());
                        process.setProcessFactory(new FakeProcessFactory(process.getParallelAsyncRunner(), currentFolder)
                            .add(new FakeJavacProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addOutputFolder(currentFolder.getFolder("outputs").await())
                                .addXlintUnchecked()
                                .addXlintDeprecation()
                                .addClasspath(currentFolder.getFolder("outputs").await().toString())
                                .addSourceFilePathStrings("sources/A.java")
                                .setFunctionAutomatically())
                            .add(new FakeConsoleTestRunnerProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addClasspath(currentFolder.getFolder("outputs").await().toString())
                                .addConsoleTestRunnerFullClassName()
                                .addProfiler(false)
                                .addVerbose(true)
                                .addTestJson(true)
                                .addOutputFolder(currentFolder.getFolder("outputs").await())
                                .addCoverage(Coverage.None)
                                .addFullClassNamesToTest(Iterable.create("A")))
                            .add(new FakeJarProcessRun()
                                .setWorkingFolder(currentFolder.getFolder("sources").await())
                                .addCreate()
                                .addJarFile("my-project.sources.jar")
                                .addContentFilePath("A.java")
                                .setFunctionAutomatically())
                            .add(new FakeJarProcessRun()
                                .setWorkingFolder(currentFolder.getFolder("outputs").await())
                                .addCreate()
                                .addJarFile("my-project.jar")
                                .addContentFilePath("A.class")
                                .setFunctionAutomatically())
                            .add(new FakeJarProcessRun()
                                .setWorkingFolder(currentFolder.getFolder("outputs").await())
                                .addCreate()
                                .addJarFile("my-project.tests.jar")
                                .addContentFilePathStrings(Iterable.create("ATests.class"))
                                .setFunctionAutomatically()));

                        QubPack.main(process);

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
                                "VERBOSE: Running /: javac -d outputs -Xlint:unchecked -Xlint:deprecation -classpath /outputs/ sources/A.java...",
                                "VERBOSE: Compilation finished.",
                                "VERBOSE: Writing build.json file...",
                                "VERBOSE: Done writing build.json file.",
                                "Running tests...",
                                "VERBOSE: Running /: java -classpath /outputs/ qub.ConsoleTestRunner --profiler=false --verbose=true --testjson=true --output-folder=/outputs/ --coverage=None A",
                                "",
                                "Creating sources jar file...",
                                "VERBOSE: Running /sources/: jar --create --file=my-project.sources.jar A.java",
                                "VERBOSE: Created /outputs/my-project.sources.jar.",
                                "Creating compiled sources jar file...",
                                "VERBOSE: Running /outputs/: jar --create --file=my-project.jar A.class",
                                "VERBOSE: Created /outputs/my-project.jar."),
                            Strings.getLines(output.getText().await()).skipLast());
                        test.assertEqual("", error.getText().await());

                        test.assertEqual(0, process.getExitCode());
                    }
                    test.assertEqual(
                        Iterable.create(
                            "Content Files:",
                            "A.java"),
                        Strings.getLines(fileSystem.getFileContentAsString("/outputs/my-project.sources.jar").await()));
                    test.assertEqual(
                        Iterable.create(
                            "Content Files:",
                            "A.class"),
                        Strings.getLines(fileSystem.getFileContentAsString("/outputs/my-project.jar").await()));
                });

                runner.test("with test folder", (Test test) ->
                {
                    final InMemoryCharacterToByteStream output = InMemoryCharacterToByteStream.create();
                    final InMemoryCharacterToByteStream error = InMemoryCharacterToByteStream.create();
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final ProjectJSON projectJSON = ProjectJSON.create();
                    projectJSON.setProject("my-project");
                    projectJSON.setPublisher("me");
                    projectJSON.setVersion("34");
                    projectJSON.setJava(ProjectJSONJava.create());
                    fileSystem.setFileContentAsString("/project.json", projectJSON.toString());
                    fileSystem.setFileContentAsString("/sources/A.java", "hello").await();
                    fileSystem.setFileContentAsString("/tests/ATests.java", "hi").await();
                    fileSystem.setFileContentAsString("/outputs/A.class", "there").await();
                    fileSystem.setFileContentAsString("/outputs/ATests.class", "again").await();
                    try (final QubProcess process = QubProcess.create())
                    {
                        process.setOutputWriteStream(output);
                        process.setErrorWriteStream(error);
                        process.setFileSystem(fileSystem);
                        process.setCurrentFolderPathString("/");

                        final Folder currentFolder = process.getCurrentFolder();
                        process.setJVMClasspath(currentFolder.getFolder("outputs").await().toString());
                        process.setProcessFactory(new FakeProcessFactory(process.getParallelAsyncRunner(), currentFolder)
                            .add(new FakeJavacProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addOutputFolder(currentFolder.getFolder("outputs").await())
                                .addXlintUnchecked()
                                .addXlintDeprecation()
                                .addClasspath(currentFolder.getFolder("outputs").await().toString())
                                .addSourceFilePathStrings("sources/A.java", "tests/ATests.java")
                                .setFunctionAutomatically())
                            .add(new FakeConsoleTestRunnerProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addClasspath(currentFolder.getFolder("outputs").await().toString())
                                .addConsoleTestRunnerFullClassName()
                                .addProfiler(false)
                                .addVerbose(false)
                                .addTestJson(true)
                                .addOutputFolder(currentFolder.getFolder("outputs").await())
                                .addCoverage(Coverage.None)
                                .addFullClassNamesToTest(Iterable.create("A", "ATests")))
                            .add(new FakeJarProcessRun()
                                .setWorkingFolder(currentFolder.getFolder("sources").await())
                                .addCreate()
                                .addJarFile("my-project.sources.jar")
                                .addContentFilePath("A.java")
                                .setFunctionAutomatically())
                            .add(new FakeJarProcessRun()
                                .setWorkingFolder(currentFolder.getFolder("outputs").await())
                                .addCreate()
                                .addJarFile("my-project.jar")
                                .addContentFilePath("A.class")
                                .setFunctionAutomatically())
                            .add(new FakeJarProcessRun()
                                .setWorkingFolder(currentFolder.getFolder("outputs").await())
                                .addCreate()
                                .addJarFile("my-project.tests.jar")
                                .addContentFilePathStrings(Iterable.create("ATests.class"))
                                .setFunctionAutomatically()));

                        QubPack.main(process);

                        test.assertEqual(0, process.getExitCode());
                    }
                    test.assertEqual(
                        Iterable.create(
                            "Compiling 2 files...",
                            "Running tests...",
                            "",
                            "Creating sources jar file...",
                            "Creating compiled sources jar file...",
                            "Creating compiled tests jar file..."),
                        Strings.getLines(output.getText().await()).skipLast());
                    test.assertEqual("", error.getText().await());
                    test.assertEqual(
                        Iterable.create(
                            "Content Files:",
                            "A.java"),
                        Strings.getLines(fileSystem.getFileContentAsString("/outputs/my-project.sources.jar").await()));
                    test.assertEqual(
                        Iterable.create(
                            "Content Files:",
                            "A.class"),
                        Strings.getLines(fileSystem.getFileContentAsString("/outputs/my-project.jar").await()));
                    test.assertEqual(
                        Iterable.create(
                            "Content Files:",
                            "ATests.class"),
                        Strings.getLines(fileSystem.getFileContentAsString("/outputs/my-project.tests.jar").await()));
                });

                runner.test("with test folder with inner class", (Test test) ->
                {
                    final InMemoryCharacterToByteStream output = InMemoryCharacterToByteStream.create();
                    final InMemoryCharacterToByteStream error = InMemoryCharacterToByteStream.create();
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final ProjectJSON projectJSON = ProjectJSON.create();
                    projectJSON.setProject("my-project");
                    projectJSON.setPublisher("me");
                    projectJSON.setVersion("34");
                    projectJSON.setJava(ProjectJSONJava.create());
                    fileSystem.setFileContentAsString("/project.json", projectJSON.toString());
                    fileSystem.setFileContentAsString("/sources/A.java", "hello").await();
                    fileSystem.setFileContentAsString("/tests/ATests.java", "hi").await();
                    fileSystem.setFileContentAsString("/outputs/A.class", "there").await();
                    fileSystem.setFileContentAsString("/outputs/ATests.class", "again").await();
                    fileSystem.setFileContentAsString("/outputs/ATests$Inner.class", "again").await();
                    try (final QubProcess process = QubProcess.create())
                    {
                        process.setOutputWriteStream(output);
                        process.setErrorWriteStream(error);
                        process.setFileSystem(fileSystem);
                        process.setCurrentFolderPathString("/");

                        final Folder currentFolder = process.getCurrentFolder();
                        process.setJVMClasspath(currentFolder.getFolder("outputs").await().toString());
                        process.setProcessFactory(new FakeProcessFactory(process.getParallelAsyncRunner(), currentFolder)
                            .add(new FakeJavacProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addOutputFolder(currentFolder.getFolder("outputs").await())
                                .addXlintUnchecked()
                                .addXlintDeprecation()
                                .addClasspath(currentFolder.getFolder("outputs").await().toString())
                                .addSourceFilePathStrings("sources/A.java", "tests/ATests.java")
                                .setFunctionAutomatically())
                            .add(new FakeConsoleTestRunnerProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addClasspath(currentFolder.getFolder("outputs").await().toString())
                                .addConsoleTestRunnerFullClassName()
                                .addProfiler(false)
                                .addVerbose(false)
                                .addTestJson(true)
                                .addOutputFolder(currentFolder.getFolder("outputs").await())
                                .addCoverage(Coverage.None)
                                .addFullClassNamesToTest(Iterable.create("A", "ATests$Inner", "ATests")))
                            .add(new FakeJarProcessRun()
                                .setWorkingFolder(currentFolder.getFolder("sources").await())
                                .addCreate()
                                .addJarFile("my-project.sources.jar")
                                .addContentFilePath("A.java")
                                .setFunctionAutomatically())
                            .add(new FakeJarProcessRun()
                                .setWorkingFolder(currentFolder.getFolder("outputs").await())
                                .addCreate()
                                .addJarFile("my-project.jar")
                                .addContentFilePath("A.class")
                                .setFunctionAutomatically())
                            .add(new FakeJarProcessRun()
                                .setWorkingFolder(currentFolder.getFolder("outputs").await())
                                .addCreate()
                                .addJarFile("my-project.tests.jar")
                                .addContentFilePathStrings(Iterable.create("ATests$Inner.class", "ATests.class"))
                                .setFunctionAutomatically()));

                        QubPack.main(process);

                        test.assertEqual(0, process.getExitCode());
                    }
                    test.assertEqual(
                        Iterable.create(
                            "Compiling 2 files...",
                            "Running tests...",
                            "",
                            "Creating sources jar file...",
                            "Creating compiled sources jar file...",
                            "Creating compiled tests jar file..."),
                        Strings.getLines(output.getText().await()).skipLast());
                    test.assertEqual("", error.getText().await());
                    test.assertEqual(
                        Iterable.create(
                            "Content Files:",
                            "A.java"),
                        Strings.getLines(fileSystem.getFileContentAsString("/outputs/my-project.sources.jar").await()));
                    test.assertEqual(
                        Iterable.create(
                            "Content Files:",
                            "A.class"),
                        Strings.getLines(fileSystem.getFileContentAsString("/outputs/my-project.jar").await()));
                    test.assertEqual(
                        Iterable.create(
                            "Content Files:",
                            "ATests$Inner.class",
                            "ATests.class"),
                        Strings.getLines(fileSystem.getFileContentAsString("/outputs/my-project.tests.jar").await()));
                });

                runner.test("with test folder with anonymous class", (Test test) ->
                {
                    final InMemoryCharacterToByteStream output = InMemoryCharacterToByteStream.create();
                    final InMemoryCharacterToByteStream error = InMemoryCharacterToByteStream.create();
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final ProjectJSON projectJSON = ProjectJSON.create();
                    projectJSON.setProject("my-project");
                    projectJSON.setPublisher("me");
                    projectJSON.setVersion("34");
                    projectJSON.setJava(ProjectJSONJava.create());
                    fileSystem.setFileContentAsString("/project.json", projectJSON.toString());
                    fileSystem.setFileContentAsString("/sources/A.java", "hello").await();
                    fileSystem.setFileContentAsString("/tests/ATests.java", "hi").await();
                    fileSystem.setFileContentAsString("/outputs/A.class", "there").await();
                    fileSystem.setFileContentAsString("/outputs/ATests.class", "again").await();
                    fileSystem.setFileContentAsString("/outputs/ATests$1.class", "again").await();
                    try (final QubProcess process = QubProcess.create())
                    {
                        process.setOutputWriteStream(output);
                        process.setErrorWriteStream(error);
                        process.setFileSystem(fileSystem);
                        process.setCurrentFolderPathString("/");

                        final Folder currentFolder = process.getCurrentFolder();
                        process.setJVMClasspath(currentFolder.getFolder("outputs").await().toString());
                        process.setProcessFactory(new FakeProcessFactory(process.getParallelAsyncRunner(), currentFolder)
                            .add(new FakeJavacProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addOutputFolder(currentFolder.getFolder("outputs").await())
                                .addXlintUnchecked()
                                .addXlintDeprecation()
                                .addClasspath(currentFolder.getFolder("outputs").await().toString())
                                .addSourceFilePathStrings("sources/A.java", "tests/ATests.java")
                                .setFunctionAutomatically())
                            .add(new FakeConsoleTestRunnerProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addClasspath(currentFolder.getFolder("outputs").await().toString())
                                .addConsoleTestRunnerFullClassName()
                                .addProfiler(false)
                                .addVerbose(false)
                                .addTestJson(true)
                                .addOutputFolder(currentFolder.getFolder("outputs").await())
                                .addCoverage(Coverage.None)
                                .addFullClassNamesToTest(Iterable.create("A", "ATests$1", "ATests")))
                            .add(new FakeJarProcessRun()
                                .setWorkingFolder(currentFolder.getFolder("sources").await())
                                .addCreate()
                                .addJarFile("my-project.sources.jar")
                                .addContentFilePath("A.java")
                                .setFunctionAutomatically())
                            .add(new FakeJarProcessRun()
                                .setWorkingFolder(currentFolder.getFolder("outputs").await())
                                .addCreate()
                                .addJarFile("my-project.jar")
                                .addContentFilePath("A.class")
                                .setFunctionAutomatically())
                            .add(new FakeJarProcessRun()
                                .setWorkingFolder(currentFolder.getFolder("outputs").await())
                                .addCreate()
                                .addJarFile("my-project.tests.jar")
                                .addContentFilePathStrings(Iterable.create("ATests$1.class", "ATests.class"))
                                .setFunctionAutomatically()));

                        QubPack.main(process);

                        test.assertEqual(
                            Iterable.create(
                                "Compiling 2 files...",
                                "Running tests...",
                                "",
                                "Creating sources jar file...",
                                "Creating compiled sources jar file...",
                                "Creating compiled tests jar file..."),
                            Strings.getLines(output.getText().await()).skipLast());

                        test.assertEqual(0, process.getExitCode());
                    }
                    test.assertEqual("", error.getText().await());
                    test.assertEqual(
                        Iterable.create(
                            "Content Files:",
                            "A.java"),
                        Strings.getLines(fileSystem.getFileContentAsString("/outputs/my-project.sources.jar").await()));
                    test.assertEqual(
                        Iterable.create(
                            "Content Files:",
                            "A.class"),
                        Strings.getLines(fileSystem.getFileContentAsString("/outputs/my-project.jar").await()));
                    test.assertEqual(
                        Iterable.create(
                            "Content Files:",
                            "ATests$1.class",
                            "ATests.class"),
                        Strings.getLines(fileSystem.getFileContentAsString("/outputs/my-project.tests.jar").await()));
                });

                runner.test("with --packjson=false", (Test test) ->
                {
                    final InMemoryCharacterToByteStream output = InMemoryCharacterToByteStream.create();
                    final InMemoryCharacterToByteStream error = InMemoryCharacterToByteStream.create();
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    fileSystem.setFileContentAsString("/project.json", ProjectJSON.create()
                        .setProject("my-project")
                        .setPublisher("me")
                        .setVersion("34")
                        .setJava(ProjectJSONJava.create())
                        .toString());
                    fileSystem.setFileContentAsString("/sources/A.java", "hello").await();
                    fileSystem.setFileContentAsString("/tests/ATests.java", "hi").await();
                    fileSystem.setFileContentAsString("/outputs/A.class", "there").await();
                    fileSystem.setFileContentAsString("/outputs/ATests.class", "again").await();
                    try (final QubProcess process = QubProcess.create("--packjson=false"))
                    {
                        process.setOutputWriteStream(output);
                        process.setErrorWriteStream(error);
                        process.setFileSystem(fileSystem);
                        process.setCurrentFolderPathString("/");

                        final Folder currentFolder = process.getCurrentFolder();
                        process.setJVMClasspath(currentFolder.getFolder("outputs").await().toString());
                        process.setProcessFactory(new FakeProcessFactory(process.getParallelAsyncRunner(), currentFolder)
                            .add(new FakeJavacProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addOutputFolder(currentFolder.getFolder("outputs").await())
                                .addXlintUnchecked()
                                .addXlintDeprecation()
                                .addClasspath(currentFolder.getFolder("outputs").await().toString())
                                .addSourceFilePathStrings("sources/A.java", "tests/ATests.java")
                                .setFunctionAutomatically())
                            .add(new FakeConsoleTestRunnerProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addClasspath(currentFolder.getFolder("outputs").await().toString())
                                .addConsoleTestRunnerFullClassName()
                                .addProfiler(false)
                                .addVerbose(false)
                                .addTestJson(true)
                                .addOutputFolder(currentFolder.getFolder("outputs").await())
                                .addCoverage(Coverage.None)
                                .addFullClassNamesToTest(Iterable.create("A", "ATests")))
                            .add(new FakeJarProcessRun()
                                .setWorkingFolder(currentFolder.getFolder("sources").await())
                                .addCreate()
                                .addJarFile("my-project.sources.jar")
                                .addContentFilePath("A.java")
                                .setFunctionAutomatically())
                            .add(new FakeJarProcessRun()
                                .setWorkingFolder(currentFolder.getFolder("outputs").await())
                                .addCreate()
                                .addJarFile("my-project.jar")
                                .addContentFilePath("A.class")
                                .setFunctionAutomatically())
                            .add(new FakeJarProcessRun()
                                .setWorkingFolder(currentFolder.getFolder("outputs").await())
                                .addCreate()
                                .addJarFile("my-project.tests.jar")
                                .addContentFilePathStrings(Iterable.create("ATests.class"))
                                .setFunctionAutomatically()));

                        QubPack.main(process);

                        test.assertEqual(
                            Iterable.create(
                                "Compiling 2 files...",
                                "Running tests...",
                                "",
                                "Creating sources jar file...",
                                "Creating compiled sources jar file...",
                                "Creating compiled tests jar file..."),
                            Strings.getLines(output.getText().await()).skipLast());

                        test.assertEqual(0, process.getExitCode());
                    }
                    test.assertEqual("", error.getText().await());
                    test.assertEqual(
                        Iterable.create(
                            "Content Files:",
                            "A.java"),
                        Strings.getLines(fileSystem.getFileContentAsString("/outputs/my-project.sources.jar").await()));
                    test.assertEqual(
                        Iterable.create(
                            "Content Files:",
                            "A.class"),
                        Strings.getLines(fileSystem.getFileContentAsString("/outputs/my-project.jar").await()));
                    test.assertEqual(
                        Iterable.create(
                            "Content Files:",
                            "ATests.class"),
                        Strings.getLines(fileSystem.getFileContentAsString("/outputs/my-project.tests.jar").await()));
                });

                runner.test("with --packjson=true but no pack.json file", (Test test) ->
                {
                    final InMemoryCharacterToByteStream output = InMemoryCharacterToByteStream.create();
                    final InMemoryCharacterToByteStream error = InMemoryCharacterToByteStream.create();
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    fileSystem.setFileContentAsString("/project.json", ProjectJSON.create()
                        .setProject("my-project")
                        .setPublisher("me")
                        .setVersion("34")
                        .setJava(ProjectJSONJava.create())
                        .toString());
                    fileSystem.setFileContentAsString("/sources/A.java", "hello").await();
                    fileSystem.setFileContentAsString("/tests/ATests.java", "hi").await();
                    fileSystem.setFileContentAsString("/outputs/A.class", "there").await();
                    fileSystem.setFileContentAsString("/outputs/ATests.class", "again").await();
                    try (final QubProcess process = QubProcess.create("--packjson=true"))
                    {
                        process.setOutputWriteStream(output);
                        process.setErrorWriteStream(error);
                        process.setFileSystem(fileSystem);
                        process.setCurrentFolderPathString("/");

                        final Folder currentFolder = process.getCurrentFolder();
                        process.setJVMClasspath(currentFolder.getFolder("outputs").await().toString());
                        process.setProcessFactory(new FakeProcessFactory(process.getParallelAsyncRunner(), currentFolder)
                            .add(new FakeJavacProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addOutputFolder(currentFolder.getFolder("outputs").await())
                                .addXlintUnchecked()
                                .addXlintDeprecation()
                                .addClasspath(currentFolder.getFolder("outputs").await().toString())
                                .addSourceFilePathStrings("sources/A.java", "tests/ATests.java")
                                .setFunctionAutomatically())
                            .add(new FakeConsoleTestRunnerProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addClasspath(currentFolder.getFolder("outputs").await().toString())
                                .addConsoleTestRunnerFullClassName()
                                .addProfiler(false)
                                .addVerbose(false)
                                .addTestJson(true)
                                .addOutputFolder(currentFolder.getFolder("outputs").await())
                                .addCoverage(Coverage.None)
                                .addFullClassNamesToTest(Iterable.create("A", "ATests")))
                            .add(new FakeJarProcessRun()
                                .setWorkingFolder(currentFolder.getFolder("sources").await())
                                .addCreate()
                                .addJarFile("my-project.sources.jar")
                                .addContentFilePath("A.java")
                                .setFunctionAutomatically())
                            .add(new FakeJarProcessRun()
                                .setWorkingFolder(currentFolder.getFolder("outputs").await())
                                .addCreate()
                                .addJarFile("my-project.jar")
                                .addContentFilePath("A.class")
                                .setFunctionAutomatically())
                            .add(new FakeJarProcessRun()
                                .setWorkingFolder(currentFolder.getFolder("outputs").await())
                                .addCreate()
                                .addJarFile("my-project.tests.jar")
                                .addContentFilePathStrings(Iterable.create("ATests.class"))
                                .setFunctionAutomatically()));

                        QubPack.main(process);

                        test.assertEqual(
                            Iterable.create(
                                "Compiling 2 files...",
                                "Running tests...",
                                "",
                                "Creating sources jar file...",
                                "Creating compiled sources jar file...",
                                "Creating compiled tests jar file..."),
                            Strings.getLines(output.getText().await()).skipLast());

                        test.assertEqual(0, process.getExitCode());
                    }
                    test.assertEqual("", error.getText().await());
                    test.assertEqual(
                        Iterable.create(
                            "Content Files:",
                            "A.java"),
                        Strings.getLines(fileSystem.getFileContentAsString("/outputs/my-project.sources.jar").await()));
                    test.assertEqual(
                        Iterable.create(
                            "Content Files:",
                            "A.class"),
                        Strings.getLines(fileSystem.getFileContentAsString("/outputs/my-project.jar").await()));
                    test.assertEqual(
                        Iterable.create(
                            "Content Files:",
                            "ATests.class"),
                        Strings.getLines(fileSystem.getFileContentAsString("/outputs/my-project.tests.jar").await()));
                    test.assertEqual(
                        new PackJSON()
                            .setSourceFiles(Iterable.create(
                                new PackJSONFile()
                                    .setRelativePath("A.java")
                                    .setLastModified(fileSystem.getFileLastModified("/sources/A.java").await())))
                            .setSourceOutputFiles(Iterable.create(
                                new PackJSONFile()
                                    .setRelativePath("A.class")
                                    .setLastModified(fileSystem.getFileLastModified("/outputs/A.class").await())))
                            .setTestOutputFiles(Iterable.create(
                                new PackJSONFile()
                                    .setRelativePath("ATests.class")
                                    .setLastModified(fileSystem.getFileLastModified("/outputs/ATests.class").await())))
                            .toString(JSONFormat.pretty),
                        fileSystem.getFileContentAsString("/outputs/pack.json").await());
                });

                runner.test("with --packjson=true and empty pack.json file", (Test test) ->
                {
                    final InMemoryCharacterToByteStream output = InMemoryCharacterToByteStream.create();
                    final InMemoryCharacterToByteStream error = InMemoryCharacterToByteStream.create();
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    fileSystem.setFileContentAsString("/project.json", ProjectJSON.create()
                        .setProject("my-project")
                        .setPublisher("me")
                        .setVersion("34")
                        .setJava(ProjectJSONJava.create())
                        .toString()).await();
                    fileSystem.setFileContentAsString("/sources/A.java", "hello").await();
                    fileSystem.setFileContentAsString("/tests/ATests.java", "hi").await();
                    fileSystem.setFileContentAsString("/outputs/A.class", "there").await();
                    fileSystem.setFileContentAsString("/outputs/ATests.class", "again").await();

                    fileSystem.setFileContentAsString("/outputs/pack.json", new PackJSON()
                        .toString()).await();

                    try (final QubProcess process = QubProcess.create("--packjson=true"))
                    {
                        process.setOutputWriteStream(output);
                        process.setErrorWriteStream(error);
                        process.setFileSystem(fileSystem);
                        process.setCurrentFolderPathString("/");

                        final Folder currentFolder = process.getCurrentFolder();
                        process.setJVMClasspath(currentFolder.getFolder("outputs").await().toString());
                        process.setProcessFactory(new FakeProcessFactory(process.getParallelAsyncRunner(), currentFolder)
                            .add(new FakeJavacProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addOutputFolder(currentFolder.getFolder("outputs").await())
                                .addXlintUnchecked()
                                .addXlintDeprecation()
                                .addClasspath(currentFolder.getFolder("outputs").await().toString())
                                .addSourceFilePathStrings("sources/A.java", "tests/ATests.java")
                                .setFunctionAutomatically())
                            .add(new FakeConsoleTestRunnerProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addClasspath("/outputs/")
                                .addConsoleTestRunnerFullClassName()
                                .addProfiler(false)
                                .addVerbose(false)
                                .addTestJson(true)
                                .addOutputFolder(currentFolder.getFolder("outputs").await())
                                .addCoverage(Coverage.None)
                                .addFullClassNamesToTest(Iterable.create("A", "ATests")))
                            .add(new FakeJarProcessRun()
                                .setWorkingFolder(currentFolder.getFolder("sources").await())
                                .addCreate()
                                .addJarFile("my-project.sources.jar")
                                .addContentFilePath("A.java")
                                .setFunctionAutomatically())
                            .add(new FakeJarProcessRun()
                                .setWorkingFolder(currentFolder.getFolder("outputs").await())
                                .addCreate()
                                .addJarFile("my-project.jar")
                                .addContentFilePath("A.class")
                                .setFunctionAutomatically())
                            .add(new FakeJarProcessRun()
                                .setWorkingFolder(currentFolder.getFolder("outputs").await())
                                .addCreate()
                                .addJarFile("my-project.tests.jar")
                                .addContentFilePathStrings(Iterable.create("ATests.class"))
                                .setFunctionAutomatically()));

                        QubPack.main(process);

                        test.assertEqual(
                            Iterable.create(
                                "Compiling 2 files...",
                                "Running tests...",
                                "",
                                "Creating sources jar file...",
                                "Creating compiled sources jar file...",
                                "Creating compiled tests jar file..."),
                            Strings.getLines(output.getText().await()).skipLast());

                        test.assertEqual(0, process.getExitCode());
                    }
                    test.assertEqual("", error.getText().await());
                    test.assertEqual(
                        Iterable.create(
                            "Content Files:",
                            "A.java"),
                        Strings.getLines(fileSystem.getFileContentAsString("/outputs/my-project.sources.jar").await()));
                    test.assertEqual(
                        Iterable.create(
                            "Content Files:",
                            "A.class"),
                        Strings.getLines(fileSystem.getFileContentAsString("/outputs/my-project.jar").await()));
                    test.assertEqual(
                        Iterable.create(
                            "Content Files:",
                            "ATests.class"),
                        Strings.getLines(fileSystem.getFileContentAsString("/outputs/my-project.tests.jar").await()));
                    test.assertEqual(
                        new PackJSON()
                            .setSourceFiles(Iterable.create(
                                new PackJSONFile()
                                    .setRelativePath("A.java")
                                    .setLastModified(fileSystem.getFileLastModified("/sources/A.java").await())))
                            .setSourceOutputFiles(Iterable.create(
                                new PackJSONFile()
                                    .setRelativePath("A.class")
                                    .setLastModified(fileSystem.getFileLastModified("/outputs/A.class").await())))
                            .setTestOutputFiles(Iterable.create(
                                new PackJSONFile()
                                    .setRelativePath("ATests.class")
                                    .setLastModified(fileSystem.getFileLastModified("/outputs/ATests.class").await())))
                            .toString(JSONFormat.pretty),
                        fileSystem.getFileContentAsString("/outputs/pack.json").await());
                });
            });
        });
    }
}
