package ecdar.mutation.models;

import ecdar.mutation.SimpleComponentSimulation;

import java.util.Arrays;

/**
 * A result of a model-based mutation test with respect to a single test-case.
 */
public class TestResult extends ExpandableContent {
    public enum Verdict {PASS, FAIL_NORMAL, FAIL_PRIMARY, OUT_OF_BOUNDS, MAX_WAIT, NO_RULE, NON_DETERMINISM, MUT_NO_DELAY}

    private final Verdict verdict;
    private final MutationTestCase testCase;

    /**
     * Constructs.
     * @param testCase test-case used for getting the result
     * @param reason reason for the verdict
     * @param testModelSimulation test model simulation
     * @param verdict verdict of the test
     */
    public TestResult(final MutationTestCase testCase, final String reason,
                      final SimpleComponentSimulation testModelSimulation,
                      final Verdict verdict) {
        super(testCase.getDescription(), "Id: " + testCase.getId() + "\n" +
                "Reason: " + reason + "\n" +
                "Trace: " + String.join(" → ", testModelSimulation.getTrace()));

        this.verdict = verdict;
        this.testCase = testCase;
    }

    public Verdict getVerdict() {
        return verdict;
    }

    public MutationTestCase getTestCase() {
        return testCase;
    }

    public static Verdict[] getIncVerdicts() {
        return new Verdict[]{Verdict.OUT_OF_BOUNDS, Verdict.MAX_WAIT, Verdict.NON_DETERMINISM, Verdict.NO_RULE, Verdict.MUT_NO_DELAY};
    }

    public static Verdict[] getFailedVerdicts() {
        return new Verdict[]{Verdict.FAIL_NORMAL, Verdict.FAIL_PRIMARY};
    }

    public boolean isPass() {
        return verdict == Verdict.PASS;
    }

    public boolean isFail() {
        return Arrays.asList(getFailedVerdicts()).contains(verdict);
    }
}
