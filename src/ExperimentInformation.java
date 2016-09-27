import java.util.ArrayList;
import java.util.List;

public class ExperimentInformation {
	private List<Long> resolveTimes;
	private double averageResolveTime;
	
	public ExperimentInformation(List<Long> resolveTimes, double averageResolveTime) {
		setResolveTimes(resolveTimes);
		setAverageResolveTime(averageResolveTime);
	}
	
	public ExperimentInformation() {
		resolveTimes = new ArrayList<Long>();
	}

	public List<Long> getResolveTimes() {
		return resolveTimes;
	}

	public void setResolveTimes(List<Long> resolveTimes) {
		this.resolveTimes = resolveTimes;
	}

	public double getAverageResolveTime() {
		return averageResolveTime;
	}

	public void setAverageResolveTime(double averageResolveTime) {
		this.averageResolveTime = averageResolveTime;
	}
}
