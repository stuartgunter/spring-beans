package org.springframework.beans;

/**
 * Defines the style of fluency that the bean exposes. This is typically either via consistently named properties or
 * by fluent methods.
 */
public enum FluentStyle {

    /**
     * Distinct methods are used by the bean (e.g. {@code new MyBean().select(value).from(value).where(value)})
     */
    METHODS,

    /**
     * Consistently named properties are used by the bean (e.g. {@code new MyBean().withX().withY().withZ()}
     */
    PROPERTIES
}
