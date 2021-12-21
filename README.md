## Introduction
Immune System of any multi-cellular organism is a very complex system with a lot of different cells playing important role to keep a body infection free. A typical scenario when all of these players are playing their role can be seen in case of an infection. A serious virus infection is one such case. The goal of the virus is to hijack a cell and use its resources to make a copy of itself which is called replication and the spreading those replicas into other other parts of the body. Immune system helps an organism to fight back the infection by literally searching for any virus signature moving from one cell to another. Immune system in general consists of multiple different cells with different behaviours some of them kills the virus called Phagocytes, some of them remembers the signature of an infection called Memory cells which is essential to prevent similar infection in future.  

## Goal
The goal of this project is to model components of the Immune system and their behaviours using self-motivated interacting agents using Java Middle-ware Java Agent Development Framework (JADE) and simulate a situation when the immune system is under attack from a virus.  

## Universe
Universe is the class which controls the dynamics of the application and it is the second entry point of the application after App.java. It instantiates two different types of containers namely the Primary and the Auxiliary and initializes all of the Agents. Initially it creates a Primary container and creates a Memory Agent and a Phagocyte Agent inside it.

The Grid is a virtual Cartesian Coordinate system where the containers live in. Depending upon the size of the Grid, Universe creates Auxiliary containers and a <code>HashMap</code> of the Container Controllers and their position on the grid, so that this map can later be used by the Agents in order to move around the the universe. The code snippet below demonstrates how the grids are allocated to the Auxiliary Containers and how the HashMap is created.
<p>
Afterwards the Cell Agents are instantiated in those containers with their name containing that of the Container they belong to.  </p>

## Constants

```java
    public static final int GRID_SIZE = 4;
    public static final int UNIVERSE_SIZE = GRID_SIZE * GRID_SIZE;
    public static final int PHAGOCYTE_SLEEP_TIME = 1000; //Seconds
    public static final int VIRUS_REPLICATION_TIME = 10000; //Seconds
    public static final int VIRUS_REPLICATION_FACTOR = 2;
    public static final int[] CELL_IDENTIFYING_DNA = new int[]
            {0, 1, 1, 0, 1, 0, 1, 0, 0, 1, 1, 0, 1, 0, 0, 1, 0, 1, 1, 1, 0, 0, 0, 1, 0, 1, 1, 0, 1, 0, 0, 1, 0, 1, 0, 1, 1, 1, 1, 0, 0, 1, 0, 1, 0};
    public static final int CELL_MUTATION_PERIOD = 10000; //Seconds
    public static final int KILL_THE_CELL_AFTERWARD = 10000;
```

## Path Router
The path-router is used by all of the agents to get the nearby-containers of a given container, by passing a container controller it returns all adjacent container controllers in north, south, east, west, north-east, south-east, north-west and south-west direction. 

## Containers

In JADE, an agent lives inside a container. Instead of creating all of my agents in the main container, I have created a grid of containers. The parks of doing that is solely the scalability of the model. In some later point in time if the project is planned to be implemented in a Cluster, this approach is  There are two types of containers used in this project. Primary container which only stores meta-agents and initially the phagocyte agents and the auxiliary container where rest of the agents live.

### Main Container
The main Container is created at the beginning of the program and instantiates the memory agent and the phagocyte agent and later point in time only holds the Memory Agent.

```java
    Runtime runtime = Runtime.instance();
        Properties properties = new ExtendedProperties();
        properties.setProperty(Profile.GUI, "true");
        Profile profile = new ProfileImpl(properties);
        return runtime.createMainContainer(profile);
```

### Auxiliary Container
The auxiliary container is the one that contains all of the cell agents, the phagocyte agents and the virus. This container also has a method <code>isCellAlive()</code> to check if the cell is alive in the container and <code>isThereAVirus()</code> method which is used by the virus itself while replication.

```java
    public ContainerController CreateAuxiliaryContainer(int index){

        id = String.valueOf(index);

        Runtime runtime = Runtime.instance();
        Profile profile = new ProfileImpl(false);
        profile.setParameter(Profile.MAIN_HOST, "localhost");
        profile.setParameter(Profile.CONTAINER_NAME, "Container-".concat(id));
        auxiliaryContainerController = runtime.createAgentContainer(profile);

        return auxiliaryContainerController;
    }
```


## Agents

4 different agents are present in this project namely **CellAgent**, **phagocyteAgent**, **memoryAgent** and **virus**. We will discuss their behaviour and communication methods below.

### Cell Agent
A Cell Agent as the name suggests represent a cell in the body of an multi-cellular organism. It lives inside an auxiliary container and has an identifying DNA which it uses in order to verify itself to the immune system. As it often occurs in autoimmune diseases where the identification can not be verified by the immune system, it gets killed. The virus also uses this signature to bind to the cell membrane in order to enter the cell and replicate itself. This action changes some part of the DNA and works as a marker for the immune cells to detect the infection and kill the virus. Once the DNA is changed, the cell no longer makes any communication with the virus agent in order to keep the infection status to be unchanged until the virus is killed by the phagocyte and the status is changed back to normal.

#### Behaviours
  * In order to communicate with the virus in the beginning of the infection there exists a Listen to virus behaviour which is a cyclic behaviour which is later removed upon infection.
  * It also has a behaviour for listening to the phagocyte Agent which is a Cyclic Behaviour.
  * In order to repair its own DNA once the virus is removed by the Phagocyte, it has a repairDNA behaviour, which is a Cyclic Behaviour.

```java
protected void setup() {
    addBehaviour(new HealthyMutation());
    addBehaviour(new ListenToPhagocyte());
    Behaviour listenToVirus = new ListenToVirus();
    if(Arrays.equals(this.myDNA, Constants.CELL_IDENTIFYING_DNA)){
        addBehaviour(listenToVirus);
    } else{
        this.removeBehaviour(listenToVirus);
    }
    addBehaviour(new DNARepairBehaviour());
}
```

#### Communication
  * For communication with the virus it uses FIPA-ACL Messages "INFORM" with Update_DNA_Message_From_Virus communication channel.
  * For verifying its DNA Identity it uses FIPA-ACL Message with Signature_Verification_Channel communication channel.
  * For receiving repair DNA command, it uses the same messaging format with DNA_Repair_Channel.

```java
MessageTemplate messageTemplate = MessageTemplate.MatchConversationId("DNA_Repair_Channel");
        ACLMessage msg = receive(messageTemplate);
        if(msg != null) {
            String messageContent = msg.getContent();
            System.out.println(messageContent);
            if(messageContent.equals("repair")){ repairDNA();}
```


### Phagocyte Agent

The Phagocyte is the agent which executes the task of killing the virus. Upon initiation it is created inside the Primary Container along with the Memory Agent. This is a mobile Agent and can route from one container to another at its own atomic will following the rules of the Universe. After moving to a new container, it asks the cell Agent present in that container to verify its Identifying DNA sequence. upon retrieval of the DNA it communicates with the Memory Agent for Identification of the Sequence and waits for further orders from it. If it gets a kill Order from the Memory Agent, It executes the order by killing the virus present in that container and sends a request to the cell present in that container to repair its DNA so that it can identify any further infection in that cell in some later point in time otherwise, it moves on to a new Container repeating its Cyclic Behaviour. The Method below shows the killing of a virus.

```java
private void killTheVirus() {
        try {
            ContainerController thisContainer = Universe.CONTAINER_CONTROLLER_HASH_MAP.get(this.getContainerController()
                                                    .getContainerName());
            AgentController virusAgentController = thisContainer.getAgent("virus.".concat(thisContainer.getContainerName()));
            virusAgentController.kill();
        } catch (ControllerException e) {
            e.printStackTrace();
        }
    }
```


#### Behaviours
  * Going to Initial Position in the beginning of its life-cycle. Which is an One-shot Behaviour.
  * Since this is a mobile agent, it has a behaviour of moving to a new cell, which is a Cyclic Behaviour.
  * It wants to communicate with the cell in the container in order to verify its identity and therefore sending it back to the Memory Agent, which is a Cyclic Behaviour.
  * Since it is not equip with the necessary tools to identify weather a DNA is a contains signature of a virus, it needs to send the code back to the Memory Agent, which is a Cyclic Behaviour.

```java
protected void setup(){
        addBehaviour(new GoingToInitialPosition());
        addBehaviour(new MovingToNewCell());
        addBehaviour(new AskingCellForIdentity());
        addBehaviour(new ConsultingWithMemoryAgent());
    }
 ```

#### Means of Communication - Message Passing
  * <code>Cell Agent:</code> It communicates with the Cell Agent using FIPA-ACL Message "INFORM" using Signature_Verification_Channel for requesting and retrieving the DNA Identification Code  from the cell.
  * <code>Memory Agent:</code> It communicates with the Memory Agent using the same FIPA-ACL Message type for sending the DNA code and receiving KillOrders.

```java
ACLMessage messageToMemory = new ACLMessage(ACLMessage.INFORM);
                messageToMemory.addReceiver(new AID("memory", AID.ISLOCALNAME));
                messageToMemory.setConversationId("DNA_Verification_Channel");
                messageToMemory.setContent(dnaToBeVerified);
                send(messageToMemory);
```

### Memory Agent
Memory Agent it the one which detects if a cell is infected or not by looking at its DNA signature. It already have a pretty good idea about how the usual cell DNA should look like, and in case of a mismatch it sends a "kill" signal to the Phagocyte agent ordering it to kill the cell. Memory agent lives in the main container and is immobile in practice its a meta agent and communicates through message passing only with the Phagocyte Agent where as in reality the signals are usually chemical or electrical signals. 

#### Behaviours
  * Since it is immobile I have only implemented one behaviour which is Communication with the Phagocyte Agent, which is a Cyclic Behaviour.

```java
    private class CommunicationWithPhagocyte extends CyclicBehaviour{
        @Override
        public void action() {
        ...
        }
    }
```




#### Communication
  * In order to receive any new DNA signature from the phagocyte agent it uses FIPA-ACL Protocol with DNA_Verification_Channel.
  * In order to send kill signal to the same it uses the very same as well.

```java
ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                reply.setConversationId("DNA_Verification_Channel");
                reply.setSender(msg.getSender());
                reply.setContent("virusPresent");
                send(reply);
```


### Virus

The virus Agent mimics a virus in real life who's goal is to replicate itself using the resources from the host cell. The virus is not a mobile agent it can only move by replicating itself to the adjacent cells. Like found in nature, the virus Agent also has a Replication Factor also known as R-factor, which decides how many copies it can make before destroying the host cell. Although in this application it is a variable, I have used R factor to be $2$ while coding, although any integer number of factor can be set for this constant along with the replication time.

<p> The virus Agent is created by the Universe in a Random container in the Grid in the beginning. After that the virus asks the cell in that container to change its DNA sequence by marking that virus is present in that container. Although complex hiding mechanisms are possible while modeling this, I have simply chosen a standard white distribution. </p>

<p> Afterwards it makes replication number of copies at random to the randomly chosen nearby Containers from the set of all possible containers following the mobility rules of the universe and starts this same behaviour in those containers as well. Before replication it also checks if the cell is already been killed by one of its copies or one of its copy is already present in the cell and it that case it drops the behaviour of replicating itself to the new container. </p>

```java
AgentController newClone = destinationController.createNewAgent(
                "virus.".concat(destinationController.getContainerName()),
                "universe.agents.VirusAgent", new Object[]{}
);
newClone.start();
```

After some time, it kills the cell and thereby commits suicide. 

#### Behaviours
  * Communicating with the cell agent, Which is a Cyclic Behaviour.
  * Cloning is a Cyclic Behaviour.
  * Killing the Cell and committing suicide is an One-shot Behaviour.

```java
    protected void setup() {
        Behaviour cloningBehaviour = new CloningBehaviour();
        if(!isClonedBefore){
            this.addBehaviour(cloningBehaviour);
            isClonedBefore = true;
        }else {
            this.removeBehaviour(cloningBehaviour);
        }
        addBehaviour(new CommunicateWithCell());
        addBehaviour(new KillingACell());
    }
```

#### Means of Communications 
  * <code>Cell Agent:</code> In order to communicate with the cell in order to request it to change its DNA the virus agent uses FIPA-ACL "INFORM" Message Template with Update_DNA_Message_From_Virus communication channel.

```java
    ACLMessage messageToCell = new ACLMessage(ACLMessage.INFORM);  // Message type

            try {
                String targetCell = "cell.".concat(String.valueOf(this.getAgent()
                .getContainerController().getContainerName()));
                //System.out.println(targetCell);
                messageToCell.addReceiver(new AID(targetCell, AID.ISLOCALNAME)); // reciever
                messageToCell.setConversationId("Update_DNA_Message_From_Virus"); // coversation id
            } catch (ControllerException e) {
                e.printStackTrace();
            }
```

## Conclusion
 This project is successful in terms of modelling a very simple scenario of a situation where a multi-cellular organism is infected by a virus and could successfully stop the infection with the help of Immune cells modelled with Agents with atomic behaviour. In future machine learning models can be implemented on a distributed environment in order to train the memory cell to perform better.  
