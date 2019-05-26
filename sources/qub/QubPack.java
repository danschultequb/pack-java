package qub;

public class QubPack
{
    private QubTest qubTest;
    private Boolean showTotalDuration;

    /**
     * Set the QubTest object that will be used to test the source code.
     * @param qubTest The QubTest object that will be used to test the source code.
     * @return This object for method chaining.
     */
    public QubPack setQubTest(QubTest qubTest)
    {
        this.qubTest = qubTest;
        return this;
    }

    /**
     * Get the QubTest object that will be used to test the source code. If no QubTest object has
     * been set, a default one will be created and returned.
     * @return The QubTest object that will be used to test the source code.
     */
    public QubTest getQubTest()
    {
        if (qubTest == null)
        {
            qubTest = new QubTest();
        }
        final QubTest result = qubTest;

        PostCondition.assertNotNull(result, "result");

        return result;
    }

    public void setShowTotalDuration(boolean showTotalDuration)
    {
        this.showTotalDuration = showTotalDuration;
    }

    public boolean getShowTotalDuration()
    {
        if (showTotalDuration == null)
        {
            showTotalDuration = true;
        }
        return showTotalDuration;
    }

    public void main(Console console)
    {
        PreCondition.assertNotNull(console, "console");

        if (shouldShowUsage(console))
        {
            console.writeLine("Usage: qub-pack [[-folder=]<folder-path-to-pack>] [-verbose]").await();
            console.writeLine("  Used to package source, test, and compiled code in source code projects.").await();
            console.writeLine("  -folder: The folder to pack. This can be specified either with the -folder").await();
            console.writeLine("           argument name or without it.").await();
            console.writeLine("  -verbose: Whether or not to show verbose logs.").await();
            console.setExitCode(-1);
        }
        else
        {
            final boolean profiler = Profiler.takeProfilerArgument(console);
            if (profiler)
            {
                Profiler.waitForProfiler(console, QubPack.class).await();
            }

            final boolean showTotalDuration = getShowTotalDuration();
            final Stopwatch stopwatch = console.getStopwatch();
            if (showTotalDuration)
            {
                stopwatch.start();
            }
            try
            {
                final QubTest qubTest = getQubTest();
                qubTest.setShowTotalDuration(false);
                qubTest.main(console);

                if (console.getExitCode() == 0)
                {
                    console.writeLine("Creating packages...").await();

                    final Folder folderToPack = getFolderToPack(console);
                    
                    final Folder outputFolder = folderToPack.getFolder("outputs").await();
                    final Folder sourceFolder = folderToPack.getFolder("sources").await();
                    final Folder testFolder = folderToPack.getFolder("tests").await();

                    final String qubHome = console.getEnvironmentVariable("QUB_HOME");
                    final Folder qubFolder = console.getFileSystem().getFolder(qubHome).await();
                    
                    final File projectJsonFile = folderToPack.getFile("project.json").await();
                    final ProjectJSON projectJson = ProjectJSON.parse(projectJsonFile).await();
                }
            }
            finally
            {
                if (showTotalDuration)
                {
                    final Duration compilationDuration = stopwatch.stop().toSeconds();
                    console.writeLine("Done (" + compilationDuration.toString("0.0") + ")").await();
                }
            }
        }
    }

    private static boolean shouldShowUsage(Console console)
    {
        PreCondition.assertNotNull(console, "console");

        return console.getCommandLine().contains(
            (CommandLineArgument argument) ->
            {
                final String argumentString = argument.toString();
                return argumentString.equals("/?") || argumentString.equals("-?");
            });
    }

    private static Path getFolderPathToPack(Console console)
    {
        PreCondition.assertNotNull(console, "console");

        Path result = null;
        final CommandLine commandLine = console.getCommandLine();
        if (commandLine.any())
        {
            CommandLineArgument folderArgument = commandLine.get("folder");
            if (folderArgument == null)
            {
                folderArgument = commandLine.getArguments()
                    .first((CommandLineArgument argument) -> argument.getName() == null);
            }
            if (folderArgument != null)
            {
                result = Path.parse(folderArgument.getValue());
            }
        }

        if (result == null)
        {
            result = console.getCurrentFolderPath();
        }

        if (!result.isRooted())
        {
            result = console.getCurrentFolderPath().resolve(result).await();
        }

        PostCondition.assertNotNull(result, "result");
        PostCondition.assertTrue(result.isRooted(), "result.isRooted()");

        return result;
    }

    private static Folder getFolderToPack(Console console)
    {
        PreCondition.assertNotNull(console, "console");

        final Path folderPathToPack = getFolderPathToPack(console);
        final FileSystem fileSystem = console.getFileSystem();
        final Folder result = fileSystem.getFolder(folderPathToPack).await();

        PostCondition.assertNotNull(result, "result");

        return result;
    }

    public static boolean isVerbose(Console console)
    {
        boolean result = false;

        CommandLineArgument verboseArgument = console.getCommandLine().get("verbose");
        if (verboseArgument != null)
        {
            final String verboseArgumentValue = verboseArgument.getValue();
            result = Strings.isNullOrEmpty(verboseArgumentValue) ||
                Booleans.isTrue(java.lang.Boolean.valueOf(verboseArgumentValue));
        }

        return result;
    }

    public static Result<Void> verbose(Console console, String message)
    {
        return verbose(console, false, message);
    }

    public static Result<Void> verbose(Console console, boolean showTimestamp, String message)
    {
        PreCondition.assertNotNull(console, "console");
        PreCondition.assertNotNull(message, "message");

        Result<Void> result = Result.success();
        if (isVerbose(console))
        {
            result = console.writeLine("VERBOSE" + (showTimestamp ? "(" + System.currentTimeMillis() + ")" : "") + ": " + message)
                .then(() -> {});
        }

        PostCondition.assertNotNull(result, "result");

        return result;
    }

    public static Result<Void> error(Console console, String message)
    {
        return error(console, false, message);
    }

    public static Result<Void> error(Console console, boolean showTimestamp, String message)
    {
        PreCondition.assertNotNull(console, "console");
        PreCondition.assertNotNull(message, "message");

        final Result<Void> result = console.writeLine("ERROR" + (showTimestamp ? "(" + System.currentTimeMillis() + ")" : "") + ": " + message).then(() -> {});
        console.incrementExitCode();

        PostCondition.assertNotNull(result, "result");

        return result;
    }

    public static void main(String[] args)
    {
        Console.run(args, (Console console) -> new QubTest().main(console));
    }
}