package model;


public class ProgressBar {
    public static void printProgressBar(int completed, int total) {
        int width = 50; // width of the progress bar
        int progressPercentage = (100 * completed) / total;
        int progress = (width * completed) / total;

        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < width; i++) {
            if (i < progress) {
                bar.append("=");
            } else {
                bar.append(" ");
            }
        }
        bar.append("] ").append(progressPercentage).append("%");

        System.out.print("\r"); // carriage return to return to the start of the line
        System.out.print(bar.toString());

        if (completed == total) {
            System.out.println("\nCompleted!"); // print a newline when done
        }
    }
}
