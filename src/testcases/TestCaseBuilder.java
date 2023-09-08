package testcases;

import exceptions.BadOrderException;

public abstract class TestCaseBuilder {

    public abstract void build(String source) throws BadOrderException;

}