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
                    test.assertThrows(new PreConditionFailure("arguments cannot be null."),
                        () -> QubPack.main(null));
                });
            });

            runner.testGroup("getParameters(FakeDesktopProcess)", () ->
            {
                runner.test("with null process", (Test test) ->
                {
                    test.assertThrows(() -> QubPack.getParameters(null),
                        new PreConditionFailure("process cannot be null."));
                });

                runner.test("with -?",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess("-?")),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final QubPackParameters parameters = QubPack.getParameters(process);
                    test.assertNull(parameters);

                    test.assertEqual(-1, process.getExitCode());
                    test.assertEqual(
                        Iterable.create(
                            "Usage: qub-pack [[--folder=]<folder-to-pack>] [--packjson] [--parallelpack] [--testjson] [--buildjson] [--warnings=<show|error|hide>] [--verbose] [--profiler] [--help]",
                            "  Used to package source and compiled code in source code projects.",
                            "  --folder:       The folder to pack. Defaults to the current folder.",
                            "  --packjson:     Whether or not to read and write a pack.json file. Defaults to true.",
                            "  --parallelpack: Whether or not the jar files will be packaged in parallel. Defaults to true.",
                            "  --testjson:     Whether or not to write the test results to a test.json file.",
                            "  --buildjson:    Whether or not to read and write a build.json file. Defaults to true.",
                            "  --warnings:     How to handle build warnings. Can be either \"show\", \"error\", or \"hide\". Defaults to \"show\".",
                            "  --verbose(v):   Whether or not to show verbose logs.",
                            "  --profiler:     Whether or not this application should pause before it is run to allow a profiler to be attached.",
                            "  --help(?):      Show the help message for this application."),
                        Strings.getLines(process.getOutputWriteStream().getText().await()));
                    test.assertEqual(
                        Iterable.create(),
                        Strings.getLines(process.getErrorWriteStream().getText().await()));
                });

                runner.test("with no command line arguments",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final Folder currentFolder = process.getCurrentFolder();

                    final QubFolder qubFolder = process.getQubFolder().await();
                    final QubProjectFolder qubBuildProjectFolder = qubFolder.getProjectFolder("qub", "build-java").await();
                    final File qubBuildCompiledSourcesFile = qubBuildProjectFolder.getCompiledSourcesFile("7").await();
                    qubBuildCompiledSourcesFile.create().await();
                    final Folder qubBuildDataFolder = qubBuildProjectFolder.getProjectDataFolder().await();
                    final QubProjectFolder qubTestProjectFolder = qubFolder.getProjectFolder("qub", "test-java").await();
                    final File qubTestCompiledSourcesFile = qubTestProjectFolder.getCompiledSourcesFile("8").await();
                    qubTestCompiledSourcesFile.create().await();
                    final Folder qubTestDataFolder = qubTestProjectFolder.getProjectDataFolder().await();

                    process.getTypeLoader()
                        .addTypeContainer(QubBuild.class, qubBuildCompiledSourcesFile)
                        .addTypeContainer(QubTest.class, qubTestCompiledSourcesFile);

                    final QubPackParameters parameters = QubPack.getParameters(process);
                    test.assertNotNull(parameters);
                    test.assertTrue(parameters.getBuildJson());
                    test.assertEqual(Coverage.None, parameters.getCoverage());
                    test.assertSame(process.getDefaultApplicationLauncher(), parameters.getDefaultApplicationLauncher());
                    test.assertSame(process.getEnvironmentVariables(), parameters.getEnvironmentVariables());
                    test.assertSame(process.getErrorWriteStream(), parameters.getErrorWriteStream());
                    test.assertEqual(currentFolder, parameters.getFolderToBuild());
                    test.assertEqual(currentFolder, parameters.getFolderToPack());
                    test.assertEqual(currentFolder, parameters.getFolderToTest());
                    test.assertEqual(process.getJVMClasspath().await(), parameters.getJvmClassPath());
                    test.assertSame(process.getOutputWriteStream(), parameters.getOutputWriteStream());
                    test.assertNull(parameters.getPattern());
                    test.assertTrue(parameters.getPackJson());
                    test.assertTrue(parameters.getParallelPack());
                    test.assertSame(process.getProcessFactory(), parameters.getProcessFactory());
                    test.assertFalse(parameters.getProfiler());
                    test.assertEqual(qubBuildDataFolder, parameters.getQubBuildDataFolder());
                    test.assertEqual(qubTestDataFolder, parameters.getQubTestDataFolder());
                    test.assertTrue(parameters.getTestJson());
                    final VerboseCharacterToByteWriteStream verbose = parameters.getVerbose();
                    test.assertNotNull(verbose);
                    test.assertFalse(verbose.isVerbose());
                    test.assertEqual(Warnings.Show, parameters.getWarnings());

                    test.assertEqual("", process.getOutputWriteStream().getText().await());
                    test.assertEqual("", process.getErrorWriteStream().getText().await());
                });
            });

            runner.testGroup("run(QubPackParameters)", () ->
            {
                runner.test("with null parameters", (Test test) ->
                {
                    test.assertThrows(() -> QubPack.run(null),
                        new PreConditionFailure("parameters cannot be null."));
                });

                runner.test("with no project.json file",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final QubPackParameters parameters = QubPackTests.getParameters(process);

                    final int exitCode = QubPack.run(parameters);

                    test.assertLinesEqual(
                        Iterable.create(
                            "ERROR: The file at \"/project.json\" doesn't exist."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(1, exitCode);
                });

                runner.test("with no source files",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final Folder currentFolder = process.getCurrentFolder();
                    final File projectJsonFile = currentFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(
                        ProjectJSON.create()
                            .setJava(ProjectJSONJava.create())
                            .toString())
                        .await();
                    final QubPackParameters parameters = QubPackTests.getParameters(process);

                    final int exitCode = QubPack.run(parameters);

                    test.assertLinesEqual(
                        Iterable.create(
                            "ERROR: No java source files found in /."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(1, exitCode);
                });

                runner.test("with simple success",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final QubProjectFolder qubTestProjectFolder = qubFolder.getProjectFolder("qub", "test-java").await();
                    final File qubTestLogFile = qubTestProjectFolder.getProjectDataFolder().await()
                        .getFile("logs/1.log").await();
                    final Folder currentFolder = process.getCurrentFolder();
                    final File projectJsonFile = currentFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(
                        ProjectJSON.create()
                            .setPublisher("me")
                            .setProject("a")
                            .setVersion("34")
                            .setJava(ProjectJSONJava.create())
                            .toString())
                        .await();
                    final Folder sourcesFolder = currentFolder.getFolder("sources").await();
                    final File aJavaFile = sourcesFolder.getFile("A.java").await();
                    aJavaFile.setContentsAsString("A.java source").await();
                    final Folder outputsFolder = currentFolder.getFolder("outputs").await();
                    final File aClassFile = outputsFolder.getFile("A.class").await();
                    aClassFile.setContentsAsString("A.java bytecode").await();
                    final File aSourcesJarFile = outputsFolder.getFile("a.sources.jar").await();
                    final File aJarFile = outputsFolder.getFile("a.jar").await();
                    final String jvmClassPath = "/fake-jvm-classpath";
                    process.getProcessFactory()
                        .add(new FakeJavacProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addVersion()
                            .setVersionFunctionAutomatically("javac 14.0.1"))
                        .add(new FakeJavacProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addOutputFolder(outputsFolder)
                            .addXlintUnchecked()
                            .addXlintDeprecation()
                            .addClasspath(outputsFolder)
                            .addSourceFile(aJavaFile.relativeTo(currentFolder))
                            .setCompileFunctionAutomatically())
                        .add(new FakeConsoleTestRunnerProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addClasspath(Iterable.create(outputsFolder.toString(), jvmClassPath))
                            .addConsoleTestRunnerFullClassName()
                            .addProfiler(false)
                            .addVerbose(false)
                            .addTestJson(true)
                            .addLogFile(qubTestLogFile)
                            .addOutputFolder(outputsFolder)
                            .addCoverage(Coverage.None)
                            .addFullClassNamesToTest(Iterable.create("A")))
                        .add(new FakeJarProcessRun()
                            .setWorkingFolder(sourcesFolder)
                            .addCreate()
                            .addJarFile(aSourcesJarFile.relativeTo(outputsFolder))
                            .addContentFilePath(aJavaFile.relativeTo(sourcesFolder))
                            .setFunctionAutomatically())
                        .add(new FakeJarProcessRun()
                            .setWorkingFolder(outputsFolder)
                            .addCreate()
                            .addJarFile(aJarFile.relativeTo(outputsFolder))
                            .addContentFilePaths(Iterable.create(aClassFile.relativeTo(outputsFolder)))
                            .setFunctionAutomatically());
                    final QubPackParameters parameters = QubPackTests.getParameters(process, jvmClassPath);

                    final int exitCode = QubPack.run(parameters);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Compiling 1 file...",
                            "Running tests...",
                            "",
                            "Creating sources jar file...",
                            "Creating compiled sources jar file..."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(0, exitCode);

                    test.assertEqual(
                        Iterable.create(
                            "Content Files:",
                            "A.java"),
                        Strings.getLines(aSourcesJarFile.getContentsAsString().await()));
                    test.assertEqual(
                        Iterable.create(
                            "Content Files:",
                            "A.class"),
                        Strings.getLines(aJarFile.getContentsAsString().await()));
                });

                runner.test("with simple success and verbose",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final QubProjectFolder qubTestProjectFolder = qubFolder.getProjectFolder("qub", "test-java").await();
                    final File qubTestLogFile = qubTestProjectFolder.getProjectDataFolder().await()
                        .getFile("logs/1.log").await();
                    final Folder currentFolder = process.getCurrentFolder();
                    final File projectJsonFile = currentFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(
                        ProjectJSON.create()
                            .setPublisher("me")
                            .setProject("a")
                            .setVersion("34")
                            .setJava(ProjectJSONJava.create())
                            .toString())
                        .await();
                    final Folder sourcesFolder = currentFolder.getFolder("sources").await();
                    final File aJavaFile = sourcesFolder.getFile("A.java").await();
                    aJavaFile.setContentsAsString("A.java source").await();
                    final Folder outputsFolder = currentFolder.getFolder("outputs").await();
                    final File aClassFile = outputsFolder.getFile("A.class").await();
                    aClassFile.setContentsAsString("A.java bytecode").await();
                    final File aSourcesJarFile = outputsFolder.getFile("a.sources.jar").await();
                    final File aJarFile = outputsFolder.getFile("a.jar").await();
                    final String jvmClassPath = "/fake-jvm-classpath";
                    process.getProcessFactory()
                        .add(new FakeJavacProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addVersion()
                            .setVersionFunctionAutomatically("javac 14.0.1"))
                        .add(new FakeJavacProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addOutputFolder(outputsFolder)
                            .addXlintUnchecked()
                            .addXlintDeprecation()
                            .addClasspath(outputsFolder)
                            .addSourceFile(aJavaFile.relativeTo(currentFolder))
                            .setCompileFunctionAutomatically())
                        .add(new FakeConsoleTestRunnerProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addClasspath(Iterable.create(outputsFolder.toString(), jvmClassPath))
                            .addConsoleTestRunnerFullClassName()
                            .addProfiler(false)
                            .addVerbose(true)
                            .addTestJson(true)
                            .addLogFile(qubTestLogFile)
                            .addOutputFolder(outputsFolder)
                            .addCoverage(Coverage.None)
                            .addFullClassNamesToTest(Iterable.create("A")))
                        .add(new FakeJarProcessRun()
                            .setWorkingFolder(sourcesFolder)
                            .addCreate()
                            .addJarFile(aSourcesJarFile.relativeTo(outputsFolder))
                            .addContentFilePath(aJavaFile.relativeTo(sourcesFolder))
                            .setFunctionAutomatically())
                        .add(new FakeJarProcessRun()
                            .setWorkingFolder(outputsFolder)
                            .addCreate()
                            .addJarFile(aJarFile.relativeTo(outputsFolder))
                            .addContentFilePaths(Iterable.create(aClassFile.relativeTo(outputsFolder)))
                            .setFunctionAutomatically());
                    final QubPackParameters parameters = QubPackTests.getParameters(process, jvmClassPath)
                        .setVerbose(VerboseCharacterToByteWriteStream.create(process.getOutputWriteStream()));

                    final int exitCode = QubPack.run(parameters);

                    test.assertLinesEqual(
                        Iterable.create(
                            "VERBOSE: Parsing project.json...",
                            "VERBOSE: Getting javac version...",
                            "VERBOSE: Running /: javac --version...",
                            "VERBOSE: javac 14.0.1",
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
                            "VERBOSE: Running /: java -classpath /outputs/;/fake-jvm-classpath qub.ConsoleTestRunner --profiler=false --verbose=true --testjson=true --logfile=/qub/qub/test-java/data/logs/1.log --output-folder=/outputs/ --coverage=None A",
                            "",
                            "Creating sources jar file...",
                            "VERBOSE: Running /sources/: jar --create --file=a.sources.jar A.java",
                            "VERBOSE: Created /outputs/a.sources.jar.",
                            "Creating compiled sources jar file...",
                            "VERBOSE: Running /outputs/: jar --create --file=a.jar A.class",
                            "VERBOSE: Created /outputs/a.jar."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(0, exitCode);

                    test.assertEqual(
                        Iterable.create(
                            "Content Files:",
                            "A.java"),
                        Strings.getLines(aSourcesJarFile.getContentsAsString().await()));
                    test.assertEqual(
                        Iterable.create(
                            "Content Files:",
                            "A.class"),
                        Strings.getLines(aJarFile.getContentsAsString().await()));
                });

                runner.test("with inner class in source file",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final QubProjectFolder qubTestProjectFolder = qubFolder.getProjectFolder("qub", "test-java").await();
                    final File qubTestLogFile = qubTestProjectFolder.getProjectDataFolder().await()
                        .getFile("logs/1.log").await();
                    final Folder currentFolder = process.getCurrentFolder();
                    final File projectJsonFile = currentFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(
                        ProjectJSON.create()
                            .setPublisher("me")
                            .setProject("a")
                            .setVersion("34")
                            .setJava(ProjectJSONJava.create())
                            .toString())
                        .await();
                    final Folder sourcesFolder = currentFolder.getFolder("sources").await();
                    final File aJavaFile = sourcesFolder.getFile("A.java").await();
                    aJavaFile.setContentsAsString("A.java source").await();
                    final Folder outputsFolder = currentFolder.getFolder("outputs").await();
                    final File aClassFile = outputsFolder.getFile("A.class").await();
                    aClassFile.setContentsAsString("A.java bytecode").await();
                    final File abClassFile = outputsFolder.getFile("A$B.class").await();
                    final File aSourcesJarFile = outputsFolder.getFile("a.sources.jar").await();
                    final File aJarFile = outputsFolder.getFile("a.jar").await();
                    abClassFile.setContentsAsString("A$B bytecode").await();
                    final String jvmClassPath = "/fake-jvm-classpath";
                    process.getProcessFactory()
                        .add(new FakeJavacProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addVersion()
                            .setVersionFunctionAutomatically("javac 14.0.1"))
                        .add(new FakeJavacProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addOutputFolder(outputsFolder)
                            .addXlintUnchecked()
                            .addXlintDeprecation()
                            .addClasspath(outputsFolder)
                            .addSourceFile(aJavaFile.relativeTo(currentFolder))
                            .setCompileFunctionAutomatically())
                        .add(new FakeConsoleTestRunnerProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addClasspath(Iterable.create(outputsFolder.toString(), jvmClassPath))
                            .addConsoleTestRunnerFullClassName()
                            .addProfiler(false)
                            .addVerbose(false)
                            .addTestJson(true)
                            .addLogFile(qubTestLogFile)
                            .addOutputFolder(outputsFolder)
                            .addCoverage(Coverage.None)
                            .addFullClassNamesToTest(Iterable.create("A")))
                        .add(new FakeJarProcessRun()
                            .setWorkingFolder(sourcesFolder)
                            .addCreate()
                            .addJarFile(aSourcesJarFile.relativeTo(outputsFolder))
                            .addContentFilePath(aJavaFile.relativeTo(sourcesFolder))
                            .setFunctionAutomatically())
                        .add(new FakeJarProcessRun()
                            .setWorkingFolder(outputsFolder)
                            .addCreate()
                            .addJarFile(aJarFile.relativeTo(outputsFolder))
                            .addContentFilePaths(Iterable.create(
                                abClassFile.relativeTo(outputsFolder),
                                aClassFile.relativeTo(outputsFolder)))
                            .setFunctionAutomatically());
                    final QubPackParameters parameters = QubPackTests.getParameters(process, jvmClassPath);

                    final int exitCode = QubPack.run(parameters);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Compiling 1 file...",
                            "Running tests...",
                            "",
                            "Creating sources jar file...",
                            "Creating compiled sources jar file..."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(0, exitCode);

                    test.assertEqual(
                        Iterable.create(
                            "Content Files:",
                            "A.java"),
                        Strings.getLines(aSourcesJarFile.getContentsAsString().await()));
                    test.assertEqual(
                        Iterable.create(
                            "Content Files:",
                            "A$B.class",
                            "A.class"),
                        Strings.getLines(aJarFile.getContentsAsString().await()));
                });

                runner.test("with anonymous classes in source file",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final QubProjectFolder qubTestProjectFolder = qubFolder.getProjectFolder("qub", "test-java").await();
                    final File qubTestLogFile = qubTestProjectFolder.getProjectDataFolder().await()
                        .getFile("logs/1.log").await();
                    final Folder currentFolder = process.getCurrentFolder();
                    final File projectJsonFile = currentFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(
                        ProjectJSON.create()
                            .setPublisher("me")
                            .setProject("a")
                            .setVersion("34")
                            .setJava(ProjectJSONJava.create())
                            .toString())
                        .await();
                    final Folder sourcesFolder = currentFolder.getFolder("sources").await();
                    final File aJavaFile = sourcesFolder.getFile("A.java").await();
                    aJavaFile.setContentsAsString("A.java source").await();
                    final Folder outputsFolder = currentFolder.getFolder("outputs").await();
                    final File aClassFile = outputsFolder.getFile("A.class").await();
                    aClassFile.setContentsAsString("A.java bytecode").await();
                    final File a1ClassFile = outputsFolder.getFile("A$1.class").await();
                    a1ClassFile.setContentsAsString("A$1 bytecode").await();
                    final File a2ClassFile = outputsFolder.getFile("A$2.class").await();
                    a2ClassFile.setContentsAsString("A$2 bytecode").await();
                    final File aJarFile = outputsFolder.getFile("a.jar").await();
                    final File aSourcesJarFile = outputsFolder.getFile("a.sources.jar").await();
                    final String jvmClassPath = "/fake-jvm-classpath";
                    process.getProcessFactory()
                        .add(new FakeJavacProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addVersion()
                            .setVersionFunctionAutomatically("javac 14.0.1"))
                        .add(new FakeJavacProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addOutputFolder(outputsFolder)
                            .addXlintUnchecked()
                            .addXlintDeprecation()
                            .addClasspath(outputsFolder)
                            .addSourceFile(aJavaFile.relativeTo(currentFolder))
                            .setCompileFunctionAutomatically())
                        .add(new FakeConsoleTestRunnerProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addClasspath(Iterable.create(outputsFolder.toString(), jvmClassPath))
                            .addConsoleTestRunnerFullClassName()
                            .addProfiler(false)
                            .addVerbose(false)
                            .addTestJson(true)
                            .addLogFile(qubTestLogFile)
                            .addOutputFolder(outputsFolder)
                            .addCoverage(Coverage.None)
                            .addFullClassNamesToTest(Iterable.create("A")))
                        .add(new FakeJarProcessRun()
                            .setWorkingFolder(sourcesFolder)
                            .addCreate()
                            .addJarFile(aSourcesJarFile.relativeTo(outputsFolder))
                            .addContentFilePath(aJavaFile.relativeTo(sourcesFolder))
                            .setFunctionAutomatically())
                        .add(new FakeJarProcessRun()
                            .setWorkingFolder(outputsFolder)
                            .addCreate()
                            .addJarFile(aJarFile.relativeTo(outputsFolder))
                            .addContentFilePaths(Iterable.create(
                                a1ClassFile.relativeTo(outputsFolder),
                                a2ClassFile.relativeTo(outputsFolder),
                                aClassFile.relativeTo(outputsFolder)))
                            .setFunctionAutomatically());
                    final QubPackParameters parameters = QubPackTests.getParameters(process, jvmClassPath);

                    final int exitCode = QubPack.run(parameters);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Compiling 1 file...",
                            "Running tests...",
                            "",
                            "Creating sources jar file...",
                            "Creating compiled sources jar file..."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(0, exitCode);

                    test.assertEqual(
                        Iterable.create(
                            "Content Files:",
                            "A.java"),
                        Strings.getLines(aSourcesJarFile.getContentsAsString().await()));
                    test.assertEqual(
                        Iterable.create(
                            "Content Files:",
                            "A$1.class",
                            "A$2.class",
                            "A.class"),
                        Strings.getLines(aJarFile.getContentsAsString().await()));
                });

                runner.test("with main class in project.json",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final QubProjectFolder qubTestProjectFolder = qubFolder.getProjectFolder("qub", "test-java").await();
                    final File qubTestLogFile = qubTestProjectFolder.getProjectDataFolder().await()
                        .getFile("logs/1.log").await();
                    final Folder currentFolder = process.getCurrentFolder();
                    final File projectJsonFile = currentFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(
                        ProjectJSON.create()
                            .setPublisher("me")
                            .setProject("a")
                            .setVersion("34")
                            .setJava(ProjectJSONJava.create()
                                .setMainClass("A"))
                            .toString())
                        .await();
                    final Folder sourcesFolder = currentFolder.getFolder("sources").await();
                    final File aJavaFile = sourcesFolder.getFile("A.java").await();
                    aJavaFile.setContentsAsString("A.java source").await();
                    final Folder outputsFolder = currentFolder.getFolder("outputs").await();
                    final File aClassFile = outputsFolder.getFile("A.class").await();
                    aClassFile.setContentsAsString("A.java bytecode").await();
                    final File manifestFile = outputsFolder.getFile("META-INF/MANIFEST.MF").await();
                    final File aSourcesJarFile = outputsFolder.getFile("a.sources.jar").await();
                    final File aJarFile = outputsFolder.getFile("a.jar").await();
                    final String jvmClassPath = "/fake-jvm-classpath";
                    process.getProcessFactory()
                        .add(new FakeJavacProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addVersion()
                            .setVersionFunctionAutomatically("javac 14.0.1"))
                        .add(new FakeJavacProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addOutputFolder(outputsFolder)
                            .addXlintUnchecked()
                            .addXlintDeprecation()
                            .addClasspath(outputsFolder)
                            .addSourceFile(aJavaFile.relativeTo(currentFolder))
                            .setCompileFunctionAutomatically())
                        .add(new FakeConsoleTestRunnerProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addClasspath(Iterable.create(outputsFolder.toString(), jvmClassPath))
                            .addConsoleTestRunnerFullClassName()
                            .addProfiler(false)
                            .addVerbose(false)
                            .addTestJson(true)
                            .addLogFile(qubTestLogFile)
                            .addOutputFolder(outputsFolder)
                            .addCoverage(Coverage.None)
                            .addFullClassNamesToTest(Iterable.create("A")))
                        .add(new FakeJarProcessRun()
                            .setWorkingFolder(sourcesFolder)
                            .addCreate()
                            .addJarFile(aSourcesJarFile.relativeTo(outputsFolder))
                            .addContentFilePath(aJavaFile.relativeTo(sourcesFolder))
                            .setFunctionAutomatically())
                        .add(new FakeJarProcessRun()
                            .setWorkingFolder(outputsFolder)
                            .addCreate()
                            .addJarFile(aJarFile.relativeTo(outputsFolder))
                            .addManifestFile(manifestFile)
                            .addContentFilePaths(Iterable.create(aClassFile.relativeTo(outputsFolder)))
                            .setFunctionAutomatically());
                    final QubPackParameters parameters = QubPackTests.getParameters(process, jvmClassPath);

                    final int exitCode = QubPack.run(parameters);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Compiling 1 file...",
                            "Running tests...",
                            "",
                            "Creating sources jar file...",
                            "Creating compiled sources jar file..."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(0, exitCode);

                    test.assertEqual(
                        Iterable.create(
                            "Content Files:",
                            "A.java"),
                        Strings.getLines(aSourcesJarFile.getContentsAsString().await()));
                    test.assertEqual(
                        Iterable.create(
                            "Manifest File:",
                            manifestFile.toString(),
                            "",
                            "Content Files:",
                            "A.class"),
                        Strings.getLines(aJarFile.getContentsAsString().await()));
                    test.assertEqual(
                        Iterable.create(
                            "Manifest-Version: 1.0",
                            "Main-Class: A"),
                        Strings.getLines(manifestFile.getContentsAsString().await()));
                });

                runner.test("with test folder",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final QubProjectFolder qubTestProjectFolder = qubFolder.getProjectFolder("qub", "test-java").await();
                    final File qubTestLogFile = qubTestProjectFolder.getProjectDataFolder().await()
                        .getFile("logs/1.log").await();
                    final Folder currentFolder = process.getCurrentFolder();
                    final File projectJsonFile = currentFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(
                        ProjectJSON.create()
                            .setPublisher("me")
                            .setProject("a")
                            .setVersion("34")
                            .setJava(ProjectJSONJava.create())
                            .toString())
                        .await();
                    final Folder sourcesFolder = currentFolder.getFolder("sources").await();
                    final File aJavaFile = sourcesFolder.getFile("A.java").await();
                    aJavaFile.setContentsAsString("A.java source").await();
                    final Folder testsFolder = currentFolder.getFolder("tests").await();
                    final File aTestsJavaFile = testsFolder.getFile("ATests.java").await();
                    aTestsJavaFile.setContentsAsString("ATests.java source").await();
                    final Folder outputsFolder = currentFolder.getFolder("outputs").await();
                    final File aClassFile = outputsFolder.getFile("A.class").await();
                    aClassFile.setContentsAsString("A.java bytecode").await();
                    final File aTestsClassFile = outputsFolder.getFile("ATests.class").await();
                    aTestsClassFile.setContentsAsString("ATests.java bytecode").await();
                    final File aSourcesJarFile = outputsFolder.getFile("a.sources.jar").await();
                    final File aTestsJarFile = outputsFolder.getFile("a.tests.jar").await();
                    final File aJarFile = outputsFolder.getFile("a.jar").await();
                    final String jvmClassPath = "/fake-jvm-classpath";
                    process.getProcessFactory()
                        .add(new FakeJavacProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addVersion()
                            .setVersionFunctionAutomatically("javac 14.0.1"))
                        .add(new FakeJavacProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addOutputFolder(outputsFolder)
                            .addXlintUnchecked()
                            .addXlintDeprecation()
                            .addClasspath(outputsFolder)
                            .addSourceFiles(aJavaFile, aTestsJavaFile)
                            .setCompileFunctionAutomatically())
                        .add(new FakeConsoleTestRunnerProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addClasspath(Iterable.create(outputsFolder.toString(), jvmClassPath))
                            .addConsoleTestRunnerFullClassName()
                            .addProfiler(false)
                            .addVerbose(false)
                            .addTestJson(true)
                            .addLogFile(qubTestLogFile)
                            .addOutputFolder(outputsFolder)
                            .addCoverage(Coverage.None)
                            .addFullClassNamesToTest(Iterable.create("A", "ATests")))
                        .add(new FakeJarProcessRun()
                            .setWorkingFolder(sourcesFolder)
                            .addCreate()
                            .addJarFile(aSourcesJarFile.relativeTo(outputsFolder))
                            .addContentFilePath(aJavaFile.relativeTo(sourcesFolder))
                            .setFunctionAutomatically())
                        .add(new FakeJarProcessRun()
                            .setWorkingFolder(outputsFolder)
                            .addCreate()
                            .addJarFile(aJarFile.relativeTo(outputsFolder))
                            .addContentFilePaths(Iterable.create(aClassFile.relativeTo(outputsFolder)))
                            .setFunctionAutomatically())
                        .add(new FakeJarProcessRun()
                            .setWorkingFolder(outputsFolder)
                            .addCreate()
                            .addJarFile(aTestsJarFile.relativeTo(outputsFolder))
                            .addContentFilePaths(Iterable.create(aTestsClassFile.relativeTo(outputsFolder)))
                            .setFunctionAutomatically());
                    final QubPackParameters parameters = QubPackTests.getParameters(process, jvmClassPath);

                    final int exitCode = QubPack.run(parameters);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Compiling 2 files...",
                            "Running tests...",
                            "",
                            "Creating sources jar file...",
                            "Creating compiled sources jar file...",
                            "Creating compiled tests jar file..."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(0, exitCode);

                    test.assertEqual(
                        Iterable.create(
                            "Content Files:",
                            "A.java"),
                        Strings.getLines(aSourcesJarFile.getContentsAsString().await()));
                    test.assertEqual(
                        Iterable.create(
                            "Content Files:",
                            "A.class"),
                        Strings.getLines(aJarFile.getContentsAsString().await()));
                    test.assertEqual(
                        Iterable.create(
                            "Content Files:",
                            "ATests.class"),
                        Strings.getLines(aTestsJarFile.getContentsAsString().await()));
                });

                runner.test("with test folder with inner class",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final QubProjectFolder qubTestProjectFolder = qubFolder.getProjectFolder("qub", "test-java").await();
                    final File qubTestLogFile = qubTestProjectFolder.getProjectDataFolder().await()
                        .getFile("logs/1.log").await();
                    final Folder currentFolder = process.getCurrentFolder();
                    final File projectJsonFile = currentFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(
                        ProjectJSON.create()
                            .setPublisher("me")
                            .setProject("a")
                            .setVersion("34")
                            .setJava(ProjectJSONJava.create())
                            .toString())
                        .await();
                    final Folder sourcesFolder = currentFolder.getFolder("sources").await();
                    final File aJavaFile = sourcesFolder.getFile("A.java").await();
                    aJavaFile.setContentsAsString("A.java source").await();
                    final Folder testsFolder = currentFolder.getFolder("tests").await();
                    final File aTestsJavaFile = testsFolder.getFile("ATests.java").await();
                    aTestsJavaFile.setContentsAsString("ATests.java source").await();
                    final Folder outputsFolder = currentFolder.getFolder("outputs").await();
                    final File aClassFile = outputsFolder.getFile("A.class").await();
                    aClassFile.setContentsAsString("A.java bytecode").await();
                    final File aTestsClassFile = outputsFolder.getFile("ATests.class").await();
                    aTestsClassFile.setContentsAsString("ATests.java bytecode").await();
                    final File aTestsBClassFile = outputsFolder.getFile("ATests$B.class").await();
                    aTestsBClassFile.setContentsAsString("ATests.java inner class bytecode").await();
                    final File aSourcesJarFile = outputsFolder.getFile("a.sources.jar").await();
                    final File aTestsJarFile = outputsFolder.getFile("a.tests.jar").await();
                    final File aJarFile = outputsFolder.getFile("a.jar").await();
                    final String jvmClassPath = "/fake-jvm-classpath";
                    process.getProcessFactory()
                        .add(new FakeJavacProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addVersion()
                            .setVersionFunctionAutomatically("javac 14.0.1"))
                        .add(new FakeJavacProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addOutputFolder(outputsFolder)
                            .addXlintUnchecked()
                            .addXlintDeprecation()
                            .addClasspath(outputsFolder)
                            .addSourceFiles(aJavaFile, aTestsJavaFile)
                            .setCompileFunctionAutomatically())
                        .add(new FakeConsoleTestRunnerProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addClasspath(Iterable.create(outputsFolder.toString(), jvmClassPath))
                            .addConsoleTestRunnerFullClassName()
                            .addProfiler(false)
                            .addVerbose(false)
                            .addTestJson(true)
                            .addLogFile(qubTestLogFile)
                            .addOutputFolder(outputsFolder)
                            .addCoverage(Coverage.None)
                            .addFullClassNamesToTest(Iterable.create("A", "ATests")))
                        .add(new FakeJarProcessRun()
                            .setWorkingFolder(sourcesFolder)
                            .addCreate()
                            .addJarFile(aSourcesJarFile.relativeTo(outputsFolder))
                            .addContentFilePath(aJavaFile.relativeTo(sourcesFolder))
                            .setFunctionAutomatically())
                        .add(new FakeJarProcessRun()
                            .setWorkingFolder(outputsFolder)
                            .addCreate()
                            .addJarFile(aJarFile.relativeTo(outputsFolder))
                            .addContentFilePaths(Iterable.create(aClassFile.relativeTo(outputsFolder)))
                            .setFunctionAutomatically())
                        .add(new FakeJarProcessRun()
                            .setWorkingFolder(outputsFolder)
                            .addCreate()
                            .addJarFile(aTestsJarFile.relativeTo(outputsFolder))
                            .addContentFilePaths(Iterable.create(
                                aTestsBClassFile.relativeTo(outputsFolder),
                                aTestsClassFile.relativeTo(outputsFolder)))
                            .setFunctionAutomatically());
                    final QubPackParameters parameters = QubPackTests.getParameters(process, jvmClassPath);

                    final int exitCode = QubPack.run(parameters);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Compiling 2 files...",
                            "Running tests...",
                            "",
                            "Creating sources jar file...",
                            "Creating compiled sources jar file...",
                            "Creating compiled tests jar file..."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(0, exitCode);

                    test.assertEqual(
                        Iterable.create(
                            "Content Files:",
                            "A.java"),
                        Strings.getLines(aSourcesJarFile.getContentsAsString().await()));
                    test.assertEqual(
                        Iterable.create(
                            "Content Files:",
                            "A.class"),
                        Strings.getLines(aJarFile.getContentsAsString().await()));
                    test.assertEqual(
                        Iterable.create(
                            "Content Files:",
                            "ATests$B.class",
                            "ATests.class"),
                        Strings.getLines(aTestsJarFile.getContentsAsString().await()));
                });

                runner.test("with test folder with anonymous class",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final QubProjectFolder qubTestProjectFolder = qubFolder.getProjectFolder("qub", "test-java").await();
                    final File qubTestLogFile = qubTestProjectFolder.getProjectDataFolder().await()
                        .getFile("logs/1.log").await();
                    final Folder currentFolder = process.getCurrentFolder();
                    final File projectJsonFile = currentFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(
                        ProjectJSON.create()
                            .setPublisher("me")
                            .setProject("a")
                            .setVersion("34")
                            .setJava(ProjectJSONJava.create())
                            .toString())
                        .await();
                    final Folder sourcesFolder = currentFolder.getFolder("sources").await();
                    final File aJavaFile = sourcesFolder.getFile("A.java").await();
                    aJavaFile.setContentsAsString("A.java source").await();
                    final Folder testsFolder = currentFolder.getFolder("tests").await();
                    final File aTestsJavaFile = testsFolder.getFile("ATests.java").await();
                    aTestsJavaFile.setContentsAsString("ATests.java source").await();
                    final Folder outputsFolder = currentFolder.getFolder("outputs").await();
                    final File aClassFile = outputsFolder.getFile("A.class").await();
                    aClassFile.setContentsAsString("A.java bytecode").await();
                    final File aTestsClassFile = outputsFolder.getFile("ATests.class").await();
                    aTestsClassFile.setContentsAsString("ATests.java bytecode").await();
                    final File aTests1ClassFile = outputsFolder.getFile("ATests$1.class").await();
                    aTests1ClassFile.setContentsAsString("ATests.java anonymous class bytecode").await();
                    final File aSourcesJarFile = outputsFolder.getFile("a.sources.jar").await();
                    final File aTestsJarFile = outputsFolder.getFile("a.tests.jar").await();
                    final File aJarFile = outputsFolder.getFile("a.jar").await();
                    final String jvmClassPath = "/fake-jvm-classpath";
                    process.getProcessFactory()
                        .add(new FakeJavacProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addVersion()
                            .setVersionFunctionAutomatically("javac 14.0.1"))
                        .add(new FakeJavacProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addOutputFolder(outputsFolder)
                            .addXlintUnchecked()
                            .addXlintDeprecation()
                            .addClasspath(outputsFolder)
                            .addSourceFiles(aJavaFile, aTestsJavaFile)
                            .setCompileFunctionAutomatically())
                        .add(new FakeConsoleTestRunnerProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addClasspath(Iterable.create(outputsFolder.toString(), jvmClassPath))
                            .addConsoleTestRunnerFullClassName()
                            .addProfiler(false)
                            .addVerbose(false)
                            .addTestJson(true)
                            .addLogFile(qubTestLogFile)
                            .addOutputFolder(outputsFolder)
                            .addCoverage(Coverage.None)
                            .addFullClassNamesToTest(Iterable.create("A", "ATests")))
                        .add(new FakeJarProcessRun()
                            .setWorkingFolder(sourcesFolder)
                            .addCreate()
                            .addJarFile(aSourcesJarFile.relativeTo(outputsFolder))
                            .addContentFilePath(aJavaFile.relativeTo(sourcesFolder))
                            .setFunctionAutomatically())
                        .add(new FakeJarProcessRun()
                            .setWorkingFolder(outputsFolder)
                            .addCreate()
                            .addJarFile(aJarFile.relativeTo(outputsFolder))
                            .addContentFilePaths(Iterable.create(aClassFile.relativeTo(outputsFolder)))
                            .setFunctionAutomatically())
                        .add(new FakeJarProcessRun()
                            .setWorkingFolder(outputsFolder)
                            .addCreate()
                            .addJarFile(aTestsJarFile.relativeTo(outputsFolder))
                            .addContentFilePaths(Iterable.create(
                                aTests1ClassFile.relativeTo(outputsFolder),
                                aTestsClassFile.relativeTo(outputsFolder)))
                            .setFunctionAutomatically());
                    final QubPackParameters parameters = QubPackTests.getParameters(process, jvmClassPath);

                    final int exitCode = QubPack.run(parameters);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Compiling 2 files...",
                            "Running tests...",
                            "",
                            "Creating sources jar file...",
                            "Creating compiled sources jar file...",
                            "Creating compiled tests jar file..."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(0, exitCode);

                    test.assertEqual(
                        Iterable.create(
                            "Content Files:",
                            "A.java"),
                        Strings.getLines(aSourcesJarFile.getContentsAsString().await()));
                    test.assertEqual(
                        Iterable.create(
                            "Content Files:",
                            "A.class"),
                        Strings.getLines(aJarFile.getContentsAsString().await()));
                    test.assertEqual(
                        Iterable.create(
                            "Content Files:",
                            "ATests$1.class",
                            "ATests.class"),
                        Strings.getLines(aTestsJarFile.getContentsAsString().await()));
                });

                runner.test("with packjson=false",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final QubProjectFolder qubTestProjectFolder = qubFolder.getProjectFolder("qub", "test-java").await();
                    final File qubTestLogFile = qubTestProjectFolder.getProjectDataFolder().await()
                        .getFile("logs/1.log").await();
                    final Folder currentFolder = process.getCurrentFolder();
                    final File projectJsonFile = currentFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(
                        ProjectJSON.create()
                            .setPublisher("me")
                            .setProject("a")
                            .setVersion("34")
                            .setJava(ProjectJSONJava.create())
                            .toString())
                        .await();
                    final Folder sourcesFolder = currentFolder.getFolder("sources").await();
                    final File aJavaFile = sourcesFolder.getFile("A.java").await();
                    aJavaFile.setContentsAsString("A.java source").await();
                    final Folder testsFolder = currentFolder.getFolder("tests").await();
                    final File aTestsJavaFile = testsFolder.getFile("ATests.java").await();
                    aTestsJavaFile.setContentsAsString("ATests.java source").await();
                    final Folder outputsFolder = currentFolder.getFolder("outputs").await();
                    final File aClassFile = outputsFolder.getFile("A.class").await();
                    aClassFile.setContentsAsString("A.java bytecode").await();
                    final File aTestsClassFile = outputsFolder.getFile("ATests.class").await();
                    aTestsClassFile.setContentsAsString("ATests.java bytecode").await();
                    final File aSourcesJarFile = outputsFolder.getFile("a.sources.jar").await();
                    final File aTestsJarFile = outputsFolder.getFile("a.tests.jar").await();
                    final File aJarFile = outputsFolder.getFile("a.jar").await();
                    final File packJson = outputsFolder.getFile("pack.json").await();
                    final String jvmClassPath = "/fake-jvm-classpath";
                    process.getProcessFactory()
                        .add(new FakeJavacProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addVersion()
                            .setVersionFunctionAutomatically("javac 14.0.1"))
                        .add(new FakeJavacProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addOutputFolder(outputsFolder)
                            .addXlintUnchecked()
                            .addXlintDeprecation()
                            .addClasspath(outputsFolder)
                            .addSourceFiles(aJavaFile, aTestsJavaFile)
                            .setCompileFunctionAutomatically())
                        .add(new FakeConsoleTestRunnerProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addClasspath(Iterable.create(outputsFolder.toString(), jvmClassPath))
                            .addConsoleTestRunnerFullClassName()
                            .addProfiler(false)
                            .addVerbose(false)
                            .addTestJson(true)
                            .addLogFile(qubTestLogFile)
                            .addOutputFolder(outputsFolder)
                            .addCoverage(Coverage.None)
                            .addFullClassNamesToTest(Iterable.create("A", "ATests")))
                        .add(new FakeJarProcessRun()
                            .setWorkingFolder(sourcesFolder)
                            .addCreate()
                            .addJarFile(aSourcesJarFile.relativeTo(outputsFolder))
                            .addContentFilePath(aJavaFile.relativeTo(sourcesFolder))
                            .setFunctionAutomatically())
                        .add(new FakeJarProcessRun()
                            .setWorkingFolder(outputsFolder)
                            .addCreate()
                            .addJarFile(aJarFile.relativeTo(outputsFolder))
                            .addContentFilePaths(Iterable.create(aClassFile.relativeTo(outputsFolder)))
                            .setFunctionAutomatically())
                        .add(new FakeJarProcessRun()
                            .setWorkingFolder(outputsFolder)
                            .addCreate()
                            .addJarFile(aTestsJarFile.relativeTo(outputsFolder))
                            .addContentFilePaths(Iterable.create(aTestsClassFile.relativeTo(outputsFolder)))
                            .setFunctionAutomatically());
                    final QubPackParameters parameters = QubPackTests.getParameters(process, jvmClassPath)
                        .setPackJson(false);

                    final int exitCode = QubPack.run(parameters);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Compiling 2 files...",
                            "Running tests...",
                            "",
                            "Creating sources jar file...",
                            "Creating compiled sources jar file...",
                            "Creating compiled tests jar file..."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(0, exitCode);

                    test.assertEqual(
                        Iterable.create(
                            "Content Files:",
                            "A.java"),
                        Strings.getLines(aSourcesJarFile.getContentsAsString().await()));
                    test.assertEqual(
                        Iterable.create(
                            "Content Files:",
                            "A.class"),
                        Strings.getLines(aJarFile.getContentsAsString().await()));
                    test.assertEqual(
                        Iterable.create(
                            "Content Files:",
                            "ATests.class"),
                        Strings.getLines(aTestsJarFile.getContentsAsString().await()));
                    test.assertFalse(packJson.exists().await());
                });

                runner.test("with packjson=true but no existing pack.json file",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final QubProjectFolder qubTestProjectFolder = qubFolder.getProjectFolder("qub", "test-java").await();
                    final File qubTestLogFile = qubTestProjectFolder.getProjectDataFolder().await()
                        .getFile("logs/1.log").await();
                    final Folder currentFolder = process.getCurrentFolder();
                    final File projectJsonFile = currentFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(
                        ProjectJSON.create()
                            .setPublisher("me")
                            .setProject("a")
                            .setVersion("34")
                            .setJava(ProjectJSONJava.create())
                            .toString())
                        .await();
                    final Folder sourcesFolder = currentFolder.getFolder("sources").await();
                    final File aJavaFile = sourcesFolder.getFile("A.java").await();
                    aJavaFile.setContentsAsString("A.java source").await();
                    final Folder testsFolder = currentFolder.getFolder("tests").await();
                    final File aTestsJavaFile = testsFolder.getFile("ATests.java").await();
                    aTestsJavaFile.setContentsAsString("ATests.java source").await();
                    final Folder outputsFolder = currentFolder.getFolder("outputs").await();
                    final File aClassFile = outputsFolder.getFile("A.class").await();
                    aClassFile.setContentsAsString("A.java bytecode").await();
                    final File aTestsClassFile = outputsFolder.getFile("ATests.class").await();
                    aTestsClassFile.setContentsAsString("ATests.java bytecode").await();
                    final File aSourcesJarFile = outputsFolder.getFile("a.sources.jar").await();
                    final File aTestsJarFile = outputsFolder.getFile("a.tests.jar").await();
                    final File aJarFile = outputsFolder.getFile("a.jar").await();
                    final File packJsonFile = outputsFolder.getFile("pack.json").await();
                    final String jvmClassPath = "/fake-jvm-classpath";
                    process.getProcessFactory()
                        .add(new FakeJavacProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addVersion()
                            .setVersionFunctionAutomatically("javac 14.0.1"))
                        .add(new FakeJavacProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addOutputFolder(outputsFolder)
                            .addXlintUnchecked()
                            .addXlintDeprecation()
                            .addClasspath(outputsFolder)
                            .addSourceFiles(aJavaFile, aTestsJavaFile)
                            .setCompileFunctionAutomatically())
                        .add(new FakeConsoleTestRunnerProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addClasspath(Iterable.create(outputsFolder.toString(), jvmClassPath))
                            .addConsoleTestRunnerFullClassName()
                            .addProfiler(false)
                            .addVerbose(false)
                            .addTestJson(true)
                            .addLogFile(qubTestLogFile)
                            .addOutputFolder(outputsFolder)
                            .addCoverage(Coverage.None)
                            .addFullClassNamesToTest(Iterable.create("A", "ATests")))
                        .add(new FakeJarProcessRun()
                            .setWorkingFolder(sourcesFolder)
                            .addCreate()
                            .addJarFile(aSourcesJarFile.relativeTo(outputsFolder))
                            .addContentFilePath(aJavaFile.relativeTo(sourcesFolder))
                            .setFunctionAutomatically())
                        .add(new FakeJarProcessRun()
                            .setWorkingFolder(outputsFolder)
                            .addCreate()
                            .addJarFile(aJarFile.relativeTo(outputsFolder))
                            .addContentFilePaths(Iterable.create(aClassFile.relativeTo(outputsFolder)))
                            .setFunctionAutomatically())
                        .add(new FakeJarProcessRun()
                            .setWorkingFolder(outputsFolder)
                            .addCreate()
                            .addJarFile(aTestsJarFile.relativeTo(outputsFolder))
                            .addContentFilePaths(Iterable.create(aTestsClassFile.relativeTo(outputsFolder)))
                            .setFunctionAutomatically());
                    final QubPackParameters parameters = QubPackTests.getParameters(process, jvmClassPath)
                        .setPackJson(true);

                    final int exitCode = QubPack.run(parameters);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Compiling 2 files...",
                            "Running tests...",
                            "",
                            "Creating sources jar file...",
                            "Creating compiled sources jar file...",
                            "Creating compiled tests jar file..."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(0, exitCode);

                    test.assertEqual(
                        Iterable.create(
                            "Content Files:",
                            "A.java"),
                        Strings.getLines(aSourcesJarFile.getContentsAsString().await()));
                    test.assertEqual(
                        Iterable.create(
                            "Content Files:",
                            "A.class"),
                        Strings.getLines(aJarFile.getContentsAsString().await()));
                    test.assertEqual(
                        Iterable.create(
                            "Content Files:",
                            "ATests.class"),
                        Strings.getLines(aTestsJarFile.getContentsAsString().await()));
                    test.assertEqual(
                        PackJSON.create()
                            .setSourceFiles(Iterable.create(
                                PackJSONFile.create(aJavaFile.relativeTo(sourcesFolder), aJavaFile.getLastModified().await())))
                            .setSourceOutputFiles(Iterable.create(
                                PackJSONFile.create(aClassFile.relativeTo(outputsFolder), aClassFile.getLastModified().await())))
                            .setTestOutputFiles(Iterable.create(
                                PackJSONFile.create(aTestsClassFile.relativeTo(outputsFolder), aTestsClassFile.getLastModified().await())))
                            .setProject("a")
                            .toString(JSONFormat.pretty),
                        packJsonFile.getContentsAsString().await());
                });

                runner.test("with packjson=true with existing empty pack.json file",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final QubFolder qubFolder = process.getQubFolder().await();
                    final QubProjectFolder qubTestProjectFolder = qubFolder.getProjectFolder("qub", "test-java").await();
                    final File qubTestLogFile = qubTestProjectFolder.getProjectDataFolder().await()
                        .getFile("logs/1.log").await();
                    final Folder currentFolder = process.getCurrentFolder();
                    final File projectJsonFile = currentFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(
                        ProjectJSON.create()
                            .setPublisher("me")
                            .setProject("a")
                            .setVersion("34")
                            .setJava(ProjectJSONJava.create())
                            .toString())
                        .await();
                    final Folder sourcesFolder = currentFolder.getFolder("sources").await();
                    final File aJavaFile = sourcesFolder.getFile("A.java").await();
                    aJavaFile.setContentsAsString("A.java source").await();
                    final Folder testsFolder = currentFolder.getFolder("tests").await();
                    final File aTestsJavaFile = testsFolder.getFile("ATests.java").await();
                    aTestsJavaFile.setContentsAsString("ATests.java source").await();
                    final Folder outputsFolder = currentFolder.getFolder("outputs").await();
                    final File aClassFile = outputsFolder.getFile("A.class").await();
                    aClassFile.setContentsAsString("A.java bytecode").await();
                    final File aTestsClassFile = outputsFolder.getFile("ATests.class").await();
                    aTestsClassFile.setContentsAsString("ATests.java bytecode").await();
                    final File aSourcesJarFile = outputsFolder.getFile("a.sources.jar").await();
                    final File aTestsJarFile = outputsFolder.getFile("a.tests.jar").await();
                    final File aJarFile = outputsFolder.getFile("a.jar").await();
                    final File packJsonFile = outputsFolder.getFile("pack.json").await();
                    packJsonFile.create().await();
                    final String jvmClassPath = "/fake-jvm-classpath";
                    process.getProcessFactory()
                        .add(new FakeJavacProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addVersion()
                            .setVersionFunctionAutomatically("javac 14.0.1"))
                        .add(new FakeJavacProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addOutputFolder(outputsFolder)
                            .addXlintUnchecked()
                            .addXlintDeprecation()
                            .addClasspath(outputsFolder)
                            .addSourceFiles(aJavaFile, aTestsJavaFile)
                            .setCompileFunctionAutomatically())
                        .add(new FakeConsoleTestRunnerProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addClasspath(Iterable.create(outputsFolder.toString(), jvmClassPath))
                            .addConsoleTestRunnerFullClassName()
                            .addProfiler(false)
                            .addVerbose(false)
                            .addTestJson(true)
                            .addLogFile(qubTestLogFile)
                            .addOutputFolder(outputsFolder)
                            .addCoverage(Coverage.None)
                            .addFullClassNamesToTest(Iterable.create("A", "ATests")))
                        .add(new FakeJarProcessRun()
                            .setWorkingFolder(sourcesFolder)
                            .addCreate()
                            .addJarFile(aSourcesJarFile.relativeTo(outputsFolder))
                            .addContentFilePath(aJavaFile.relativeTo(sourcesFolder))
                            .setFunctionAutomatically())
                        .add(new FakeJarProcessRun()
                            .setWorkingFolder(outputsFolder)
                            .addCreate()
                            .addJarFile(aJarFile.relativeTo(outputsFolder))
                            .addContentFilePaths(Iterable.create(aClassFile.relativeTo(outputsFolder)))
                            .setFunctionAutomatically())
                        .add(new FakeJarProcessRun()
                            .setWorkingFolder(outputsFolder)
                            .addCreate()
                            .addJarFile(aTestsJarFile.relativeTo(outputsFolder))
                            .addContentFilePaths(Iterable.create(aTestsClassFile.relativeTo(outputsFolder)))
                            .setFunctionAutomatically());
                    final QubPackParameters parameters = QubPackTests.getParameters(process, jvmClassPath)
                        .setPackJson(true);

                    final int exitCode = QubPack.run(parameters);

                    test.assertLinesEqual(
                        Iterable.create(
                            "Compiling 2 files...",
                            "Running tests...",
                            "",
                            "Creating sources jar file...",
                            "Creating compiled sources jar file...",
                            "Creating compiled tests jar file..."),
                        process.getOutputWriteStream());
                    test.assertLinesEqual(
                        Iterable.create(),
                        process.getErrorWriteStream());
                    test.assertEqual(0, exitCode);

                    test.assertEqual(
                        Iterable.create(
                            "Content Files:",
                            "A.java"),
                        Strings.getLines(aSourcesJarFile.getContentsAsString().await()));
                    test.assertEqual(
                        Iterable.create(
                            "Content Files:",
                            "A.class"),
                        Strings.getLines(aJarFile.getContentsAsString().await()));
                    test.assertEqual(
                        Iterable.create(
                            "Content Files:",
                            "ATests.class"),
                        Strings.getLines(aTestsJarFile.getContentsAsString().await()));
                    test.assertEqual(
                        PackJSON.create()
                            .setSourceFiles(Iterable.create(
                                PackJSONFile.create(aJavaFile.relativeTo(sourcesFolder), aJavaFile.getLastModified().await())))
                            .setSourceOutputFiles(Iterable.create(
                                PackJSONFile.create(aClassFile.relativeTo(outputsFolder), aClassFile.getLastModified().await())))
                            .setTestOutputFiles(Iterable.create(
                                PackJSONFile.create(aTestsClassFile.relativeTo(outputsFolder), aTestsClassFile.getLastModified().await())))
                            .setProject("a")
                            .toString(JSONFormat.pretty),
                        packJsonFile.getContentsAsString().await());
                });
            });
        });
    }

    static QubPackParameters getParameters(FakeDesktopProcess process)
    {
        PreCondition.assertNotNull(process, "process");

        return QubPackParametersTests.getParameters(process, "/fake-jvm-classpath");
    }

    static QubPackParameters getParameters(FakeDesktopProcess process, String jvmClasspath)
    {
        PreCondition.assertNotNull(process, "process");
        PreCondition.assertNotNullAndNotEmpty(jvmClasspath, "jvmClasspath");

        final InMemoryCharacterToByteStream output = process.getOutputWriteStream();
        final InMemoryCharacterToByteStream error = process.getErrorWriteStream();
        final Folder folderToPack = process.getCurrentFolder();
        final EnvironmentVariables environmentVariables = process.getEnvironmentVariables();
        final FakeProcessFactory processFactory = process.getProcessFactory();
        final FakeDefaultApplicationLauncher defaultApplicationLauncher = process.getDefaultApplicationLauncher();
        final QubFolder qubFolder = process.getQubFolder().await();

        final File qubBuildCompiledSourcesFile = qubFolder.getCompiledSourcesFile("qub", "build-java", "7").await();
        qubBuildCompiledSourcesFile.create().await();
        final File qubTestCompiledSourcesFile = qubFolder.getCompiledSourcesFile("qub", "test-java", "8").await();
        qubTestCompiledSourcesFile.create().await();
        final FakeTypeLoader typeLoader = process.getTypeLoader()
            .addTypeContainer(QubBuild.class, qubBuildCompiledSourcesFile)
            .addTypeContainer(QubTest.class, qubTestCompiledSourcesFile);

        return new QubPackParameters(output, error, folderToPack, environmentVariables, processFactory, defaultApplicationLauncher, jvmClasspath, typeLoader, qubFolder);
    }
}
