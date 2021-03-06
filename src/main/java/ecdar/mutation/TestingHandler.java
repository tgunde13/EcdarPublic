package ecdar.mutation;

import ecdar.mutation.models.MutationTestCase;
import ecdar.mutation.models.MutationTestPlan;
import ecdar.mutation.models.TestResult;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A test driver that runs model-based mutation test-cases on a system under test.
 * The test driver displays information about resultViews.
 * You can retest test-cases, also while this is still conducting tests.
 */
public class TestingHandler implements ConcurrentJobsHandler {
    private final MutationTestPlan testPlan;
    private Instant testStart;
    private final ConcurrentJobsDriver jobsDriver;


    /**
     * Constructs.
     * @param testPlan the test plan associated with the tests to run
     */
    TestingHandler(final MutationTestPlan testPlan) {
        this.testPlan = testPlan;
        this.jobsDriver = new ConcurrentJobsDriver(this);
    }


    /* Properties */

    @Override
    public int getMaxConcurrentJobs() {
        return getPlan().getConcurrentSutInstances();
    }

    private MutationTestPlan getPlan() {
        return testPlan;
    }

    public ConcurrentJobsDriver getJobsDriver() {
        return jobsDriver;
    }

    /* Other */

    /**
     * Tests some test-cases.
     * @param cases the test-cases
     */
    public void testFromScratch(final List<MutationTestCase> cases) {
        testStart = Instant.now();

        jobsDriver.addJobs(cases.stream().map(testCase -> (Runnable)() -> performTest(testCase)).collect(Collectors.toList()));
    }

    /**
     * Retests a single test-case.
     * @param testCase the test-case
     */
    public void retest(final MutationTestCase testCase) {
        retest(Stream.of(testCase).collect(Collectors.toList()));
    }

    /**
     * Retests some test-cases.
     * @param cases the test-cases
     */
    public void retest(final List<MutationTestCase> cases) {
        synchronized (getPlan()) {
            if (getPlan().shouldStop()) return;

            getPlan().setStatus(MutationTestPlan.Status.WORKING);
        }

        cases.forEach(testCase -> jobsDriver.addJob(() -> performTest(testCase)));

        // Do not measure time when retesting
        testStart = null;
        getPlan().setTestTimeText("");
    }

    /**
     * Performs a test-case on the test plans system under test(sut).
     * @param testCase to perform.
     */
    private void performTest(final MutationTestCase testCase) {
        new TestDriver(testCase, getPlan(), this::onTestDone).start();
    }

    /**
     * Is triggered when a test-case execution is done.
     * It updates UI labels to tell user about the progress.
     * It also updates the jobsDriver about the job progress.
     * @param result the test result
     */
    private synchronized void onTestDone(final TestResult result) {
        // Result is null if an error occurred
        if (result != null) {
            final Instant endTime = Instant.now();

            Platform.runLater(() -> {
                getPlan().addResult(result);

                // clock is null if we retest
                if (testStart != null) {
                    getPlan().setTestTimeText("Testing time: " + MutationTestPlanPresentation.readableFormat(Duration.between(testStart, endTime)));
                }
            });
        }

        jobsDriver.onJobDone();
    }

    @Override
    public boolean shouldStop() {
        return getPlan().shouldStop();
    }

    @Override
    public void onStopped() { Platform.runLater(() -> getPlan().setStatus(MutationTestPlan.Status.IDLE)); }

    @Override
    public void onAllJobsSuccessfullyDone() {
        final Text text = new Text("Done");
        text.setFill(Color.GREEN);
        writeProgress(text);
        getPlan().setStatus(MutationTestPlan.Status.IDLE);
    }

    @Override
    public void onProgressRemaining(final int remaining) {
        writeProgress("Testing... (" + remaining + " test-case" + (remaining == 1 ? "" : "s") + " remaining)");
    }

    /**
     * Writes progress.
     * @param message the message describing the progress
     */
    private void writeProgress(final String message) {
        final Text text = new Text(message);
        text.setFill(Color.web("#333333"));
        writeProgress(text);
    }

    /**
     * Writes progress in a java fx thread.
     * @param text the text describing the progress
     */
    private void writeProgress(final Text text) {
        getPlan().writeProgress(text);
    }
}
