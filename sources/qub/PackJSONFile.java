package qub;

public class PackJSONFile
{
    private Path relativePath;
    private DateTime lastModified;

    public PackJSONFile setRelativePath(String relativePath)
    {
        PreCondition.assertNotNullAndNotEmpty(relativePath, "relativePath");

        return this.setRelativePath(Path.parse(relativePath));
    }

    public PackJSONFile setRelativePath(Path relativePath)
    {
        PreCondition.assertNotNull(relativePath, "relativePath");
        PreCondition.assertFalse(relativePath.isRooted(), "relativePath.isRooted()");

        this.relativePath = relativePath;

        return this;
    }

    public Path getRelativePath()
    {
        return this.relativePath;
    }

    public PackJSONFile setLastModified(DateTime lastModified)
    {
        PreCondition.assertNotNull(lastModified, "lastModified");

        this.lastModified = lastModified;

        return this;
    }

    public DateTime getLastModified()
    {
        return this.lastModified;
    }

    @Override
    public boolean equals(Object rhs)
    {
        return rhs instanceof PackJSONFile && this.equals((PackJSONFile)rhs);
    }

    public boolean equals(PackJSONFile rhs)
    {
        return rhs != null &&
            Comparer.equal(this.relativePath, rhs.relativePath) &&
            Comparer.equal(this.lastModified, rhs.lastModified);
    }

    @Override
    public String toString()
    {
        return Strings.escapeAndQuote(this.relativePath) + ":" + Strings.escapeAndQuote(this.lastModified);
    }

    public void toJsonProperty(JSONObjectBuilder packJsonFiles)
    {
        PreCondition.assertNotNull(packJsonFiles, "packJsonFiles");
        PreCondition.assertNotNull(this.getRelativePath(), "this.getRelativePath()");

        packJsonFiles.stringOrNullProperty(this.relativePath.toString(), this.lastModified == null ? null : this.lastModified.toString());
    }

    public static Result<PackJSONFile> parse(JSONProperty property)
    {
        PreCondition.assertNotNull(property, "property");

        return Result.create(() ->
        {
            final String relativePath = property.getName();
            final DateTime lastModified = DateTime.parse(((JSONQuotedString)property.getValueSegment()).toUnquotedString()).await();
            return new PackJSONFile()
                .setRelativePath(relativePath)
                .setLastModified(lastModified);
        });
    }
}
