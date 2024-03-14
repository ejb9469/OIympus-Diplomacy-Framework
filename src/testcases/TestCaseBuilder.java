package testcases;

import exceptions.DiplomacyException;

import java.io.IOException;

public abstract class TestCaseBuilder {

    public abstract void build(String source) throws DiplomacyException;

}