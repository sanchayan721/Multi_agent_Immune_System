package universe.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;
import universe.Universe;
import universe.containers.AuxiliaryContainer;
import universe.laws.Constants;
import universe.laws.Movement;
import java.util.ArrayList;
import java.util.Random;

import static universe.laws.Constants.*;

public class VirusAgent extends Agent {

    @Override
    protected void setup() {


        // Putting Behaviours in a queue
        SequentialBehaviour virusBehaviour = new SequentialBehaviour() {
            public int onEnd() {
                reset();
                myAgent.addBehaviour(this);
                return super.onEnd();
            }
        };
        virusBehaviour.addSubBehaviour(new CommunicateWithCell());
        virusBehaviour.addSubBehaviour(new CloningBehaviour());
        virusBehaviour.addSubBehaviour(new KillingACell());

        addBehaviour(virusBehaviour);
    }

    private class CloningBehaviour extends OneShotBehaviour{
        Movement adjacentContainers = new Movement();
        public void action() {

            doWait(VIRUS_REPLICATION_TIME);

            try {

                ContainerController currentContainerController = Universe.CONTAINER_CONTROLLER_HASH_MAP.get(getContainerController().getContainerName());
                String currentContainerName = currentContainerController.getContainerName();
                //System.out.println("Yo! Virus at ".concat(currentContainerName));
                ArrayList<ContainerController> possiblePlacesToClone = adjacentContainers.getAdjacentContainerControllers(currentContainerController);
                ArrayList<ContainerController> cloningLocations = getPlacesToClone(possiblePlacesToClone);
                //System.out.println(cloningLocations);
                cloneInContainers(cloningLocations);

            } catch (Exception exception){
                exception.getStackTrace();
            }
        }

        private ArrayList<ContainerController> getPlacesToClone(ArrayList<ContainerController> possiblePlaces){
            ArrayList<ContainerController> cloningLocation = new ArrayList<>();
            Random random = new Random();
            if(universe.laws.Constants.VIRUS_REPLICATION_FACTOR > possiblePlaces.size()){
                return possiblePlaces;
            } else {
                for (var i = 0; i < universe.laws.Constants.VIRUS_REPLICATION_FACTOR; i++){
                    int randomIndex = random.nextInt(possiblePlaces.size());
                    cloningLocation.add(possiblePlaces.get(randomIndex));
                }
                return cloningLocation;
            }
        }

        private void cloneInContainers(ArrayList<ContainerController> locations) throws ControllerException {
            for(ContainerController destinationController: locations){

                if (AuxiliaryContainer.isCellAlive(destinationController)){

                    if(!AuxiliaryContainer.isThereAVirus(destinationController)){

                        AgentController newClone = destinationController.createNewAgent(
                                "virus.".concat(destinationController.getContainerName()),
                                "universe.agents.VirusAgent", new Object[]{}
                        );
                        newClone.start();
                    }

                }
            }
        }
    }

    private class KillingACell extends OneShotBehaviour{
        @Override
        public void action() {

            doWait(KILL_THE_CELL_AFTERWARD);

            try {

                ContainerController thisContainer = Universe.CONTAINER_CONTROLLER_HASH_MAP.get(this.getAgent().getContainerController().getContainerName());
                AgentController cellAgentController = thisContainer.getAgent("cell.".concat(thisContainer.getContainerName()));
                AgentController virusAgentController = thisContainer.getAgent("virus.".concat(thisContainer.getContainerName()));
                virusAgentController.kill();
                cellAgentController.kill();
                System.out.println(ANSI_RED + "VIRUS" + ANSI_RESET + ": \tKilled ".concat(cellAgentController.getName()));

            } catch (ControllerException e) {
                e.printStackTrace();
            }
        }
    }

    private class CommunicateWithCell extends OneShotBehaviour{
        @Override
        public void action() {

            ACLMessage messageToCell = new ACLMessage(ACLMessage.INFORM);  // Message type

            try {
                String targetCell = "cell.".concat(String.valueOf(this.getAgent().getContainerController().getContainerName()));
                messageToCell.addReceiver(new AID(targetCell, AID.ISLOCALNAME)); // receiver
                messageToCell.setConversationId("Update_DNA_Message_From_Virus"); // conversation id
            } catch (ControllerException e) {
                e.printStackTrace();
            }
            // Send message to Cell About Genetic Modification
            messageToCell.setContent(VIRUS_SIGNATURE);
            send(messageToCell); // sending method
        }
    }

}
