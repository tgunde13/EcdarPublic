package ecdar.mutation.operators;

import ecdar.abstractions.Component;
import ecdar.mutation.MutationTestingException;
import ecdar.mutation.models.MutationTestCase;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An operator for creating mutants of a component.
 */
public abstract class MutationOperator {
    private final BooleanProperty selected = new SimpleBooleanProperty(true);


    /* Properties */

    public boolean isSelected() {
        return selected.get();
    }

    public BooleanProperty getSelectedProperty() {
        return selected;
    }

    public void setSelected(final boolean selected) {
        this.selected.set(selected);
    }


    /* Abstract methods */

    /**
     * Gets a readable text to display.
     * @return the text
     */
    public abstract String getText();

    /**
     * Gets a string for storing and retrieving with JSON.
     * @return the string
     */
    public abstract String getCodeName();

    /**
     * Generates mutants.
     * @param original the component to mutate
     * @return list of potential test-cases
     * @throws MutationTestingException if a mutation error occurs
     */
    public abstract List<MutationTestCase> generateTestCases(final Component original) throws MutationTestingException;

    /**
     * Gets a description of the operator to use as a tooltip.
     * @return the description
     */
    public abstract String getDescription();

    /**
     * Gets an upper limit for how many mutants the operator would create.
     * See our paper on mutation testing for the math behind this.
     * @param original the component to mutate
     * @return the upper limit
     */
    public abstract int getUpperLimit(final Component original);

    /**
     * Gets if the upper limit is also the exact number of mutants
     * @return true iff the upper limit is exact
     */
    public abstract boolean isUpperLimitExact();


    /* Other methods */

    /**
     * Generate mutants.
     * @param original the component to mutate
     * @return list of mutants
     * @throws MutationTestingException if a mutation error occurs
     */
    public List<Component> generateMutants(final Component original) throws MutationTestingException {
        return generateTestCases(original).stream().map(MutationTestCase::getMutant).collect(Collectors.toList());
    }

    /**
     * Gets all available mutation operators.
     * @return the operators
     */
    public static List<MutationOperator> getAllOperators() {
        final List<MutationOperator> operators = new ArrayList<>();

        operators.add(new ChangeSourceOperator());
        operators.add(new ChangeTargetOperator());
        operators.add(new ChangeActionInputsOperator());
        operators.add(new ChangeActionOutputsOperator());
        operators.add(new ChangeGuardConstantOperator());
        operators.add(new ChangeGuardOpClocksOperator());
        operators.add(new ChangeGuardOpLocalsOperator());
        operators.add(new ChangeVarUpdateOperator());
        operators.add(new ChangeInvariantOperator());
        operators.add(new SinkLocationOperator());
        operators.add(new InvertResetOperator());

        return operators;
    }
}
