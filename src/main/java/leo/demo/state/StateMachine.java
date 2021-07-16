package leo.demo.state;

import java.util.LinkedList;
import java.util.List;

public class StateMachine<S, E, C, T> {
    /**
     * The list of transitions that comprise and define the state machine.
     */
    private final List<StateTransition<S, E, C, T>> transitions = new LinkedList<>();

    /**
     * Initial state of the state machine.
     * <p>
     * Initial invocations of a {@link StateMachineInstance} will trigger from this state.
     */
    private final S initialState;

    /**
     * Create a new state machine with the given initial state.
     *
     * @param initialState Initial state of the state machine.
     */
    public StateMachine(S initialState) {
        this.initialState = initialState;
    }

    /**
     * Adds a transition to the state machine.
     *
     * @param transition Transition to add to the state machine.
     */
    public void addTransition(StateTransition<S, E, C, T> transition) {
        this.transitions.add(transition);
    }

    /**
     * Starts a new instance of the state machine, and returns the instance object.
     *
     * @param context A context for the state machine instance that will be provided to all action invocations.
     * @return A new state machine instance containing the state of the state machine.
     */
    public StateMachineInstance<S, E, C, T> start(C context) {
        return new StateMachineInstance<>(this, context);
    }

    /**
     * Returns the initial state of the state machine.
     *
     * @return The initial state of the state machine.
     */
    public S getInitialState() {
        return initialState;
    }

    /**
     * Returns the transitions that define the state machine.
     *
     * @return The transitions that define the state machine.
     */
    public List<StateTransition<S, E, C, T>> getTransitions() {
        return transitions;
    }
}
