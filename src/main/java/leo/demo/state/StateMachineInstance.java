package leo.demo.state;

public class StateMachineInstance<S, E, C, T> {
    /**
     * The {@link StateMachine} that the instance represents.
     */
    private final StateMachine<S, E, C, T> stateMachine;

    /**
     * Optional context that will be passed to all action invocations.
     */
    private final C context;

    /**
     * Current state of the state machine.
     */
    private S currentState;

    /**
     * Builds a new state machine instance with the state machine the instance will represent and an optional context.
     *
     * @param stateMachine State machine definition.
     * @param context      Optional context to provide to actions.
     */
    StateMachineInstance(StateMachine<S, E, C, T> stateMachine, C context) {
        this.stateMachine = stateMachine;
        this.context = context;
        this.currentState = stateMachine.getInitialState();
    }

    /**
     * Triggers a state transition in the state machine based on the given event and object.
     *
     * @param event  Event that will trigger a transition in the state machine.
     * @param object An optional object that represents some data related to the given event.
     */
    public void submit(E event, T object) {
        for (StateTransition<S, E, C, T> transition : stateMachine.getTransitions()) {
            if (transition.getFrom() == currentState && transition.getEvent() == event) {
                currentState = transition.getTo();

                if (transition.getAction() != null) {
                    transition.getAction().accept(context, object);
                }

                return;
            }
        }

        throw new IllegalStateException(
            "Unable to transition from state " + currentState + " on event " + event + " with object " + (object != null ? object.toString() : "null"));
    }
}
