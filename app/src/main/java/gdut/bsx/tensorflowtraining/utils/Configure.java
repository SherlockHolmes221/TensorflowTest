package gdut.bsx.tensorflowtraining.utils;

public class Configure {

    public static final int INPUT_SIZE = 299;
    public static final int IMAGE_MEAN = 128;
    public static final float IMAGE_STD = 128;
    public static final String INPUT_NAME = "Mul";
    public static final String OUTPUT_NAME = "final_result";
    public static final String MODEL_FILE = "file:///android_asset/model/yoga_optimized_graph.pb";
    public static final String LABEL_FILE = "file:///android_asset/model/retrained_labels.txt";

    public static int CURRENT_MODE;


    public static int getCurrentMode() {
        return CURRENT_MODE;
    }

    public static void setCurrentMode(int currentMode) {
        CURRENT_MODE = currentMode;
    }
}
