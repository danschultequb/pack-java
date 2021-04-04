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
                    runner.test("with " + Strings.escapeAndQuote(pattern),
                        (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                        (Test test, FakeDesktopProcess process) ->
                    {
                        final QubPackParameters parameters = QubPackParametersTests.getParameters(process);

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
                runner.test("with null",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final QubPackParameters parameters = QubPackParametersTests.getParameters(process);

                    test.assertThrows(() -> parameters.setCoverage(null),
                        new PreConditionFailure("coverage cannot be null."));
                    test.assertEqual(Coverage.None, parameters.getCoverage());
                });

                final Action1<Coverage> setCoverageTest = (Coverage coverage) ->
                {
                    runner.test("with " + coverage,
                        (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                        (Test test, FakeDesktopProcess process) ->
                    {
                        final QubPackParameters parameters = QubPackParametersTests.getParameters(process);

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
                    runner.test("with " + testJson,
                        (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                        (Test test, FakeDesktopProcess process) ->
                    {
                        final QubPackParameters parameters = QubPackParametersTests.getParameters(process);

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
                    runner.test("with " + Strings.escapeAndQuote(newJvmClassPath),
                        (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                        (Test test, FakeDesktopProcess process) ->
                    {
                        final QubPackParameters parameters = QubPackParametersTests.getParameters(process);

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
                    runner.test("with " + profiler,
                        (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                        (Test test, FakeDesktopProcess process) ->
                    {
                        final QubPackParameters parameters = QubPackParametersTests.getParameters(process);

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
                runner.test("with null",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final QubPackParameters parameters = QubPackParametersTests.getParameters(process);

                    test.assertThrows(() -> parameters.setWarnings(null),
                    new PreConditionFailure("warnings cannot be null."));
                    test.assertEqual(Warnings.Show, parameters.getWarnings());
                });

                final Action1<Warnings> setWarningsTest = (Warnings warnings) ->
                {
                    runner.test("with " + warnings,
                        (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                        (Test test, FakeDesktopProcess process) ->
                    {
                        final QubPackParameters parameters = QubPackParametersTests.getParameters(process);

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
                    runner.test("with " + buildJson,
                        (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                        (Test test, FakeDesktopProcess process) ->
                    {
                        final QubPackParameters parameters = QubPackParametersTests.getParameters(process);

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
                runner.test("with null",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final QubPackParameters parameters = QubPackParametersTests.getParameters(process);

                    test.assertThrows(() -> parameters.setVerbose(null),
                        new PreConditionFailure("verbose cannot be null."));
                    final VerboseCharacterToByteWriteStream verbose = parameters.getVerbose();
                    test.assertNotNull(verbose);
                    test.assertFalse(verbose.isVerbose());
                });

                runner.test("with non-null",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final QubPackParameters parameters = QubPackParametersTests.getParameters(process);

                    final VerboseCharacterToByteWriteStream verbose = VerboseCharacterToByteWriteStream.create(process.getOutputWriteStream());
                    final QubPackParameters setVerboseResult = parameters.setVerbose(verbose);
                    test.assertSame(parameters, setVerboseResult);
                    test.assertSame(verbose, parameters.getVerbose());
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
