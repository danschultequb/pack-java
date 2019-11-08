package qub;

public class QubPack
{
    public static void main(String[] args)
    {
        Console.run(args, (Console console) -> new QubPack().main(console));
    }

    public void main(Console console)
    {
        PreCondition.assertNotNull(console, "console");

        final QubPackParameters parameters = QubPack.getParameters(console);
        if (parameters != null)
        {
            final Stopwatch stopwatch = console.getStopwatch();
            stopwatch.start();
            try
            {
                console.setExitCode(this.run(parameters));
            }
            finally
            {
                final Duration compilationDuration = stopwatch.stop().toSeconds();
                console.writeLine("Done (" + compilationDuration.toString("0.0") + ")").await();
            }
        }
    }

    private JarCreator jarCreator;

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

    public static CommandLineParameter<Folder> addFolderToPack(CommandLineParameters parameters, Process process)
    {
        PreCondition.assertNotNull(parameters, "parameters");
        PreCondition.assertNotNull(process, "process");

        return parameters.addPositionalFolder("folder", process)
            .setValueName("<folder-to-pack>")
            .setDescription("The folder to pack. Defaults to the current folder.");
    }

    /**
     * Get the QubPackParameters from the provided Process.
     * @param process The process to get the QubPackParameters from.
     * @return The QubPackParameters.
     */
    public static QubPackParameters getParameters(Process process)
    {
        PreCondition.assertNotNull(process, "process");

        final CommandLineParameters parameters = process.createCommandLineParameters()
            .setApplicationName("qub-pack")
            .setApplicationDescription("Used to package source and compiled code in source code projects.");
        final CommandLineParameter<Folder> folderToPackParameter = QubPack.addFolderToPack(parameters, process);
        final CommandLineParameterBoolean testJsonParameter = QubTest.addTestJsonParameter(parameters);
        final CommandLineParameter<String> jvmClassPathParameter = QubTest.addJvmClassPathParameter(parameters);
        final CommandLineParameterBoolean buildJsonParameter = QubBuild.addBuildJsonParameter(parameters);
        final CommandLineParameter<Warnings> warningsParameter = QubBuild.addWarningsParameter(parameters);
        final CommandLineParameterVerbose verboseParameter = parameters.addVerbose(process);
        final CommandLineParameterProfiler profilerParameter = parameters.addProfiler(process, QubPack.class);
        final CommandLineParameterHelp helpParameter = parameters.addHelp();

        QubPackParameters result = null;
        if (!helpParameter.showApplicationHelpLines(process).await())
        {
            profilerParameter.await();
            profilerParameter.removeValue().await();

            final ByteWriteStream output = process.getOutputByteWriteStream();
            final ByteWriteStream error = process.getErrorByteWriteStream();
            final DefaultApplicationLauncher defaultApplicationLauncher = process.getDefaultApplicationLauncher();
            final Folder folderToPack = folderToPackParameter.getValue().await();
            final EnvironmentVariables environmentVariables = process.getEnvironmentVariables();
            final ProcessFactory processFactory = process.getProcessFactory();
            final boolean testJson = testJsonParameter.removeValue().await();
            final String jvmClassPath = jvmClassPathParameter.removeValue().await();
            final boolean buildJson = buildJsonParameter.removeValue().await();
            final Warnings warnings = warningsParameter.removeValue().await();
            final VerboseCharacterWriteStream verboseStream = verboseParameter.getVerboseCharacterWriteStream().await();

            result = new QubPackParameters(output, error, folderToPack, environmentVariables, processFactory, defaultApplicationLauncher)
                .setTestJson(testJson)
                .setJvmClassPath(jvmClassPath)
                .setBuildJson(buildJson)
                .setWarnings(warnings)
                .setVerbose(verboseStream);
        }

        return result;
    }

    public int run(QubPackParameters parameters)
    {
        PreCondition.assertNotNull(parameters, "parameters");

        int result = QubTest.run(parameters);
        if (result == 0)
        {
            final JarCreator jarCreator = getJarCreator();

            final ProcessFactory processFactory = parameters.getProcessFactory();
            final Folder folderToPack = parameters.getFolderToPack();
            final CharacterWriteStream output = parameters.getOutputCharacterWriteStream();
            final ByteWriteStream outputByteWriteStream = parameters.getOutputByteWriteStream();
            final ByteWriteStream errorByteWriteStream = parameters.getErrorByteWriteStream();
            final VerboseCharacterWriteStream verbose = parameters.getVerbose();
            final boolean isVerbose = verbose.isVerbose();

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

            output.writeLine("Creating sources jar file...").await();
            jarCreator.setBaseFolder(sourceFolder);
            jarCreator.setJarName(projectJson.getProject() + ".sources");
            jarCreator.setFiles(sourceJavaFiles);
            final File sourcesJarFile = jarCreator.createJarFile(processFactory, outputByteWriteStream, errorByteWriteStream, verbose).await();
            final File sourcesJarFileInOutputsFolder = outputFolder.getFile(sourcesJarFile.getName()).await();
            sourcesJarFile.copyTo(sourcesJarFileInOutputsFolder).await();
            sourcesJarFile.delete().await();
            verbose.writeLine("Created " + sourcesJarFileInOutputsFolder + ".").await();

            output.writeLine("Creating compiled sources jar file...").await();
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
            final File compiledSourcesJarFile = jarCreator.createJarFile(processFactory, outputByteWriteStream, errorByteWriteStream, verbose).await();
            verbose.writeLine("Created " + compiledSourcesJarFile + ".").await();

            final Folder testFolder = folderToPack.getFolder("tests").await();
            if (testFolder.exists().await())
            {
                output.writeLine("Creating compiled tests jar file...").await();
                final Iterable<File> testJavaFiles = testFolder.getFilesRecursively().await()
                    .where((File file) -> Comparer.equal(file.getFileExtension(), ".java"))
                    .toList();
                jarCreator.setBaseFolder(outputFolder);
                jarCreator.setJarName(projectJson.getProject() + ".tests");
                jarCreator.setFiles(QubPack.getSourceClassFiles(outputFolder, outputClassFiles, testFolder, testJavaFiles));
                final File compiledTestsJarFile = jarCreator.createJarFile(processFactory, outputByteWriteStream, errorByteWriteStream, verbose).await();
                verbose.writeLine("Created " + compiledTestsJarFile + ".").await();
            }
        }

        return result;
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
}