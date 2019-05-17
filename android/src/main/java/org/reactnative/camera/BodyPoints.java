package org.reactnative.camera;

public class BodyPoints {
    private int[][] mBodyPoints;

    private static final int BODYPART_COUNT = 14;
    private static final int ROW_COUNT = 96;
    private static final int COL_COUNT = 96;
    private static final int COORDINATE_COUNT = 2;
    private static final float GAUSSIAN_WEIGHT = 0.2f;

    public BodyPoints(float[][][] heatmap){
        mBodyPoints = getMaxValues(heatmap);
    }

    public int[][] getBodyPoints() { return mBodyPoints;}

    /**
     * Goes through all values in the heatmap and returns the locations of the max for each limb.
     * @param heatmap A [96][96][14] sized nested array.
     * @return A [14][2] array with x y coordinates for each limb. Inner array is empty, if the given limb wasn't found.
     */
    private static int[][] getMaxValues(float[][][] heatmap) {
        int[][] arr = new int[BODYPART_COUNT][COORDINATE_COUNT];
        for (int bodypart = 0; bodypart < BODYPART_COUNT; ++bodypart) {
            //Resetting max for each bodypart
            float max =  0;

            // Default values bodypoint-not-found. iOS does the same.
            arr[bodypart][0] = -1;
            arr[bodypart][1] = -1;

            //First and last rows and columns are ignored to avoid issues when blurring
            for (int row= 1; row < ROW_COUNT - 1; ++row) {
                for (int col = 1; col < COL_COUNT - 1; ++col) {
                    float gaussianValue = getGaussian(heatmap, bodypart, row, col);
                    //If this value is the best so far, we save it
                    if (gaussianValue > max) {
                        max = gaussianValue;
                        // Flipped "back" on purpose, as the model flips the output
                        arr[bodypart][0] = col;
                        arr[bodypart][1] = row;
                    }
                }
            }
        }
        return arr;
    }

    private static float getGaussian(float[][][] heatmap, int bodypart, int row, int col){
        float sum = 0;
        //Iterating over the pixels adjacent to current.
        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                int currentRow = row + i;
                int currentCol = col + j;

                float value = heatmap[currentRow][currentCol][bodypart];
                float weight = i == 0 && j == 0 ? 1 : GAUSSIAN_WEIGHT;
                sum += value * weight;
            }
        }
        return sum;
    }
}

