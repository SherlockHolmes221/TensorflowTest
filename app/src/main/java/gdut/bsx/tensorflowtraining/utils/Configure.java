package gdut.bsx.tensorflowtraining.utils;

public class Configure {

    public static final int INPUT_SIZE = 299;
    public static final int IMAGE_MEAN = 299;
    public static final float IMAGE_STD = 1;
    public static final String INPUT_NAME = "Mul";
    public static final String OUTPUT_NAME = "final_result";
    public static final String MODEL_FILE = "file:///android_asset/model/optimized_graph.pb";
    public static final String LABEL_FILE = "file:///android_asset/model/retrained_labels.txt";

}
