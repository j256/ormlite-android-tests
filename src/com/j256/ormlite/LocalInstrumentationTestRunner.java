package com.j256.ormlite;

import junit.framework.TestSuite;
import android.test.suitebuilder.TestSuiteBuilder;

public class LocalInstrumentationTestRunner extends android.test.InstrumentationTestRunner {

	@Override
	public TestSuite getTestSuite() {
		return new TestSuiteBuilder(getClass()).includeAllPackagesUnderHere().build();
	}

	@Override
	public TestSuite getAllTests() {
		return getTestSuite();
	}
}
