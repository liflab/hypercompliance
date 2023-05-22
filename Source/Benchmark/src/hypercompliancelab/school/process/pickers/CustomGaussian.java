package hypercompliancelab.school.process.pickers;

import ca.uqac.lif.synthia.random.GaussianFloat;

public class CustomGaussian extends GaussianFloat {

    public int pick(double x, double y) {

        double mean = (x + y) / 2;
        double stddev = (y - x) / Math.sqrt(12);

        double result;
        do {
            result = mean + stddev * super.pick();
        } while (result < x || result > y);

        return (int) result;
    }

}
