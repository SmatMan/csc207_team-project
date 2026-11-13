package use_case.set_diet_preset;

/**
 * The Input Data for the Set Diet Preset Use Case.
 */
public class SetDietPresetInputData {

    private final String presetName;
    private final boolean active;

    public SetDietPresetInputData(String presetName, boolean active) {
        this.presetName = presetName;
        this.active = active;
    }

    String getPresetName() {
        return presetName;
    }

    boolean isActive() {
        return active;
    }

}
