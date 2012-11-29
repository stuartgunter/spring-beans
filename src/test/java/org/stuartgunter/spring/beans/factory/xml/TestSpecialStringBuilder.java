package org.stuartgunter.spring.beans.factory.xml;

public class TestSpecialStringBuilder {

    private String prefix;
    private String suffix;
    private String body;

    public TestSpecialStringBuilder usingPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public TestSpecialStringBuilder usingSuffix(String suffix) {
        this.suffix = suffix;
        return this;
    }

    public TestSpecialStringBuilder usingBody(String body) {
        this.body = body;
        return this;
    }

    public String construct() {
        return prefix + body + suffix;
    }
}
