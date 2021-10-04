package universe.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.ContainerID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;
import universe.Universe;
import universe.containers.AuxiliaryContainer;
import universe.laws.Movement;

import java.util.ArrayList;
import java.util.Random;

import static universe.laws.Constants.*;

public class PhagocyteAgent extends Agent {

    private String dnaToBeVerified = null;
    private Boolean cellPresentInContainer = true;

    @Override
    protected void setup(){

        // Putting Behaviours in a queue
        SequentialBehaviour afterLandingBehaviour = new SequentialBehaviour() {
            public int onEnd() {
                reset();
                myAgent.addBehaviour(this);
                return super.onEnd();
            }
        };
        afterLandingBehaviour.addSubBehaviour(new AskingCellForIdentity());
        afterLandingBehaviour.addSubBehaviour(new ConsultingWithMemoryAgent());
        afterLandingBehaviour.addSubBehaviour(new MovingToNewCell());

        // Launching the Behaviours
        addBehaviour(new GoingToInitialPosition());
        addBehaviour(afterLandingBehaviour);
    }



    private class GoingToInitialPosition extends OneShotBehaviour{
        @Override
        public void action(){

            try {
                doWait(PHAGOCYTE_SLEEP_TIME);
                moveToNewContainer(Universe.CONTAINER_CONTROLLER_HASH_MAP.get("Container-0"));
            }catch (Exception exception){
                exception.getStackTrace();
            }
        }
    }

    private class MovingToNewCell extends OneShotBehaviour{
        @Override
        public void action() {
            doWait(PHAGOCYTE_SLEEP_TIME);
            Movement movement = new Movement();
            try {
                ContainerController currentContainerController = Universe.CONTAINER_CONTROLLER_HASH_MAP.get(getContainerController().getContainerName());
                System.out.println("\t\uD83E\uDDD0\t" + ANSI_GREEN + "▶" + ANSI_RESET + "  ".concat(currentContainerController.getContainerName()));

                ArrayList<ContainerController> possibleMoves = movement.getAdjacentContainerControllers(currentContainerController);

                Random rand = new Random();
                ContainerController destinationContainer = possibleMoves.get(rand.nextInt(possibleMoves.size()));

                moveToNewContainer(destinationContainer);

            }catch (ControllerException e) {
                e.printStackTrace();
            }

        }
    }

    private void moveToNewContainer(ContainerController destinationContainer){
        try{
            ContainerID dest = new ContainerID();
            dest.setName(destinationContainer.getContainerName());
            this.doMove(dest);
        } catch (Exception exception){
            exception.getStackTrace();
        }

    }

    private class AskingCellForIdentity extends OneShotBehaviour{
        @Override
        public void action() {

            if(AuxiliaryContainer.isCellAlive(this.getAgent().getContainerController())){

                ACLMessage messageToCell = new ACLMessage(ACLMessage.INFORM);  // Message type
                try {
                    String targetCell = "cell.".concat(String.valueOf(this.getAgent().getContainerController().getContainerName()));
                    messageToCell.addReceiver(new AID(targetCell, AID.ISLOCALNAME)); // receiver
                    messageToCell.setConversationId("Signature_Verification_Channel"); // conversation id
                    String messageContent = "Verify_Identity";
                    messageToCell.setContent(messageContent);
                    send(messageToCell);
                } catch (ControllerException e) {
                    e.printStackTrace();
                }
                doWait(PHAGOCYTE_CELL_COMMUNICATION_TIME);
                MessageTemplate messageTemplate = MessageTemplate.MatchConversationId("Signature_Verification_Channel");
                ACLMessage messageFromCell = receive(messageTemplate);
                if(messageFromCell != null){
                    dnaToBeVerified = messageFromCell.getContent();
                }
            }
            else {
                cellPresentInContainer = false;
            }
        }
    }

    private class ConsultingWithMemoryAgent extends OneShotBehaviour{
        @Override
        public void action() {
            if(dnaToBeVerified != null && cellPresentInContainer){
                ACLMessage messageToMemory = new ACLMessage(ACLMessage.INFORM);
                messageToMemory.addReceiver(new AID("memory", AID.ISLOCALNAME));
                messageToMemory.setConversationId("DNA_Verification_Channel");
                messageToMemory.setContent(dnaToBeVerified);
                send(messageToMemory);
            }
            doWait(PHAGOCYTE_MEMORY_COMMUNICATION_TIME);
            MessageTemplate messageTemplate = MessageTemplate.MatchConversationId("DNA_Verification_Channel");
            ACLMessage message = receive(messageTemplate);
            if (message != null){
                String decisionFromMemoryAgent = message.getContent();
                if(decisionFromMemoryAgent.equals("virusPresent")) {
                    repairDNA();
                    if(AuxiliaryContainer.isThereAVirus(this.getAgent().getContainerController())){
                        killTheVirus();
                        System.out.println(ANSI_GREEN + "PHAGOCYTE" + ANSI_RESET + ": \tMemory is right. Virus Present");
                    }else{
                        System.out.println(ANSI_GREEN + "PHAGOCYTE" + ANSI_RESET + ": \tMemory is wrong. No virus Here");
                    }
                }else if(decisionFromMemoryAgent.equals("LooksGood")){
                    if(AuxiliaryContainer.isThereAVirus(this.getAgent().getContainerController())){
                        System.out.println(ANSI_GREEN + "PHAGOCYTE" + ANSI_RESET + ": \tMemory is wrong. Virus Present");
                    }
                }
            }
        }
    }

    private void killTheVirus() {
        try {
            ContainerController thisContainer = this.getContainerController();
            AgentController virusAgentController = thisContainer.getAgent("virus.".concat(thisContainer.getContainerName()));
            System.out.println(ANSI_GREEN + "PHAGOCYTE" + ANSI_RESET + ": \tKilled " +ANSI_RED + "virus" + ANSI_RESET);
            virusAgentController.kill();
        } catch (ControllerException e) {
            e.printStackTrace();
        }
    }

    private void repairDNA(){
        ACLMessage messageToCell = new ACLMessage(ACLMessage.INFORM);  // Message type

        try {
            String targetCell = "cell.".concat(String.valueOf(this.getContainerController().getContainerName()));
            //System.out.println(targetCell);
            messageToCell.addReceiver(new AID(targetCell, AID.ISLOCALNAME)); // receiver
            messageToCell.setConversationId("DNA_Repair_Channel"); // conversation id
            String messageContent = "repair";
            messageToCell.setContent(messageContent);
            send(messageToCell);

        } catch (ControllerException e) {
            e.printStackTrace();
        }
    }
}
