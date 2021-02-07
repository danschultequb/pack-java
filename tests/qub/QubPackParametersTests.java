package qub;

public interface QubPackParametersTests
{
    static void test(TestRunner runner)
    {
        PreCondition.assertNotNull(runner, "runner");

        runner.testGroup(QubPackParameters.class, () ->
        {
            runner.testGroup("setPattern(String)", () ->
            {
                final Action1<String> setPatternTest = (String pattern) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(pattern), (Test test) ->
                    {
                        final InMemoryCharacterToByteStream output = InMemoryCharacterToByteStream.create();
                        final InMemoryCharacterToByteStream error = InMemoryCharacterToByteStream.create();
                        final InMemoryFileSystem fileSystem = InMemoryFileSystem.create(test.getClock());
                        fileSystem.createRoot("/").await();
                        final Folder currentFolder = fileSystem.getFolder("/current/folder/").await();
                        final FakeProcessFactory processFactory = FakeProcessFactory.create(test.getParallelAsyncRunner(), currentFolder);
                        final FakeDefaultApplicationLauncher defaultApplicationLauncher = FakeDefaultApplicationLauncher.create(fileSystem);
                        final QubFolder qubFolder = QubFolder.get(fileSystem.getFolder("/qub/").await());
                        final File qubBuildCompiledSourcesFile = qubFolder.getCompiledSourcesFile("qub", "build-java", "7").await();
                        qubBuildCompiledSourcesFile.create().await();
                        final File qubTestCompiledSourcesFile = qubFolder.getCompiledSourcesFile("qub", "test-java", "8").await();
                        qubTestCompiledSourcesFile.create().await();
                        final EnvironmentVariables environmentVariables = EnvironmentVariables.create()
                            .set("QUB_HOME", qubFolder.toString());
                        final String jvmClassPath = "/fake-jvm-classpath";
                        final FakeTypeLoader typeLoader = FakeTypeLoader.create()
                            .addTypeContainer(QubBuild.class, qubBuildCompiledSourcesFile)
                            .addTypeContainer(QubTest.class, qubTestCompiledSourcesFile);
                        final QubPackParameters parameters = new QubPackParameters(output, error, currentFolder, environmentVariables, processFactory, defaultApplicationLauncher, jvmClassPath, typeLoader);

                        final QubPackParameters setPatternResult = parameters.setPattern(pattern);
                        test.assertSame(parameters, setPatternResult);
                        test.assertEqual(pattern, parameters.getPattern());
                    });
                };

                setPatternTest.run(null);
                setPatternTest.run("");
                setPatternTest.run("hello");
            });

            runner.testGroup("setCoverage(Coverage)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final InMemoryCharacterToByteStream output = InMemoryCharacterToByteStream.create();
                    final InMemoryCharacterToByteStream error = InMemoryCharacterToByteStream.create();
                    final InMemoryFileSystem fileSystem = InMemoryFileSystem.create(test.getClock());
                    fileSystem.createRoot("/").await();
                    final Folder currentFolder = fileSystem.getFolder("/current/folder/").await();
                    final FakeProcessFactory processFactory = FakeProcessFactory.create(test.getParallelAsyncRunner(), currentFolder);
                    final FakeDefaultApplicationLauncher defaultApplicationLauncher = FakeDefaultApplicationLauncher.create(fileSystem);
                    final QubFolder qubFolder = QubFolder.get(fileSystem.getFolder("/qub/").await());
                    final File qubBuildCompiledSourcesFile = qubFolder.getCompiledSourcesFile("qub", "build-java", "7").await();
                    qubBuildCompiledSourcesFile.create().await();
                    final File qubTestCompiledSourcesFile = qubFolder.getCompiledSourcesFile("qub", "test-java", "8").await();
                    qubTestCompiledSourcesFile.create().await();
                    final EnvironmentVariables environmentVariables = EnvironmentVariables.create()
                        .set("QUB_HOME", qubFolder.toString());
                    final String jvmClassPath = "/fake-jvm-classpath";
                    final FakeTypeLoader typeLoader = FakeTypeLoader.create()
                        .addTypeContainer(QubBuild.class, qubBuildCompiledSourcesFile)
                        .addTypeContainer(QubTest.class, qubTestCompiledSourcesFile);
                    final QubPackParameters parameters = new QubPackParameters(output, error, currentFolder, environmentVariables, processFactory, defaultApplicationLauncher, jvmClassPath, typeLoader);

                    test.assertThrows(() -> parameters.setCoverage(null),
                        new PreConditionFailure("coverage cannot be null."));
                    test.assertEqual(Coverage.None, parameters.getCoverage());
                });

                final Action1<Coverage> setCoverageTest = (Coverage coverage) ->
                {
                    runner.test("with " + coverage, (Test test) ->
                    {
                        final InMemoryCharacterToByteStream output = InMemoryCharacterToByteStream.create();
                        final InMemoryCharacterToByteStream error = InMemoryCharacterToByteStream.create();
                        final InMemoryFileSystem fileSystem = InMemoryFileSystem.create(test.getClock());
                        fileSystem.createRoot("/").await();
                        final Folder currentFolder = fileSystem.getFolder("/current/folder/").await();
                        final FakeProcessFactory processFactory = FakeProcessFactory.create(test.getParallelAsyncRunner(), currentFolder);
                        final FakeDefaultApplicationLauncher defaultApplicationLauncher = FakeDefaultApplicationLauncher.create(fileSystem);
                        final QubFolder qubFolder = QubFolder.get(fileSystem.getFolder("/qub/").await());
                        final File qubBuildCompiledSourcesFile = qubFolder.getCompiledSourcesFile("qub", "build-java", "7").await();
                        qubBuildCompiledSourcesFile.create().await();
                        final File qubTestCompiledSourcesFile = qubFolder.getCompiledSourcesFile("qub", "test-java", "8").await();
                        qubTestCompiledSourcesFile.create().await();
                        final EnvironmentVariables environmentVariables = EnvironmentVariables.create()
                            .set("QUB_HOME", qubFolder.toString());
                        final String jvmClassPath = "/fake-jvm-classpath";
                        final FakeTypeLoader typeLoader = FakeTypeLoader.create()
                            .addTypeContainer(QubBuild.class, qubBuildCompiledSourcesFile)
                            .addTypeContainer(QubTest.class, qubTestCompiledSourcesFile);
                        final QubPackParameters parameters = new QubPackParameters(output, error, currentFolder, environmentVariables, processFactory, defaultApplicationLauncher, jvmClassPath, typeLoader);

                        final QubPackParameters setCoverageResult = parameters.setCoverage(coverage);
                        test.assertSame(parameters, setCoverageResult);
                        test.assertEqual(coverage, parameters.getCoverage());
                    });
                };

                for (final Coverage coverage : Coverage.values())
                {
                    setCoverageTest.run(coverage);
                }
            });

            runner.testGroup("setTestJson(boolean)", () ->
            {
                final Action1<Boolean> setTestJson = (Boolean testJson) ->
                {
                    runner.test("with " + testJson, (Test test) ->
                    {
                        final InMemoryCharacterToByteStream output = InMemoryCharacterToByteStream.create();
                        final InMemoryCharacterToByteStream error = InMemoryCharacterToByteStream.create();
                        final InMemoryFileSystem fileSystem = InMemoryFileSystem.create(test.getClock());
                        fileSystem.createRoot("/").await();
                        final Folder currentFolder = fileSystem.getFolder("/current/folder/").await();
                        final FakeProcessFactory processFactory = FakeProcessFactory.create(test.getParallelAsyncRunner(), currentFolder);
                        final FakeDefaultApplicationLauncher defaultApplicationLauncher = FakeDefaultApplicationLauncher.create(fileSystem);
                        final QubFolder qubFolder = QubFolder.get(fileSystem.getFolder("/qub/").await());
                        final File qubBuildCompiledSourcesFile = qubFolder.getCompiledSourcesFile("qub", "build-java", "7").await();
                        qubBuildCompiledSourcesFile.create().await();
                        final File qubTestCompiledSourcesFile = qubFolder.getCompiledSourcesFile("qub", "test-java", "8").await();
                        qubTestCompiledSourcesFile.create().await();
                        final EnvironmentVariables environmentVariables = EnvironmentVariables.create()
                            .set("QUB_HOME", qubFolder.toString());
                        final String jvmClassPath = "/fake-jvm-classpath";
                        final FakeTypeLoader typeLoader = FakeTypeLoader.create()
                            .addTypeContainer(QubBuild.class, qubBuildCompiledSourcesFile)
                            .addTypeContainer(QubTest.class, qubTestCompiledSourcesFile);
                        final QubPackParameters parameters = new QubPackParameters(output, error, currentFolder, environmentVariables, processFactory, defaultApplicationLauncher, jvmClassPath, typeLoader);

                        final QubPackParameters setTestJsonResult = parameters.setTestJson(testJson);
                        test.assertSame(parameters, setTestJsonResult);
                        test.assertEqual(testJson, parameters.getTestJson());
                    });
                };

                setTestJson.run(false);
                setTestJson.run(true);
            });

            runner.testGroup("setJvmClassPath(String)", () ->
            {
                final Action1<String> setTestJson = (String newJvmClassPath) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(newJvmClassPath), (Test test) ->
                    {
                        final InMemoryCharacterToByteStream output = InMemoryCharacterToByteStream.create();
                        final InMemoryCharacterToByteStream error = InMemoryCharacterToByteStream.create();
                        final InMemoryFileSystem fileSystem = InMemoryFileSystem.create(test.getClock());
                        fileSystem.createRoot("/").await();
                        final Folder currentFolder = fileSystem.getFolder("/current/folder/").await();
                        final FakeProcessFactory processFactory = FakeProcessFactory.create(test.getParallelAsyncRunner(), currentFolder);
                        final FakeDefaultApplicationLauncher defaultApplicationLauncher = FakeDefaultApplicationLauncher.create(fileSystem);
                        final QubFolder qubFolder = QubFolder.get(fileSystem.getFolder("/qub/").await());
                        final File qubBuildCompiledSourcesFile = qubFolder.getCompiledSourcesFile("qub", "build-java", "7").await();
                        qubBuildCompiledSourcesFile.create().await();
                        final File qubTestCompiledSourcesFile = qubFolder.getCompiledSourcesFile("qub", "test-java", "8").await();
                        qubTestCompiledSourcesFile.create().await();
                        final EnvironmentVariables environmentVariables = EnvironmentVariables.create()
                            .set("QUB_HOME", qubFolder.toString());
                        final String jvmClassPath = "/fake-jvm-classpath";
                        final FakeTypeLoader typeLoader = FakeTypeLoader.create()
                            .addTypeContainer(QubBuild.class, qubBuildCompiledSourcesFile)
                            .addTypeContainer(QubTest.class, qubTestCompiledSourcesFile);
                        final QubPackParameters parameters = new QubPackParameters(output, error, currentFolder, environmentVariables, processFactory, defaultApplicationLauncher, jvmClassPath, typeLoader);

                        final QubPackParameters setJvmClassPathResult = parameters.setJvmClassPath(newJvmClassPath);
                        test.assertSame(parameters, setJvmClassPathResult);
                        test.assertEqual(newJvmClassPath, parameters.getJvmClassPath());
                    });
                };

                setTestJson.run(null);
                setTestJson.run("");
                setTestJson.run("hello");
            });

            runner.testGroup("setProfiler(boolean)", () ->
            {
                final Action1<Boolean> setProfiler = (Boolean profiler) ->
                {
                    runner.test("with " + profiler, (Test test) ->
                    {
                        final InMemoryCharacterToByteStream output = InMemoryCharacterToByteStream.create();
                        final InMemoryCharacterToByteStream error = InMemoryCharacterToByteStream.create();
                        final InMemoryFileSystem fileSystem = InMemoryFileSystem.create(test.getClock());
                        fileSystem.createRoot("/").await();
                        final Folder currentFolder = fileSystem.getFolder("/current/folder/").await();
                        final FakeProcessFactory processFactory = FakeProcessFactory.create(test.getParallelAsyncRunner(), currentFolder);
                        final FakeDefaultApplicationLauncher defaultApplicationLauncher = FakeDefaultApplicationLauncher.create(fileSystem);
                        final QubFolder qubFolder = QubFolder.get(fileSystem.getFolder("/qub/").await());
                        final File qubBuildCompiledSourcesFile = qubFolder.getCompiledSourcesFile("qub", "build-java", "7").await();
                        qubBuildCompiledSourcesFile.create().await();
                        final File qubTestCompiledSourcesFile = qubFolder.getCompiledSourcesFile("qub", "test-java", "8").await();
                        qubTestCompiledSourcesFile.create().await();
                        final EnvironmentVariables environmentVariables = EnvironmentVariables.create()
                            .set("QUB_HOME", qubFolder.toString());
                        final String jvmClassPath = "/fake-jvm-classpath";
                        final FakeTypeLoader typeLoader = FakeTypeLoader.create()
                            .addTypeContainer(QubBuild.class, qubBuildCompiledSourcesFile)
                            .addTypeContainer(QubTest.class, qubTestCompiledSourcesFile);
                        final QubPackParameters parameters = new QubPackParameters(output, error, currentFolder, environmentVariables, processFactory, defaultApplicationLauncher, jvmClassPath, typeLoader);

                        final QubPackParameters setProfilerResult = parameters.setProfiler(profiler);
                        test.assertSame(parameters, setProfilerResult);
                        test.assertEqual(profiler, parameters.getProfiler());
                    });
                };

                setProfiler.run(false);
                setProfiler.run(true);
            });

            runner.testGroup("setWarnings(Warnings)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final InMemoryCharacterToByteStream output = InMemoryCharacterToByteStream.create();
                    final InMemoryCharacterToByteStream error = InMemoryCharacterToByteStream.create();
                    final InMemoryFileSystem fileSystem = InMemoryFileSystem.create(test.getClock());
                    fileSystem.createRoot("/").await();
                    final Folder currentFolder = fileSystem.getFolder("/current/folder/").await();
                    final FakeProcessFactory processFactory = FakeProcessFactory.create(test.getParallelAsyncRunner(), currentFolder);
                    final FakeDefaultApplicationLauncher defaultApplicationLauncher = FakeDefaultApplicationLauncher.create(fileSystem);
                    final QubFolder qubFolder = QubFolder.get(fileSystem.getFolder("/qub/").await());
                    final File qubBuildCompiledSourcesFile = qubFolder.getCompiledSourcesFile("qub", "build-java", "7").await();
                    qubBuildCompiledSourcesFile.create().await();
                    final File qubTestCompiledSourcesFile = qubFolder.getCompiledSourcesFile("qub", "test-java", "8").await();
                    qubTestCompiledSourcesFile.create().await();
                    final EnvironmentVariables environmentVariables = EnvironmentVariables.create()
                        .set("QUB_HOME", qubFolder.toString());
                    final String jvmClassPath = "/fake-jvm-classpath";
                    final FakeTypeLoader typeLoader = FakeTypeLoader.create()
                        .addTypeContainer(QubBuild.class, qubBuildCompiledSourcesFile)
                        .addTypeContainer(QubTest.class, qubTestCompiledSourcesFile);
                    final QubPackParameters parameters = new QubPackParameters(output, error, currentFolder, environmentVariables, processFactory, defaultApplicationLauncher, jvmClassPath, typeLoader);

                    test.assertThrows(() -> parameters.setWarnings(null),
                        new PreConditionFailure("warnings cannot be null."));
                    test.assertEqual(Warnings.Show, parameters.getWarnings());
                });

                final Action1<Warnings> setWarningsTest = (Warnings warnings) ->
                {
                    runner.test("with " + warnings, (Test test) ->
                    {
                        final InMemoryCharacterToByteStream output = InMemoryCharacterToByteStream.create();
                        final InMemoryCharacterToByteStream error = InMemoryCharacterToByteStream.create();
                        final InMemoryFileSystem fileSystem = InMemoryFileSystem.create(test.getClock());
                        fileSystem.createRoot("/").await();
                        final Folder currentFolder = fileSystem.getFolder("/current/folder/").await();
                        final FakeProcessFactory processFactory = FakeProcessFactory.create(test.getParallelAsyncRunner(), currentFolder);
                        final FakeDefaultApplicationLauncher defaultApplicationLauncher = FakeDefaultApplicationLauncher.create(fileSystem);
                        final QubFolder qubFolder = QubFolder.get(fileSystem.getFolder("/qub/").await());
                        final File qubBuildCompiledSourcesFile = qubFolder.getCompiledSourcesFile("qub", "build-java", "7").await();
                        qubBuildCompiledSourcesFile.create().await();
                        final File qubTestCompiledSourcesFile = qubFolder.getCompiledSourcesFile("qub", "test-java", "8").await();
                        qubTestCompiledSourcesFile.create().await();
                        final EnvironmentVariables environmentVariables = EnvironmentVariables.create()
                            .set("QUB_HOME", qubFolder.toString());
                        final String jvmClassPath = "/fake-jvm-classpath";
                    final FakeTypeLoader typeLoader = FakeTypeLoader.create()
                        .addTypeContainer(QubBuild.class, qubBuildCompiledSourcesFile)
                        .addTypeContainer(QubTest.class, qubTestCompiledSourcesFile);
                        final QubPackParameters parameters = new QubPackParameters(output, error, currentFolder, environmentVariables, processFactory, defaultApplicationLauncher, jvmClassPath, typeLoader);

                        final QubPackParameters setWarningsResult = parameters.setWarnings(warnings);
                        test.assertSame(parameters, setWarningsResult);
                        test.assertEqual(warnings, parameters.getWarnings());
                    });
                };

                for (final Warnings warnings : Warnings.values())
                {
                    setWarningsTest.run(warnings);
                }
            });

            runner.testGroup("setBuildJson(boolean)", () ->
            {
                final Action1<Boolean> setBuildJson = (Boolean buildJson) ->
                {
                    runner.test("with " + buildJson, (Test test) ->
                    {
                        final InMemoryCharacterToByteStream output = InMemoryCharacterToByteStream.create();
                        final InMemoryCharacterToByteStream error = InMemoryCharacterToByteStream.create();
                        final InMemoryFileSystem fileSystem = InMemoryFileSystem.create(test.getClock());
                        fileSystem.createRoot("/").await();
                        final Folder currentFolder = fileSystem.getFolder("/current/folder/").await();
                        final FakeProcessFactory processFactory = FakeProcessFactory.create(test.getParallelAsyncRunner(), currentFolder);
                        final FakeDefaultApplicationLauncher defaultApplicationLauncher = FakeDefaultApplicationLauncher.create(fileSystem);
                        final QubFolder qubFolder = QubFolder.get(fileSystem.getFolder("/qub/").await());
                        final File qubBuildCompiledSourcesFile = qubFolder.getCompiledSourcesFile("qub", "build-java", "7").await();
                        qubBuildCompiledSourcesFile.create().await();
                        final File qubTestCompiledSourcesFile = qubFolder.getCompiledSourcesFile("qub", "test-java", "8").await();
                        qubTestCompiledSourcesFile.create().await();
                        final EnvironmentVariables environmentVariables = EnvironmentVariables.create()
                            .set("QUB_HOME", qubFolder.toString());
                        final String jvmClassPath = "/fake-jvm-classpath";
                        final FakeTypeLoader typeLoader = FakeTypeLoader.create()
                            .addTypeContainer(QubBuild.class, qubBuildCompiledSourcesFile)
                            .addTypeContainer(QubTest.class, qubTestCompiledSourcesFile);
                        final QubPackParameters parameters = new QubPackParameters(output, error, currentFolder, environmentVariables, processFactory, defaultApplicationLauncher, jvmClassPath, typeLoader);

                        final QubPackParameters setBuildJsonResult = parameters.setBuildJson(buildJson);
                        test.assertSame(parameters, setBuildJsonResult);
                        test.assertEqual(buildJson, parameters.getBuildJson());
                    });
                };

                setBuildJson.run(false);
                setBuildJson.run(true);
            });

            runner.testGroup("setVerbose(VerboseCharacterWriteStream)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final InMemoryCharacterToByteStream output = InMemoryCharacterToByteStream.create();
                    final InMemoryCharacterToByteStream error = InMemoryCharacterToByteStream.create();
                    final InMemoryFileSystem fileSystem = InMemoryFileSystem.create(test.getClock());
                    fileSystem.createRoot("/").await();
                    final Folder currentFolder = fileSystem.getFolder("/current/folder/").await();
                    final FakeProcessFactory processFactory = FakeProcessFactory.create(test.getParallelAsyncRunner(), currentFolder);
                    final FakeDefaultApplicationLauncher defaultApplicationLauncher = FakeDefaultApplicationLauncher.create(fileSystem);
                    final QubFolder qubFolder = QubFolder.get(fileSystem.getFolder("/qub/").await());
                    final File qubBuildCompiledSourcesFile = qubFolder.getCompiledSourcesFile("qub", "build-java", "7").await();
                    qubBuildCompiledSourcesFile.create().await();
                    final File qubTestCompiledSourcesFile = qubFolder.getCompiledSourcesFile("qub", "test-java", "8").await();
                    qubTestCompiledSourcesFile.create().await();
                    final EnvironmentVariables environmentVariables = EnvironmentVariables.create()
                        .set("QUB_HOME", qubFolder.toString());
                    final String jvmClassPath = "/fake-jvm-classpath";
                    final FakeTypeLoader typeLoader = FakeTypeLoader.create()
                        .addTypeContainer(QubBuild.class, qubBuildCompiledSourcesFile)
                        .addTypeContainer(QubTest.class, qubTestCompiledSourcesFile);
                    final QubPackParameters parameters = new QubPackParameters(output, error, currentFolder, environmentVariables, processFactory, defaultApplicationLauncher, jvmClassPath, typeLoader);

                    test.assertThrows(() -> parameters.setVerbose(null),
                        new PreConditionFailure("verbose cannot be null."));
                    final VerboseCharacterToByteWriteStream verbose = parameters.getVerbose();
                    test.assertNotNull(verbose);
                    test.assertFalse(verbose.isVerbose());
                });

                runner.test("with non-null", (Test test) ->
                {
                    final InMemoryCharacterToByteStream output = InMemoryCharacterToByteStream.create();
                    final InMemoryCharacterToByteStream error = InMemoryCharacterToByteStream.create();
                    final InMemoryFileSystem fileSystem = InMemoryFileSystem.create(test.getClock());
                    fileSystem.createRoot("/").await();
                    final Folder currentFolder = fileSystem.getFolder("/current/folder/").await();
                    final FakeProcessFactory processFactory = FakeProcessFactory.create(test.getParallelAsyncRunner(), currentFolder);
                    final FakeDefaultApplicationLauncher defaultApplicationLauncher = FakeDefaultApplicationLauncher.create(fileSystem);
                    final QubFolder qubFolder = QubFolder.get(fileSystem.getFolder("/qub/").await());
                    final File qubBuildCompiledSourcesFile = qubFolder.getCompiledSourcesFile("qub", "build-java", "7").await();
                    qubBuildCompiledSourcesFile.create().await();
                    final File qubTestCompiledSourcesFile = qubFolder.getCompiledSourcesFile("qub", "test-java", "8").await();
                    qubTestCompiledSourcesFile.create().await();
                    final EnvironmentVariables environmentVariables = EnvironmentVariables.create()
                        .set("QUB_HOME", qubFolder.toString());
                    final String jvmClassPath = "/fake-jvm-classpath";
                    final FakeTypeLoader typeLoader = FakeTypeLoader.create()
                        .addTypeContainer(QubBuild.class, qubBuildCompiledSourcesFile)
                        .addTypeContainer(QubTest.class, qubTestCompiledSourcesFile);
                    final QubPackParameters parameters = new QubPackParameters(output, error, currentFolder, environmentVariables, processFactory, defaultApplicationLauncher, jvmClassPath, typeLoader);

                    final VerboseCharacterToByteWriteStream verbose = VerboseCharacterToByteWriteStream.create(output);
                    final QubPackParameters setVerboseResult = parameters.setVerbose(verbose);
                    test.assertSame(parameters, setVerboseResult);
                    test.assertSame(verbose, parameters.getVerbose());
                });
            });
        });
    }
}
