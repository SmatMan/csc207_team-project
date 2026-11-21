package use_case.set_diet_preset;

/**
 * Input Boundary for turning a diet preset on or off.
 */
public interface SetDietPresetInputBoundary {

    /**
     * Executes the set diet preset use case.
     * @param setDietPresetInputData the input data
     */
    void execute(SetDietPresetInputData setDietPresetInputData);
}
