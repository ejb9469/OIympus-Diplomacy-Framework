package testcases;

import exceptions.BadOrderException;

public class BackstabbrNetTestCaseBuilder extends TestCaseBuilder {

    public static final String[] VALID_HOSTS = new String[]{"https://www.backstabbr.com/game/", "http://www.backstabbr.com/game/"};

    @Override
    public void build(String source) throws BadOrderException {

        boolean valid = false;
        for (String host : VALID_HOSTS) {
            if (source.startsWith(host)) {
                valid = true;
                break;
            }
        }
        if (!valid)
            throw new IllegalArgumentException("Invalid URL specified.");

    }

}