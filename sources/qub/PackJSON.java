package qub;

public class PackJSON
{
    private static final String sourceFilesPropertyName = "sourceFiles";
    private static final String sourceOutputFilesPropertyName = "sourceOutputFiles";
    private static final String testOutputFilesPropertyName = "testOutputFiles";

    private Iterable<PackJSONFile> sourceFiles;
    private Iterable<PackJSONFile> sourceOutputFiles;
    private Iterable<PackJSONFile> testOutputFiles;

    public PackJSON setSourceFiles(Iterable<PackJSONFile> sourceFiles)
    {
        PreCondition.assertNotNull(sourceFiles, "sourceFiles");

        this.sourceFiles = sourceFiles;

        return this;
    }

    public Iterable<PackJSONFile> getSourceFiles()
    {
        return this.sourceFiles;
    }

    public PackJSON setSourceOutputFiles(Iterable<PackJSONFile> sourceOutputFiles)
    {
        PreCondition.assertNotNull(sourceOutputFiles, "sourceOutputFiles");

        this.sourceOutputFiles = sourceOutputFiles;

        return this;
    }

    public Iterable<PackJSONFile> getSourceOutputFiles()
    {
        return this.sourceOutputFiles;
    }

    public PackJSON setTestOutputFiles(Iterable<PackJSONFile> testOutputFiles)
    {
        PreCondition.assertNotNull(testOutputFiles, "testOutputFiles");

        this.testOutputFiles = testOutputFiles;

        return this;
    }

    public Iterable<PackJSONFile> getTestOutputFiles()
    {
        return this.testOutputFiles;
    }

    @Override
    public boolean equals(Object rhs)
    {
        return rhs instanceof PackJSON && this.equals((PackJSON)rhs);
    }

    public boolean equals(PackJSON rhs)
    {
        return rhs != null &&
            Comparer.equal(this.sourceFiles, rhs.sourceFiles) &&
            Comparer.equal(this.sourceOutputFiles, rhs.sourceOutputFiles) &&
            Comparer.equal(this.testOutputFiles, rhs.testOutputFiles);
    }

    @Override
    public String toString()
    {
        return this.toJson().toString();
    }

    public JSONObject toJson()
    {
        return JSON.object(this::toJson);
    }

    public void toJson(JSONObjectBuilder json)
    {
        PreCondition.assertNotNull(json, "json");

        PackJSON.writeFiles(this.sourceFiles, PackJSON.sourceFilesPropertyName, json);
        PackJSON.writeFiles(this.sourceOutputFiles, PackJSON.sourceOutputFilesPropertyName, json);
        PackJSON.writeFiles(this.testOutputFiles, PackJSON.testOutputFilesPropertyName, json);
    }

    private static void writeFiles(Iterable<PackJSONFile> files, String propertyName, JSONObjectBuilder json)
    {
        PreCondition.assertNotNullAndNotEmpty(propertyName, "propertyName");
        PreCondition.assertNotNull(json, "json");

        if (!Iterable.isNullOrEmpty(files))
        {
            json.objectProperty(propertyName, filesJson ->
            {
                for (final PackJSONFile file : files)
                {
                    file.toJsonProperty(filesJson);
                }
            });
        }
    }

    public static Result<PackJSON> parse(JSONObject json)
    {
        PreCondition.assertNotNull(json, "json");

        return Result.create(() ->
        {
            final PackJSON result = new PackJSON();
            PackJSON.parseFiles(PackJSON.sourceFilesPropertyName, json, result::setSourceFiles);
            PackJSON.parseFiles(PackJSON.sourceOutputFilesPropertyName, json, result::setSourceOutputFiles);
            PackJSON.parseFiles(PackJSON.testOutputFilesPropertyName, json, result::setTestOutputFiles);
            return result;
        });
    }

    private static void parseFiles(String propertyName, JSONObject json, Action1<Iterable<PackJSONFile>> action)
    {
        PreCondition.assertNotNullAndNotEmpty(propertyName, "propertyName");
        PreCondition.assertNotNull(json, "json");
        PreCondition.assertNotNull(action, "action");

        json.getObjectPropertyValue(propertyName)
            .then((JSONObject filesJson) ->
            {
                final List<PackJSONFile> files = List.create();
                for (final JSONProperty fileProperty : filesJson.getProperties())
                {
                    PackJSONFile.parse(fileProperty)
                        .then(files::add)
                        .catchError()
                        .await();
                }
                action.run(files);
            })
            .catchError()
            .await();
    }
}
