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
                        final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                        final Folder folderToPack = fileSystem.getFolder("/hello").await();
                        final EnvironmentVariables environmentVariables = new EnvironmentVariables();
                        final FakeProcessFactory processFactory = new FakeProcessFactory(test.getMainAsyncRunner(), folderToPack);
                        final FakeDefaultApplicationLauncher defaultApplicationLauncher = new FakeDefaultApplicationLauncher();
                        final String jvmClassPath = "fake;jvm;class;path";
                        final QubPackParameters parameters = new QubPackParameters(output, error, folderToPack, environmentVariables, processFactory, defaultApplicationLauncher, jvmClassPath);

                        test.<QubPackParameters>assertSame(parameters, parameters.setPattern(pattern));
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
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    final Folder folderToPack = fileSystem.getFolder("/hello").await();
                    final EnvironmentVariables environmentVariables = new EnvironmentVariables();
                    final FakeProcessFactory processFactory = new FakeProcessFactory(test.getMainAsyncRunner(), folderToPack);
                    final FakeDefaultApplicationLauncher defaultApplicationLauncher = new FakeDefaultApplicationLauncher();
                    final String jvmClassPath = "fake;jvm;class;path";
                    final QubPackParameters parameters = new QubPackParameters(output, error, folderToPack, environmentVariables, processFactory, defaultApplicationLauncher, jvmClassPath);

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
                        final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                        final Folder folderToPack = fileSystem.getFolder("/hello").await();
                        final EnvironmentVariables environmentVariables = new EnvironmentVariables();
                        final FakeProcessFactory processFactory = new FakeProcessFactory(test.getMainAsyncRunner(), folderToPack);
                        final FakeDefaultApplicationLauncher defaultApplicationLauncher = new FakeDefaultApplicationLauncher();
                        final String jvmClassPath = "fake;jvm;class;path";
                        final QubPackParameters parameters = new QubPackParameters(output, error, folderToPack, environmentVariables, processFactory, defaultApplicationLauncher, jvmClassPath);

                        test.<QubPackParameters>assertSame(parameters, parameters.setCoverage(coverage));
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
                        final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                        final Folder folderToPack = fileSystem.getFolder("/hello").await();
                        final EnvironmentVariables environmentVariables = new EnvironmentVariables();
                        final FakeProcessFactory processFactory = new FakeProcessFactory(test.getMainAsyncRunner(), folderToPack);
                        final FakeDefaultApplicationLauncher defaultApplicationLauncher = new FakeDefaultApplicationLauncher();
                        final String jvmClassPath = "fake;jvm;class;path";
                        final QubPackParameters parameters = new QubPackParameters(output, error, folderToPack, environmentVariables, processFactory, defaultApplicationLauncher, jvmClassPath);

                        test.<QubPackParameters>assertSame(parameters, parameters.setTestJson(testJson));
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
                        final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                        final Folder folderToPack = fileSystem.getFolder("/hello").await();
                        final EnvironmentVariables environmentVariables = new EnvironmentVariables();
                        final FakeProcessFactory processFactory = new FakeProcessFactory(test.getMainAsyncRunner(), folderToPack);
                        final FakeDefaultApplicationLauncher defaultApplicationLauncher = new FakeDefaultApplicationLauncher();
                        final String jvmClassPath = "fake;jvm;class;path";
                        final QubPackParameters parameters = new QubPackParameters(output, error, folderToPack, environmentVariables, processFactory, defaultApplicationLauncher, jvmClassPath);

                        test.<QubPackParameters>assertSame(parameters, parameters.setJvmClassPath(newJvmClassPath));
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
                        final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                        final Folder folderToPack = fileSystem.getFolder("/hello").await();
                        final EnvironmentVariables environmentVariables = new EnvironmentVariables();
                        final FakeProcessFactory processFactory = new FakeProcessFactory(test.getMainAsyncRunner(), folderToPack);
                        final FakeDefaultApplicationLauncher defaultApplicationLauncher = new FakeDefaultApplicationLauncher();
                        final String jvmClassPath = "fake;jvm;class;path";
                        final QubPackParameters parameters = new QubPackParameters(output, error, folderToPack, environmentVariables, processFactory, defaultApplicationLauncher, jvmClassPath);

                        test.<QubPackParameters>assertSame(parameters, parameters.setProfiler(profiler));
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
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    final Folder folderToPack = fileSystem.getFolder("/hello").await();
                    final EnvironmentVariables environmentVariables = new EnvironmentVariables();
                    final FakeProcessFactory processFactory = new FakeProcessFactory(test.getMainAsyncRunner(), folderToPack);
                    final FakeDefaultApplicationLauncher defaultApplicationLauncher = new FakeDefaultApplicationLauncher();
                    final String jvmClassPath = "fake;jvm;class;path";
                    final QubPackParameters parameters = new QubPackParameters(output, error, folderToPack, environmentVariables, processFactory, defaultApplicationLauncher, jvmClassPath);

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
                        final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                        final Folder folderToPack = fileSystem.getFolder("/hello").await();
                        final EnvironmentVariables environmentVariables = new EnvironmentVariables();
                        final FakeProcessFactory processFactory = new FakeProcessFactory(test.getMainAsyncRunner(), folderToPack);
                        final FakeDefaultApplicationLauncher defaultApplicationLauncher = new FakeDefaultApplicationLauncher();
                        final String jvmClassPath = "fake;jvm;class;path";
                        final QubPackParameters parameters = new QubPackParameters(output, error, folderToPack, environmentVariables, processFactory, defaultApplicationLauncher, jvmClassPath);

                        test.<QubPackParameters>assertSame(parameters, parameters.setWarnings(warnings));
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
                        final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                        final Folder folderToPack = fileSystem.getFolder("/hello").await();
                        final EnvironmentVariables environmentVariables = new EnvironmentVariables();
                        final FakeProcessFactory processFactory = new FakeProcessFactory(test.getMainAsyncRunner(), folderToPack);
                        final FakeDefaultApplicationLauncher defaultApplicationLauncher = new FakeDefaultApplicationLauncher();
                        final String jvmClassPath = "fake;jvm;class;path";
                        final QubPackParameters parameters = new QubPackParameters(output, error, folderToPack, environmentVariables, processFactory, defaultApplicationLauncher, jvmClassPath);

                        test.<QubPackParameters>assertSame(parameters, parameters.setBuildJson(buildJson));
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
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    final Folder folderToPack = fileSystem.getFolder("/hello").await();
                    final EnvironmentVariables environmentVariables = new EnvironmentVariables();
                    final FakeProcessFactory processFactory = new FakeProcessFactory(test.getMainAsyncRunner(), folderToPack);
                    final FakeDefaultApplicationLauncher defaultApplicationLauncher = new FakeDefaultApplicationLauncher();
                    final String jvmClassPath = "fake;jvm;class;path";
                    final QubPackParameters parameters = new QubPackParameters(output, error, folderToPack, environmentVariables, processFactory, defaultApplicationLauncher, jvmClassPath);

                    test.assertThrows(() -> parameters.setVerbose(null),
                        new PreConditionFailure("verbose cannot be null."));
                    final VerboseCharacterWriteStream verbose = parameters.getVerbose();
                    test.assertNotNull(verbose);
                    test.assertFalse(verbose.isVerbose());
                });

                runner.test("with non-null", (Test test) ->
                {
                    final InMemoryCharacterToByteStream output = InMemoryCharacterToByteStream.create();
                    final InMemoryCharacterToByteStream error = InMemoryCharacterToByteStream.create();
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    final Folder folderToPack = fileSystem.getFolder("/hello").await();
                    final EnvironmentVariables environmentVariables = new EnvironmentVariables();
                    final FakeProcessFactory processFactory = new FakeProcessFactory(test.getMainAsyncRunner(), folderToPack);
                    final FakeDefaultApplicationLauncher defaultApplicationLauncher = new FakeDefaultApplicationLauncher();
                    final String jvmClassPath = "fake;jvm;class;path";
                    final QubPackParameters parameters = new QubPackParameters(output, error, folderToPack, environmentVariables, processFactory, defaultApplicationLauncher, jvmClassPath);

                    final VerboseCharacterWriteStream verbose = new VerboseCharacterWriteStream(true, output);
                    test.<QubPackParameters>assertSame(parameters, parameters.setVerbose(verbose));
                    test.assertSame(verbose, parameters.getVerbose());
                });
            });
        });
    }
}
