package qub;

public class PackJSON
{
    private static final String projectPropertyName = "project";
    private static final String sourceFilesPropertyName = "sourceFiles";
    private static final String sourceOutputFilesPropertyName = "sourceOutputFiles";
    private static final String testOutputFilesPropertyName = "testOutputFiles";

    private final JSONObject json;

    private PackJSON(JSONObject json)
    {
        PreCondition.assertNotNull(json, "json");

        this.json = json;
    }

    public static PackJSON create()
    {
        return PackJSON.create(JSONObject.create());
    }

    public static PackJSON create(JSONObject json)
    {
        return new PackJSON(json);
    }

    public PackJSON setProject(String project)
    {
        PreCondition.assertNotNullAndNotEmpty(project, "project");

        this.json.setString(PackJSON.projectPropertyName, project);

        return this;
    }

    public String getProject()
    {
        return this.json.getString(PackJSON.projectPropertyName)
            .catchError()
            .await();
    }

    public PackJSON setSourceFiles(Iterable<PackJSONFile> sourceFiles)
    {
        PreCondition.assertNotNull(sourceFiles, "sourceFiles");

        PackJSON.setPackJsonFiles(this.json, PackJSON.sourceFilesPropertyName, sourceFiles);

        return this;
    }

    public Iterable<PackJSONFile> getSourceFiles()
    {
        return PackJSON.parsePackJSONFiles(this.json, PackJSON.sourceFilesPropertyName);
    }

    public PackJSON setSourceOutputFiles(Iterable<PackJSONFile> sourceOutputFiles)
    {
        PreCondition.assertNotNull(sourceOutputFiles, "sourceOutputFiles");

        PackJSON.setPackJsonFiles(this.json, PackJSON.sourceOutputFilesPropertyName, sourceOutputFiles);

        return this;
    }

    public Iterable<PackJSONFile> getSourceOutputFiles()
    {
        return PackJSON.parsePackJSONFiles(this.json, PackJSON.sourceOutputFilesPropertyName);
    }

    public PackJSON setTestOutputFiles(Iterable<PackJSONFile> testOutputFiles)
    {
        PreCondition.assertNotNull(testOutputFiles, "testOutputFiles");

        PackJSON.setPackJsonFiles(this.json, PackJSON.testOutputFilesPropertyName, testOutputFiles);

        return this;
    }

    public Iterable<PackJSONFile> getTestOutputFiles()
    {
        return PackJSON.parsePackJSONFiles(this.json, PackJSON.testOutputFilesPropertyName);
    }

    @Override
    public boolean equals(Object rhs)
    {
        return rhs instanceof PackJSON && this.equals((PackJSON)rhs);
    }

    public boolean equals(PackJSON rhs)
    {
        return rhs != null &&
            Comparer.equal(this.getSourceFiles(), rhs.getSourceFiles()) &&
            Comparer.equal(this.getSourceOutputFiles(), rhs.getSourceOutputFiles()) &&
            Comparer.equal(this.getTestOutputFiles(), rhs.getTestOutputFiles());
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
        return this.json;
    }

    private static void setPackJsonFiles(JSONObject json, String propertyName, Iterable<PackJSONFile> packJSONFiles)
    {
        PreCondition.assertNotNull(json, "json");
        PreCondition.assertNotNullAndNotEmpty(propertyName, "propertyName");
        PreCondition.assertNotNull(packJSONFiles, "packJSONFiles");

        json.setObject(propertyName, JSONObject.create(packJSONFiles.map(PackJSONFile::toJsonProperty)));
    }

    private static Iterable<PackJSONFile> parsePackJSONFiles(JSONObject json, String propertyName)
    {
        PreCondition.assertNotNull(json, "json");
        PreCondition.assertNotNullAndNotEmpty(propertyName, "propertyName");

        final List<PackJSONFile> result = List.create();
        final JSONObject sourceFiles = json.getObject(propertyName).catchError().await();
        if (sourceFiles != null)
        {
            for (final JSONProperty jsonProperty : sourceFiles.getProperties())
            {
                PackJSONFile.parse(jsonProperty)
                    .then(result::add)
                    .catchError()
                    .await();
            }
        }
        return result;
    }
}
