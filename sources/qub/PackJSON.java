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
        return this.toString(JSONFormat.consise);
    }

    public String toString(JSONFormat format)
    {
        PreCondition.assertNotNull(format, "format");

        return this.toJson().toString(format);
    }

    public JSONObject toJson()
    {
        final JSONObject result = JSONObject.create();

        PackJSON.setFilesProperty(result, PackJSON.sourceFilesPropertyName, this.sourceFiles);
        PackJSON.setFilesProperty(result, PackJSON.sourceOutputFilesPropertyName, this.sourceOutputFiles);
        PackJSON.setFilesProperty(result, PackJSON.testOutputFilesPropertyName, this.testOutputFiles);

        PostCondition.assertNotNull(result, "result");

        return result;
    }

    private static void setFilesProperty(JSONObject jsonObject, String propertyName, Iterable<PackJSONFile> files)
    {
        PreCondition.assertNotNull(jsonObject, "jsonObject");
        PreCondition.assertNotNullAndNotEmpty(propertyName, "propertyName");

        if (!Iterable.isNullOrEmpty(files))
        {
            jsonObject.setObject(propertyName, JSONObject.create(files.map(PackJSONFile::toJsonProperty)));
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

        json.getObject(propertyName)
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
