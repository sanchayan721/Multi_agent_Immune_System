package universe.containers;

import jade.wrapper.AgentContainer;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.ExtendedProperties;
import jade.util.leap.Properties;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class MainContainer {

    public AgentContainer createMainContainer(){

        Runtime runtime = Runtime.instance();
        Properties properties = new ExtendedProperties();
        properties.setProperty(Profile.GUI, "true");
        Profile profile = new ProfileImpl(properties);
        return runtime.createMainContainer(profile);

    }

    public void createPhagocyteAndMemoryAgents(ContainerController containerController){
        try {
            AgentController memoryAgentController = containerController.createNewAgent("memory", "universe.agents.MemoryAgent", new Object[]{});
            AgentController phagocyteAgentController = containerController.createNewAgent("phagocyte", "universe.agents.PhagocyteAgent", new Object[]{});
            memoryAgentController.start();
            phagocyteAgentController.start();

        } catch (StaleProxyException exception) {
            exception.printStackTrace();
        }

    }
}
