package universe.agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import universe.laws.Constants;

import java.util.ArrayList;
import java.util.Arrays;

public class MemoryAgent extends Agent {

    @Override
    protected void setup() {
        addBehaviour(new CommunicationWithPhagocyte());
    }

    private class CommunicationWithPhagocyte extends CyclicBehaviour{
        @Override
        public void action() {
            MessageTemplate messageTemplate = MessageTemplate.MatchConversationId("DNA_Verification_Channel");
            ACLMessage msg = receive(messageTemplate);

            if(msg != null) {
                String cellDNAString = msg.getContent();
                ArrayList<Integer> updateSet = new ArrayList<>();
                for (Character s:cellDNAString.toCharArray()){
                    updateSet.add(Character.getNumericValue(s));
                }

                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                reply.setConversationId("DNA_Verification_Channel");
                reply.setSender(msg.getSender());

                int[] cellDNA = updateSet.stream().mapToInt(i->i).toArray();

                if (!Arrays.equals(cellDNA, Constants.CELL_IDENTIFYING_DNA)) {
                    reply.setContent("virusPresent");
                } else {
                    reply.setContent("LooksGood");
                }
                send(reply);
            }
        }
    }
}
