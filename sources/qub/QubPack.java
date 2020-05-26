package qub;

public interface QubPack
{
    static void main(String[] args)
    {
        QubProcess.run(args, (Action1<QubProcess>)QubPack::main);
    }

    static void main(QubProcess process)
    {
        PreCondition.assertNotNull(process, "process");

        final QubPackParameters parameters = QubPack.getParameters(process);
        if (parameters != null)
        {
            process.showDuration(() ->
            {
                process.setExitCode(QubPack.run(parameters));
            });
        }
    }

    static CommandLineParameter<Folder> addFolderToPack(CommandLineParameters parameters, QubProcess process)
    {
        PreCondition.assertNotNull(parameters, "parameters");
        PreCondition.assertNotNull(process, "process");

        return parameters.addPositionalFolder("folder", process)
            .setValueName("<folder-to-pack>")
            .setDescription("The folder to pack. Defaults to the current folder.");
    }

    static CommandLineParameterBoolean addPackJsonParameter(CommandLineParameters parameters)
    {
        PreCondition.assertNotNull(parameters, "parameters");

        final boolean packJsonDefault = QubPackParameters.getPackJsonDefault();
        return parameters.addBoolean("packjson", packJsonDefault)
            .setDescription("Whether or not to read and write a pack.json file. Defaults to " + packJsonDefault + ".");
    }

    /**
     * Get the QubPackParameters from the provided Process.
     * @param process The process to get the QubPackParameters from.
     * @return The QubPackParameters.
     */
    static QubPackParameters getParameters(QubProcess process)
    {
        PreCondition.assertNotNull(process, "process");

        final CommandLineParameters parameters = process.createCommandLineParameters()
            .setApplicationName("qub-pack")
            .setApplicationDescription("Used to package source and compiled code in source code projects.");
        final CommandLineParameter<Folder> folderToPackParameter = QubPack.addFolderToPack(parameters, process);
        final CommandLineParameterBoolean packJsonParameter = QubPack.addPackJsonParameter(parameters);
        final CommandLineParameterBoolean testJsonParameter = QubTest.addTestJsonParameter(parameters);
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

            final CharacterToByteReadStream input = process.getInputReadStream();
            final CharacterToByteWriteStream output = process.getOutputWriteStream();
            final CharacterToByteWriteStream error = process.getErrorWriteStream();
            final DefaultApplicationLauncher defaultApplicationLauncher = process.getDefaultApplicationLauncher();
            final Folder folderToPack = folderToPackParameter.getValue().await();
            final boolean packJson = packJsonParameter.getValue().await();
            final EnvironmentVariables environmentVariables = process.getEnvironmentVariables();
            final ProcessFactory processFactory = process.getProcessFactory();
            final boolean testJson = testJsonParameter.removeValue().await();
            final String jvmClassPath = process.getJVMClasspath().await();
            final boolean buildJson = buildJsonParameter.removeValue().await();
            final Warnings warnings = warningsParameter.removeValue().await();
            final VerboseCharacterWriteStream verboseStream = verboseParameter.getVerboseCharacterWriteStream().await();
            final boolean profiler = profilerParameter.getValue().await();

            result = new QubPackParameters(input, output, error, folderToPack, environmentVariables, processFactory, defaultApplicationLauncher, jvmClassPath)
                .setPackJson(packJson)
                .setTestJson(testJson)
                .setBuildJson(buildJson)
                .setWarnings(warnings)
                .setVerbose(verboseStream)
                .setProfiler(profiler);
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
            final boolean usePackJson = parameters.getPackJson();
            final CharacterToByteWriteStream output = parameters.getOutputWriteStream();
            final CharacterToByteWriteStream error = parameters.getErrorWriteStream();
            final VerboseCharacterWriteStream verbose = parameters.getVerbose();

            final Folder outputFolder = folderToPack.getFolder("outputs").await();
            final File packJsonFile = outputFolder.getFile("pack.json").await();
            PackJSON packJson = null;
            if (usePackJson)
            {
                final String packJsonFileContents = packJsonFile.getContentsAsString()
                    .catchError(FileNotFoundException.class)
                    .await();
                if (!Strings.isNullOrEmpty(packJsonFileContents))
                {
                    packJson = PackJSON.parse(JSON.parseObject(packJsonFileContents).await()).await();
                }
                if (packJson == null)
                {
                    packJson = new PackJSON();
                }
            }

            final Folder sourceFolder = folderToPack.getFolder("sources").await();
            final Iterable<File> sourceJavaFiles = sourceFolder.getFilesRecursively().await()
                .where((File file) -> Comparer.equal(file.getFileExtension(), ".java"))
                .toList();

            final File projectJsonFile = folderToPack.getFile("project.json").await();
            final ProjectJSON projectJson = ProjectJSON.parse(projectJsonFile).await();
            final String project = projectJson.getProject();

            final boolean shouldCreateSourcesJarFile = QubPack.shouldCreateJarFile(packJson, PackJSON::getSourceFiles, PackJSON::setSourceFiles, sourceFolder, sourceJavaFiles);

            if (!shouldCreateSourcesJarFile)
            {
                output.writeLine("Skipping sources jar file.").await();
            }
            else
            {
                output.writeLine("Creating sources jar file...").await();
                final File sourcesJarFile = sourceFolder.getFile(project + ".sources.jar").await();
                final int createSourcesJarFileResult = QubPack.createJarFile(processFactory, sourceFolder, sourcesJarFile, sourceJavaFiles, verbose, output, error);
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
            }

            final Iterable<File> outputClassFiles = outputFolder.getFilesRecursively().await()
                .where((File file) -> Comparer.equal(file.getFileExtension(), ".class"))
                .toList();

            final Iterable<File> compiledSourcesFile = QubPack.getSourceClassFiles(outputFolder, outputClassFiles, sourceFolder, sourceJavaFiles);
            final boolean shouldCreateCompiledSourcesJarFile = QubPack.shouldCreateJarFile(packJson, PackJSON::getSourceOutputFiles, PackJSON::setSourceOutputFiles, outputFolder, compiledSourcesFile);
            if (!shouldCreateCompiledSourcesJarFile)
            {
                output.writeLine("Skipping compiled sources jar file.").await();
            }
            else
            {
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
                final int createCompiledSourcesJarFileResult = QubPack.createJarFile(processFactory, outputFolder, manifestFile, compiledSourcesJarFile, compiledSourcesFile, verbose, output, error);
                if (createCompiledSourcesJarFileResult == 0)
                {
                    verbose.writeLine("Created " + compiledSourcesJarFile + ".").await();
                }
                else
                {
                    ++result;
                }
            }

            boolean shouldCreateCompiledTestsJarFile = false;
            final Folder testFolder = folderToPack.getFolder("tests").await();
            if (testFolder.exists().await())
            {
                final File compiledTestsJarFile = outputFolder.getFile(projectJson.getProject() + ".tests.jar").await();
                final Iterable<File> testJavaFiles = testFolder.getFilesRecursively().await()
                    .where((File file) -> Comparer.equal(file.getFileExtension(), ".java"))
                    .toList();
                final Iterable<File> testSourceClassFiles = QubPack.getSourceClassFiles(outputFolder, outputClassFiles, testFolder, testJavaFiles);

                shouldCreateCompiledTestsJarFile = QubPack.shouldCreateJarFile(packJson, PackJSON::getTestOutputFiles, PackJSON::setTestOutputFiles, outputFolder, testSourceClassFiles);
                if (!shouldCreateCompiledTestsJarFile)
                {
                    output.writeLine("Skipping compiled tests jar file.").await();
                }
                else
                {
                    output.writeLine("Creating compiled tests jar file...").await();
                    final int createTestSourcesJarFileResult = QubPack.createJarFile(processFactory, outputFolder, compiledTestsJarFile, testSourceClassFiles, verbose, output, error);
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

            if (usePackJson && (shouldCreateSourcesJarFile || shouldCreateCompiledSourcesJarFile || shouldCreateCompiledTestsJarFile))
            {
                packJsonFile.setContentsAsString(packJson.toString(JSONFormat.pretty)).await();
            }
        }

        return result;
    }

    static boolean shouldCreateJarFile(PackJSON packJson, Function1<PackJSON,Iterable<PackJSONFile>> getPackJSONFiles, Action2<PackJSON,Iterable<PackJSONFile>> setPackJSONFiles, Folder folder, Iterable<File> files)
    {
        boolean result;
        if (packJson == null)
        {
            result = true;
        }
        else
        {
            result = false;
            Iterable<PackJSONFile> packJsonFiles = getPackJSONFiles.run(packJson);
            if (packJsonFiles == null)
            {
                packJsonFiles = Iterable.create();
            }
            final List<PackJSONFile> newPackJsonSourceFiles = List.create();
            final List<PackJSONFile> deletedSourceFiles = List.create(packJsonFiles);
            for (final File file : files)
            {
                final Path fileRelativePath = file.relativeTo(folder);
                final PackJSONFile packJsonFile = packJsonFiles.first(value -> Comparer.equal(value.getRelativePath(), fileRelativePath));
                final DateTime sourceJavaFileLastModified = file.getLastModified().await();
                if (packJsonFile == null || !Comparer.equal(packJsonFile.getLastModified(), sourceJavaFileLastModified))
                {
                    newPackJsonSourceFiles.add(new PackJSONFile()
                        .setRelativePath(fileRelativePath)
                        .setLastModified(sourceJavaFileLastModified));
                    result = true;
                }
                else
                {
                    newPackJsonSourceFiles.add(packJsonFile);
                    deletedSourceFiles.removeFirst(value -> Comparer.equal(value.getRelativePath(), fileRelativePath));
                }
            }

            setPackJSONFiles.run(packJson, newPackJsonSourceFiles);

            if (deletedSourceFiles.any())
            {
                result = true;
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