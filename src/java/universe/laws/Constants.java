package universe.laws;


public class Constants {

    public static final int GRID_SIZE = 5;
    public static final int UNIVERSE_SIZE = GRID_SIZE * GRID_SIZE;
    public static final int PHAGOCYTE_SLEEP_TIME = 2000; //Seconds
    public static final int PHAGOCYTE_CELL_COMMUNICATION_TIME = 100;
    public static final int PHAGOCYTE_MEMORY_COMMUNICATION_TIME = 100;
    public static final int VIRUS_REPLICATION_TIME = 5000; //Seconds
    public static final int VIRUS_REPLICATION_FACTOR = 2;
    public static final int[] CELL_IDENTIFYING_DNA = new int[]
            {0, 1, 1, 0, 1, 0, 1, 0, 0, 1, 1, 0, 1, 0, 0, 1, 0, 1, 1, 1, 0, 0, 0, 1, 0, 1, 1, 0, 1, 0, 0, 1, 0, 1, 0, 1, 1, 1, 1, 0, 0, 1, 0, 1, 0};
    public static final int CELL_MUTATION_PERIOD = 5000; //Seconds
    public static final int KILL_THE_CELL_AFTERWARD = 20000;
    public static final String VIRUS_SIGNATURE = "10,25,20,22";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RESET = "\u001B[0m";
}
