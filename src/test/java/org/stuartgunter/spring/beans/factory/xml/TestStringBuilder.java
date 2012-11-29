package org.stuartgunter.spring.beans.factory.xml;

public class TestStringBuilder {

    private String prefix;
    private String suffix;
    private String body;
    private boolean uppercase;

    public TestStringBuilder withUppercase(boolean uppercase) {
        this.uppercase = uppercase;
        return this;
    }

    public TestStringBuilder withPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public TestStringBuilder withSuffix(String suffix) {
        this.suffix = suffix;
        return this;
    }

    public TestStringBuilder withBody(String body) {
        this.body = body;
        return this;
    }

    public String build() {
        String result = prefix + body + suffix;
        return uppercase ? result.toUpperCase() : result;
    }
}
