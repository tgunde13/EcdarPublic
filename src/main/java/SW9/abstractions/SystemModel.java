package SW9.abstractions;

import SW9.utility.UndoRedoStack;
import SW9.utility.colors.Color;
import SW9.utility.colors.EnabledColor;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A model of a system
 */
public class SystemModel extends HighLevelModelObject {
    // TODO brug Christians løsning
    private static final AtomicInteger hiddenId = new AtomicInteger(0); // Used to generate unique IDs

    // Verification properties
    private final StringProperty description = new SimpleStringProperty("");

    // Styling properties
    private final Box box = new Box();

    public SystemModel() {
        setRandomColor();
    }

    SystemModel(final JsonObject json) {
        deserialize(json);
    }

    public Box getBox() {
        return box;
    }

    public StringProperty getDescriptionProperty() {
        return description;
    }

    /**
     * Dyes the system.
     * @param color the color to dye with
     * @param intensity the intensity of the color
     */
    public void dye(final Color color, final Color.Intensity intensity) {
        final Color previousColor = colorProperty().get();
        final Color.Intensity previousColorIntensity = colorIntensityProperty().get();

        UndoRedoStack.pushAndPerform(() -> { // Perform
            // Color the component
            setColorIntensity(intensity);
            setColor(color);
        }, () -> { // Undo
            // Color the component
            setColorIntensity(previousColorIntensity);
            setColor(previousColor);
        }, String.format("Changed the color of %s to %s", this, color.name()), "color-lens");
    }

    @Override
    public JsonObject serialize() {
        final JsonObject result = super.serialize();

        result.addProperty(DESCRIPTION, getDescription());

        box.addProperties(result);

        result.addProperty(COLOR, EnabledColor.getIdentifier(getColor()));

        return result;
    }

    @Override
    public void deserialize(final JsonObject json) {
        super.deserialize(json);

        setDescription(json.getAsJsonPrimitive(DESCRIPTION).getAsString());

        box.setProperties(json);

        final EnabledColor enabledColor = EnabledColor.fromIdentifier(json.getAsJsonPrimitive(COLOR).getAsString());
        if (enabledColor != null) {
            setColorIntensity(enabledColor.intensity);
            setColor(enabledColor.color);
        }
    }

    public String getDescription() {
        return description.get();
    }

    public void setDescription(final String description) {
        this.description.setValue(description);
    }
}