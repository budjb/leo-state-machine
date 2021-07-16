package leo.demo.state;

import java.util.function.BiConsumer;

public class StateTransition<S, E, C, T> {
    /**
     * State the transition starts from.
     * <p>
     * A {@link StateMachine} uses this to determine if this transition is appropriate for a given event.
     */
    private final S from;

    /**
     * State that the transition will cause a {@link StateMachine} to progress to.
     */
    private final S to;

    /**
     * The event that the transition will react to.
     * <p>
     * A {@link StateMachine} uses this to determine if this transition is appropriate for a given state.
     */
    private final E event;

    /**
     * An optional action to take if this transition matches a given starting state and event.
     * <p>
     * If present, the action will be invoked once the transition has occurred. In the absence of an action, a state
     * transition will progress a state machine with no further side effects.
     */
    private final BiConsumer<C, T> action;

    /**
     * Constructor for transitions that do not contain an action.
     *
     * @param from State to transition from.
     * @param to   State to transition to.
     * @param on   Event the will trigger the transition.
     */
    public StateTransition(S from, S to, E on) {
        this(from, to, on, null);
    }

    /**
     * Constructor for transitions that contain an action.
     *
     * @param from   State to transition from.
     * @param to     State to transition to.
     * @param on     Event the will trigger the transition.
     * @param action Action that will be invoked when the transition occurs.
     */
    public StateTransition(S from, S to, E on, BiConsumer<C, T> action) {
        this.from = from;
        this.to = to;
        this.event = on;
        this.action = action;
    }

    /**
     * Returns the starting state of the transition.
     *
     * @return The starting state of the transition.
     */
    public S getFrom() {
        return from;
    }

    /**
     * Returns the resulting state of the transition.
     *
     * @return The resulting state of the transition.
     */
    public S getTo() {
        return to;
    }

    /**
     * Returns the event that will trigger the transition.
     *
     * @return The event that will trigger the transition.
     */
    public E getEvent() {
        return event;
    }

    /**
     * Returns the action that will be invoked on transition.
     *
     * @return The action that will be invoked on transition, or {@code null} if one is not preset.
     */
    public BiConsumer<C, T> getAction() {
        return action;
    }
}


