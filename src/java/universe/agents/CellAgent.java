package universe.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import universe.Universe;
import universe.laws.Constants;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;


public class CellAgent extends Agent {

    private final int[] myDNA = copyDNAFromSourceCodeOfUniverse();

    private int[] copyDNAFromSourceCodeOfUniverse(){
        int[] replicaDNA = new int[Constants.CELL_IDENTIFYING_DNA.length];
        System.arraycopy(Constants.CELL_IDENTIFYING_DNA, 0, replicaDNA, 0, Constants.CELL_IDENTIFYING_DNA.length);
        return replicaDNA;
    }

    @Override
    protected void setup() {
        //addBehaviour(new HealthyMutation());
        addBehaviour(new ListenToPhagocyte());
        addBehaviour(new ListenToVirus());
        addBehaviour(new DNARepairBehaviour());
    }


    private class ListenToPhagocyte extends CyclicBehaviour{
        @Override
        public void action() {
            MessageTemplate messageTemplate = MessageTemplate.MatchConversationId("Signature_Verification_Channel");
            ACLMessage msg = receive(messageTemplate);

            if(msg != null){

                String messageContent = msg.getContent();
                if(messageContent.equals("Verify_Identity")){

                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setConversationId("Signature_Verification_Channel");
                    reply.setSender(msg.getSender());

                    StringBuilder myDNACode = new StringBuilder();
                    for (int codon: myDNA){
                        myDNACode.append(codon);
                    }

                    reply.setContent(myDNACode.toString());
                    send(reply);

                }
            }
        }
    }

    private  class ListenToVirus extends CyclicBehaviour{

        @Override
        public void action() {

            MessageTemplate messageTemplate = MessageTemplate.MatchConversationId("Update_DNA_Message_From_Virus");
            ACLMessage msg = receive(messageTemplate);

            if(msg != null){

                String messageContent = msg.getContent();
                int[] updatePoints = getPointUpdates(messageContent);
                this.getAgent().removeBehaviour(this);
                updateDNA(updatePoints);
            }
        }
        private int[] getPointUpdates(String messageStream){
            Pattern pattern = Pattern.compile("\\d+");
            Matcher matcher = pattern.matcher(messageStream);
            ArrayList<Integer> updateSet = new ArrayList<>();
            while(matcher.find()) {
                updateSet.add(Integer.valueOf(matcher.group()));
            }
            return updateSet.stream().mapToInt(i->i).toArray();
        }
    }

    private class DNARepairBehaviour extends CyclicBehaviour{
        @Override
        public void action() {
            MessageTemplate messageTemplate = MessageTemplate.MatchConversationId("DNA_Repair_Channel");
            ACLMessage msg = receive(messageTemplate);

            if(msg != null) {
                String messageContent = msg.getContent();
                if(messageContent.equals("repair")){
                    System.arraycopy(Constants.CELL_IDENTIFYING_DNA, 0, myDNA, 0, Constants.CELL_IDENTIFYING_DNA.length);
                    this.getAgent().addBehaviour(new ListenToVirus());
                }
            }
        }
    }

    private class HealthyMutation extends CyclicBehaviour{
        @Override
        public void action(){
            doWait(Constants.CELL_MUTATION_PERIOD);
            int randomPosition = (int) (Math.random()*(myDNA.length));
            updateDNA(new int[]{randomPosition});
        }
    }


    private void updateDNA(int[] flipLocations){

        for(int flipLocation: flipLocations){
            if(myDNA[flipLocation] == 0){
                this.myDNA[flipLocation] = 1;
            } else if(myDNA[flipLocation] == 1){
                this.myDNA[flipLocation] = 0;
            }
        }
    }

}
