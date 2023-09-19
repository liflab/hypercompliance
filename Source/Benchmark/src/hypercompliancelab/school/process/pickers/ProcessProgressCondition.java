package hypercompliancelab.school.process.pickers;

import java.io.Serializable;


@Deprecated
public class ProcessProgressCondition implements Serializable {


    private static final long serialVersionUID = 1L;
    int maxStepReached;
    public ProcessProgressCondition(int i) {
        maxStepReached = i;
    }

    public boolean proceed(int transitionNumber) {
//        return transitionNumber <= maxStepReached;

        return true;

//        return new java.util.Random().nextBoolean();
    }

}
