package qub;

public class PackJSONFile
{
    private final JSONProperty json;

    private PackJSONFile(JSONProperty json)
    {
        PreCondition.assertNotNull(json, "json");

        this.json = json;
    }

    public static PackJSONFile create(String relativePath, DateTime lastModified)
    {
        PreCondition.assertNotNullAndNotEmpty(relativePath, "relativePath");
        PreCondition.assertNotNull(lastModified, "lastModified");

        return PackJSONFile.create(Path.parse(relativePath), lastModified);
    }

    public static PackJSONFile create(Path relativePath, DateTime lastModified)
    {
        PreCondition.assertNotNull(relativePath, "relativePath");
        PreCondition.assertFalse(relativePath.isRooted(), "relativePath.isRooted()");
        PreCondition.assertNotNull(lastModified, "lastModified");

        return new PackJSONFile(JSONProperty.create(relativePath.toString(), lastModified.toString()));
    }

    public static Result<PackJSONFile> parse(JSONProperty json)
    {
        PreCondition.assertNotNull(json, "json");

        return Result.create(() ->
        {
            // If the DateTime successfully parses the property value, then it's a valid
            // PackJSONFile JSONProperty.
            DateTime.parse(json.getStringValue().await()).await();

            return new PackJSONFile(json);
        });
    }

    public Path getRelativePath()
    {
        return Path.parse(this.json.getName());
    }

    public DateTime getLastModified()
    {
        return DateTime.parse(this.json.getStringValue().await()).await();
    }

    @Override
    public boolean equals(Object rhs)
    {
        return rhs instanceof PackJSONFile && this.equals((PackJSONFile)rhs);
    }

    public boolean equals(PackJSONFile rhs)
    {
        return rhs != null &&
            Comparer.equal(this.getRelativePath(), rhs.getRelativePath()) &&
            Comparer.equal(this.getLastModified(), rhs.getLastModified());
    }

    @Override
    public String toString()
    {
        return this.toString(JSONFormat.consise);
    }

    public String toString(JSONFormat format)
    {
        PreCondition.assertNotNull(format, "format");

        return this.json.toString(format);
    }

    public JSONProperty toJsonProperty()
    {
        PreCondition.assertNotNull(this.getRelativePath(), "this.getRelativePath()");

        return this.json;
    }
}
