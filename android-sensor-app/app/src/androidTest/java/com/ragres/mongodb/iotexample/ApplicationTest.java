package com.ragres.mongodb.iotexample;

import android.test.ApplicationTestCase;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<AndroidApplication> {

    private AndroidApplication androidApplication;

    public ApplicationTest() {
        super(AndroidApplication.class);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        createApplication();
        androidApplication = getApplication();
    }

    public void testAppOkay() {
        assertNotNull(androidApplication);
    }
}