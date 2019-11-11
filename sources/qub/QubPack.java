package qub;

public interface QubPack
{
    static void main(String[] args)
    {
        Console.run(args, QubPack::main);
    }

    static void main(Console console)
    {
        PreCondition.assertNotNull(console, "console");

        final QubPackParameters parameters = QubPack.getParameters(console);
        if (parameters != null)
        {
            final Stopwatch stopwatch = console.getStopwatch();
            stopwatch.start();
            try
            {
                console.setExitCode(QubPack.run(parameters));
            }
            finally
            {
                final Duration compilationDuration = stopwatch.stop().toSeconds();
                console.writeLine("Done (" + compilationDuration.toString("0.0") + ")").await();
            }
        }
    }

    static CommandLineParameter<Folder> addFolderToPack(CommandLineParameters parameters, Process process)
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
    static QubPackParameters getParameters(Process process)
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

    static int run(QubPackParameters parameters)
    {
        PreCondition.assertNotNull(parameters, "parameters");

        int result = QubTest.run(parameters);
        if (result == 0)
        {
            final ProcessFactory processFactory = parameters.getProcessFactory();
            final Folder folderToPack = parameters.getFolderToPack();
            final CharacterWriteStream output = parameters.getOutputCharacterWriteStream();
            final ByteWriteStream outputByteWriteStream = parameters.getOutputByteWriteStream();
            final ByteWriteStream errorByteWriteStream = parameters.getErrorByteWriteStream();
            final VerboseCharacterWriteStream verbose = parameters.getVerbose();

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
            final String project = projectJson.getProject();

            output.writeLine("Creating sources jar file...").await();
            final File sourcesJarFile = sourceFolder.getFile(project + ".sources.jar").await();
            final int createSourcesJarFileResult = QubPack.createJarFile(processFactory, sourceFolder, sourcesJarFile, sourceJavaFiles, verbose, outputByteWriteStream, errorByteWriteStream);
            if (createSourcesJarFileResult == 0)
            {
                final File sourcesJarFileInOutputsFolder = outputFolder.getFile(sourcesJarFile.getName()).await();
                sourcesJarFile.copyTo(sourcesJarFileInOutputsFolder).await();
                sourcesJarFile.delete().await();
                verbose.writeLine("Created " + sourcesJarFileInOutputsFolder + ".").await();
            }
            else
            {
                ++result;
            }

            output.writeLine("Creating compiled sources jar file...").await();
            File manifestFile = null;
            final String mainClass = projectJson.getJava().getMainClass();
            if (!Strings.isNullOrEmpty(mainClass))
            {
                manifestFile = outputFolder.getFile("META-INF/MANIFEST.MF").await();
                final String manifestFileContents =
                    "Manifest-Version: 1.0\n" +
                    "Main-Class: " + mainClass + "\n";
                manifestFile.setContentsAsString(manifestFileContents).await();
            }
            final File compiledSourcesJarFile = outputFolder.getFile(project + ".jar").await();
            final Iterable<File> compiledSourcesFile = QubPack.getSourceClassFiles(outputFolder, outputClassFiles, sourceFolder, sourceJavaFiles);
            final int createCompiledSourcesJarFileResult = QubPack.createJarFile(processFactory, outputFolder, manifestFile, compiledSourcesJarFile, compiledSourcesFile, verbose, outputByteWriteStream, errorByteWriteStream);
            if (createCompiledSourcesJarFileResult == 0)
            {
                verbose.writeLine("Created " + compiledSourcesJarFile + ".").await();
            }
            else
            {
                ++result;
            }

            final Folder testFolder = folderToPack.getFolder("tests").await();
            if (testFolder.exists().await())
            {
                output.writeLine("Creating compiled tests jar file...").await();
                final File compiledTestsJarFile = outputFolder.getFile(projectJson.getProject() + ".tests.jar").await();
                final Iterable<File> testJavaFiles = testFolder.getFilesRecursively().await()
                    .where((File file) -> Comparer.equal(file.getFileExtension(), ".java"))
                    .toList();
                final Iterable<File> testSourceClassFiles = QubPack.getSourceClassFiles(outputFolder, outputClassFiles, testFolder, testJavaFiles);
                final int createTestSourcesJarFileResult = QubPack.createJarFile(processFactory, outputFolder, compiledTestsJarFile, testSourceClassFiles, verbose, outputByteWriteStream, errorByteWriteStream);
                if (createTestSourcesJarFileResult == 0)
                {
                    verbose.writeLine("Created " + compiledTestsJarFile + ".").await();
                }
                else
                {
                    ++result;
                }
            }
        }

        return result;
    }

    static int createJarFile(ProcessFactory processFactory, Folder baseFolder, File jarFile, Iterable<File> files, VerboseCharacterWriteStream verbose, ByteWriteStream outputByteWriteStream, ByteWriteStream errorByteWriteStream)
    {
        return QubPack.createJarFile(processFactory, baseFolder, null, jarFile, files, verbose, outputByteWriteStream, errorByteWriteStream);
    }

    static int createJarFile(ProcessFactory processFactory, Folder baseFolder, File manifestFile, File jarFile, Iterable<File> files, VerboseCharacterWriteStream verbose, ByteWriteStream outputByteWriteStream, ByteWriteStream errorByteWriteStream)
    {
        PreCondition.assertNotNull(processFactory, "processFactory");
        PreCondition.assertNotNull(baseFolder, "baseFolder");
        PreCondition.assertNotNull(jarFile, "jarFile");
        PreCondition.assertNotNullAndNotEmpty(files, "files");
        PreCondition.assertNotNull(verbose, "verbose");
        PreCondition.assertNotNull(outputByteWriteStream, "outputByteWriteStream");
        PreCondition.assertNotNull(errorByteWriteStream, "errorByteWriteStream");

        final JarProcessBuilder jar = JarProcessBuilder.get(processFactory).await()
            .setWorkingFolder(baseFolder)
            .addCreate()
            .addJarFile(jarFile);
        if (manifestFile != null)
        {
            jar.addManifestFile(manifestFile);
        }

        jar.addContentFiles(files);

        if (verbose.isVerbose())
        {
            jar.redirectOutput(outputByteWriteStream);
            jar.redirectError(errorByteWriteStream);
            verbose.writeLine("Running " + jar.getCommand()).await();
        }

        return jar.run().await();
    }

    static boolean isSourceClassFile(Folder outputFolder, File outputClassFile, Folder sourceFolder, Iterable<File> sourceJavaFiles)
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

    static Iterable<File> getSourceClassFiles(Folder outputFolder, Iterable<File> outputClassFiles, Folder sourceFolder, Iterable<File> sourceJavaFiles)
    {
        return outputClassFiles
            .where((File outputClassFile) ->
            {
                return QubPack.isSourceClassFile(outputFolder, outputClassFile, sourceFolder, sourceJavaFiles);
            });
    }
}