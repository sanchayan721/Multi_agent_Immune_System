package universe.laws;

import jade.wrapper.ContainerController;
import universe.Universe;

import java.util.*;

public class Movement {

    int gridDimensions = Constants.GRID_SIZE;

    public ArrayList<ContainerController> getAdjacentContainerControllers(ContainerController currentContainerController) {
        int[] currentCoordinate = Universe.CONTROLLER_GRID_MAP.get(currentContainerController);

        ArrayList<ContainerController> movableContainers = new ArrayList<>();
        int[][] possibleMoves;
        int[] west = {-1, 0};
        int[] east = {1, 0};
        int[] north = {0, -1};
        int[] south = {0, 1};
        int[] north_east = {1, -1};
        int[] north_west = {-1, -1};
        int[] south_east = {1, 1};
        int[] south_west = {-1, 1};


        if (currentCoordinate[0] == 0) {
            if (currentCoordinate[1] == 0) {
                possibleMoves = new int[][]{east, south_east, south};
            } else if (currentCoordinate[1] == gridDimensions - 1) {
                possibleMoves = new int[][]{east, north_east, north};
            } else {
                possibleMoves = new int[][]{east, north_east, south_east, south};
            }
        } else if (currentCoordinate[0] == gridDimensions - 1) {
            if (currentCoordinate[1] == 0) {
                possibleMoves = new int[][]{west, south_west, south};
            } else if (currentCoordinate[1] == gridDimensions - 1) {
                possibleMoves = new int[][]{north, north_west, west};
            } else {
                possibleMoves = new int[][]{north, north_west, west, south_west, south};
            }
        } else if (currentCoordinate[1] == 0) {
            possibleMoves = new int[][]{west, south_west, south, south_east, east};
        } else if (currentCoordinate[1] == gridDimensions - 1) {
            possibleMoves = new int[][]{east, north_east, north, north_west, west};
        } else {
            possibleMoves = new int[][]{east, north_east, north, north_west, west, south_west, south, south_east};
        }

        for (int[] move : possibleMoves) {
            int[] coordinate = {currentCoordinate[0] + move[0], currentCoordinate[1] + move[1]};
            movableContainers.add(getKeyController(Universe.CONTROLLER_GRID_MAP, coordinate));
        }
        //System.out.println(movableContainers);
        return movableContainers;

    }

    public ContainerController getKeyController(Map<ContainerController, int[]> map, int[] value) {
        ContainerController keyContainerController = null;
        for (ContainerController controller : map.keySet()) {
            if (Arrays.equals(map.get(controller), (value))) {
                keyContainerController = controller;
            }
        }
        return keyContainerController;
    }
}
