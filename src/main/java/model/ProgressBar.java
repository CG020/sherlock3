package model;


import java.util.concurrent.TimeUnit;

public class ProgressBar {
    public static void printProgressBar(int completed, int total, long startTime) {
        int width = 50; // width of the progress bar
        int progressPercentage = (100 * completed) / total;
        int progress = (width * completed) / total;
        long currentTime = System.currentTimeMillis();
        long timeElapsed = currentTime - startTime; // in milliseconds
        long estimatedTotalTime = (timeElapsed / completed) * total;
        long estimatedTimeLeft = estimatedTotalTime - timeElapsed;

        String timeLeftFormatted = String.format("%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes(estimatedTimeLeft),
                TimeUnit.MILLISECONDS.toSeconds(estimatedTimeLeft) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(estimatedTimeLeft)));

        String elapsedTimeFormatted = String.format("%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes(timeElapsed),
                TimeUnit.MILLISECONDS.toSeconds(timeElapsed) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeElapsed)));

        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < width; i++) {
            if (i < progress) {
                bar.append("=");
            } else {
                bar.append(" ");
            }
        }
        bar.append("] ").append(progressPercentage).append("% ");
        bar.append("Elapsed: ").append(elapsedTimeFormatted).append(", ");
        bar.append("Left: ").append(timeLeftFormatted);

        System.out.print("\r"); // carriage return, to return to the start of the line
        System.out.print(bar.toString());

        if (completed == total) {
            System.out.println("\nCompleted!"); // print a newline when done
        }
    }

    public static void printMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory(); // Maximum memory JVM will attempt to use
        long allocatedMemory = runtime.totalMemory(); // Currently allocated memory
        long freeMemory = runtime.freeMemory(); // Free memory inside allocated
        long usedMemory = allocatedMemory - freeMemory; // Used memory
        long availableMemory = freeMemory + (maxMemory - allocatedMemory); // Approx. available memory

        int width = 50; // Width of the memory usage bar
        long totalMemory = maxMemory; // Consider maxMemory as the total for calculation
        int usedPercentage = (int) ((100 * usedMemory) / totalMemory);
        int progress = (width * usedPercentage) / 100;

        StringBuilder bar = new StringBuilder("Mem [");
        for (int i = 0; i < width; i++) {
            if (i < progress) {
                bar.append("#");
            } else {
                bar.append(" ");
            }
        }
        bar.append("] ").append(usedPercentage).append("% of Max JVM Memory");

        System.out.print("\r"); // carriage return, to return to the start of the line
        System.out.println(bar.toString());
    }

}