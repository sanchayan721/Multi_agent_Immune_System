package universe;

import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;
import universe.containers.AuxiliaryContainer;
import universe.containers.MainContainer;
import universe.laws.Constants;

import java.util.Arrays;
import java.util.HashMap;

public class Universe {

    public static final HashMap<String, ContainerController> CONTAINER_CONTROLLER_HASH_MAP = new HashMap<>();
    public static final HashMap<ContainerController, int[]> CONTROLLER_GRID_MAP = new HashMap<>();

    public void startUniverse(){
        // Creating MainContainer
        MainContainer mainContainer = new MainContainer();
        ContainerController mainContainerController = mainContainer.createMainContainer();

        // Creating AuxiliaryContainer and HashMap
        AuxiliaryContainer auxiliaryContainer = new AuxiliaryContainer();

        int index = 0;
        for(int index_y = 0; index_y < Constants.GRID_SIZE; index_y++){
            for(int index_x = 0; index_x < Constants.GRID_SIZE; index_x++){

                ContainerController containerController = auxiliaryContainer.CreateAuxiliaryContainer(index);
                try {
                    CONTAINER_CONTROLLER_HASH_MAP.put(containerController.getContainerName(), containerController);
                    CONTROLLER_GRID_MAP.put(containerController, new int[]{index_x, index_y});
                } catch (Exception exception){
                    exception.getStackTrace();
                }
                index++;
            }
        }

        // Assigning Grid Value to AuxiliaryContainers


        // Create Memory Agent and Phagocyte Agent in Main Container
        mainContainer.createPhagocyteAndMemoryAgents(mainContainerController);

        // Create Cell Agents in Auxiliary Containers
        for(ContainerController auxiliaryContainerController: CONTROLLER_GRID_MAP.keySet()){
            auxiliaryContainer.createCell(auxiliaryContainerController);
        }
        // Creating First Virus Agent on A Random Container
        int randInt = (int) (Math.random()*(Constants.UNIVERSE_SIZE));
        ContainerController randomContainerController = CONTAINER_CONTROLLER_HASH_MAP.get("Container-".concat(String.valueOf(randInt)));

        try {
            AgentController virusController = randomContainerController.createNewAgent(
                    "virus.".concat(randomContainerController.getContainerName()),
                    "universe.agents.VirusAgent", new Object[]{}
            );
            System.out.println("First "+Constants.ANSI_RED + "Virus " + Constants.ANSI_RESET +
                                    "dropped at ".concat(randomContainerController.getContainerName()));
            virusController.start();
        } catch (Exception exception){
            exception.getStackTrace();
        }
    }
}
