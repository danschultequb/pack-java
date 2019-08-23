package qub;

public class QubPack
{
    private QubTest qubTest;
    private JarCreator jarCreator;
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

    /**
     * Set the JarCreator that will be used to create jar files.
     * @param jarCreator The JarCreator that will be used to create jar files.
     * @return This object for method chaining.
     */
    public QubPack setJarCreator(JarCreator jarCreator)
    {
        this.jarCreator = jarCreator;
        return this;
    }

    /**
     * Get the JarCreator that will be used to create jar files. If no JarCreator has been set, a
     * default one will be created and returned.
     * @return The JarCreator that will be used to create jar files.
     */
    public JarCreator getJarCreator()
    {
        if (jarCreator == null)
        {
            jarCreator = new JavaJarCreator();
        }
        final JarCreator result = jarCreator;

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

        final CommandLineParameters parameters = console.createCommandLineParameters();
        final CommandLineParameter<Folder> folderToPackParameter = parameters.addPositionalFolder("folder", console)
            .setValueName("<folder-to-pack>")
            .setDescription("The folder to pack. Defaults to the current folder.");
        final CommandLineParameterVerbose verbose = parameters.addVerbose(console);
        final CommandLineParameterProfiler profiler = parameters.addProfiler(console, QubPack.class);
        final CommandLineParameterBoolean help = parameters.addHelp();

        if (help.getValue().await())
        {
            parameters.writeHelpLines(console, "qub-pack", "Used to package source and compiled code in source code projects.").await();
            console.setExitCode(-1);
        }
        else
        {
            profiler.await();
            profiler.removeValue().await();

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
                    final JarCreator jarCreator = getJarCreator();

                    final Folder folderToPack = folderToPackParameter.getValue().await();

                    final Folder outputFolder = folderToPack.getFolder("outputs").await();
                    final Iterable<File> outputClassFiles = outputFolder.getFilesRecursively().await()
                        .where((File file) -> Comparer.equal(file.getFileExtension(), ".class"))
                        .toList();

                    final Folder sourceFolder = folderToPack.getFolder("sources").await();
                    final Iterable<File> sourceJavaFiles = sourceFolder.getFilesRecursively().await()
                        .where((File file) -> Comparer.equal(file.getFileExtension(), ".java"))
                        .toList();

                    final File projectJsonFile = folderToPack.getFile("project.json").await();
                    final ProjectJSON projectJson = ProjectJSON.parse(projectJsonFile).await();

                    console.writeLine("Creating sources jar file...").await();
                    jarCreator.setBaseFolder(sourceFolder);
                    jarCreator.setJarName(projectJson.getProject() + ".sources");
                    jarCreator.setFiles(sourceJavaFiles);
                    final File sourcesJarFile = jarCreator.createJarFile(console, verbose.getValue().await()).await();
                    final File sourcesJarFileInOutputsFolder = outputFolder.getFile(sourcesJarFile.getName()).await();
                    sourcesJarFile.copyTo(sourcesJarFileInOutputsFolder).await();
                    sourcesJarFile.delete().await();
                    verbose.writeLine("Created " + sourcesJarFileInOutputsFolder + ".").await();

                    console.writeLine("Creating compiled sources jar file...").await();
                    jarCreator.setBaseFolder(outputFolder);
                    jarCreator.setJarName(projectJson.getProject());
                    final String mainClass = projectJson.getJava().getMainClass();
                    if (!Strings.isNullOrEmpty(mainClass))
                    {
                        final File manifestFile = outputFolder.getFile("META-INF/MANIFEST.MF").await();
                        final String manifestFileContents =
                            "Manifest-Version: 1.0\n" +
                            "Main-Class: " + mainClass + "\n";
                        manifestFile.setContentsAsString(manifestFileContents).await();
                        jarCreator.setManifestFile(manifestFile);
                    }
                    jarCreator.setFiles(QubPack.getSourceClassFiles(outputFolder, outputClassFiles, sourceFolder, sourceJavaFiles));
                    final File compiledSourcesJarFile = jarCreator.createJarFile(console, verbose.getValue().await()).await();
                    verbose.writeLine("Created " + compiledSourcesJarFile + ".").await();

                    final Folder testFolder = folderToPack.getFolder("tests").await();
                    if (testFolder.exists().await())
                    {
                        console.writeLine("Creating compiled tests jar file...").await();
                        final Iterable<File> testJavaFiles = testFolder.getFilesRecursively().await()
                            .where((File file) -> Comparer.equal(file.getFileExtension(), ".java"))
                            .toList();
                        jarCreator.setBaseFolder(outputFolder);
                        jarCreator.setJarName(projectJson.getProject() + ".tests");
                        jarCreator.setFiles(QubPack.getSourceClassFiles(outputFolder, outputClassFiles, testFolder, testJavaFiles));
                        final File compiledTestsJarFile = jarCreator.createJarFile(console, verbose.getValue().await()).await();
                        verbose.writeLine("Created " + compiledTestsJarFile + ".").await();
                    }
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

    public static boolean isSourceClassFile(Folder outputFolder, File outputClassFile, Folder sourceFolder, Iterable<File> sourceJavaFiles)
    {
        PreCondition.assertNotNull(outputFolder, "outputFolder");
        PreCondition.assertNotNull(outputClassFile, "outputClassFile");
        PreCondition.assertNotNull(sourceFolder, "sourceFolder");
        PreCondition.assertNotNull(sourceJavaFiles, "sourceJavaFiles");

        Path outputClassFilePath = outputClassFile.relativeTo(outputFolder).withoutFileExtension();
        if (outputClassFilePath.getSegments().last().contains("$"))
        {
            final String outputClassFileRelativePathString = outputClassFilePath.toString();
            final int dollarSignIndex = outputClassFileRelativePathString.lastIndexOf('$');
            final String outputClassFileRelativePathStringWithoutDollarSign = outputClassFileRelativePathString.substring(0, dollarSignIndex);
            outputClassFilePath = Path.parse(outputClassFileRelativePathStringWithoutDollarSign);
        }
        final Path outputClassFileRelativePath = outputClassFilePath;
        return sourceJavaFiles.contains((File sourceJavaFile) ->
        {
            final Path sourceJavaFileRelativePath = sourceJavaFile.relativeTo(sourceFolder).withoutFileExtension();
            return outputClassFileRelativePath.equals(sourceJavaFileRelativePath);
        });
    }

    public static Iterable<File> getSourceClassFiles(Folder outputFolder, Iterable<File> outputClassFiles, Folder sourceFolder, Iterable<File> sourceJavaFiles)
    {
        return outputClassFiles
            .where((File outputClassFile) ->
            {
                return QubPack.isSourceClassFile(outputFolder, outputClassFile, sourceFolder, sourceJavaFiles);
            });
    }

    public static void main(String[] args)
    {
        Console.run(args, (Console console) -> new QubPack().main(console));
    }
}