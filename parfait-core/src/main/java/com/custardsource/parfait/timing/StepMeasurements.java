package com.custardsource.parfait.timing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class StepMeasurements {
    private final StepMeasurements parent;
    private final List<StepMeasurements> children = new ArrayList<StepMeasurements>();
    private final List<MetricMeasurement> metricInstances = new ArrayList<MetricMeasurement>();

    private final Class<?> eventClass;
    private final String action;

    public StepMeasurements(StepMeasurements parent,
            Class<?> eventClass, String action) {
        this.parent = parent;
        if (parent != null) {
            parent.addChildExecution(this);
        }
        this.eventClass = eventClass;
        this.action = action;
    }

    public StepMeasurements getParent() {
        return parent;
    }
    
    public void addMetricInstance(MetricMeasurement metric) {
        metricInstances.add(metric);
    }
    
    public void startAll() {
        for (MetricMeasurement metric : metricInstances) {
            metric.startTimer();
        }
    }

    public void stopAll() {
        for (MetricMeasurement metric : metricInstances) {
            metric.stopTimer();
        }
    }
    public void pauseAll() {
        for (MetricMeasurement metric : metricInstances) {
            metric.pauseOwnTime();
        }
    }
    public void resumeAll() {
        for (MetricMeasurement metric : metricInstances) {
            metric.resumeOwnTime();
        }
    }

    /**
     * @return a nicely-formatted list of all the events taken to reach the one under
     *         measurement (including that one as the last element)
     */
    String getBackTrace() {
        if (parent == null) {
            return stackTraceElement();
        }
        return parent.getBackTrace() + "/" + stackTraceElement();
    }

    /**
     * @return a nicely-formatted list of all the events invoked after the one under
     *         measurement (including that one as the first element)
     */
    String getForwardTrace() {
        if (children.isEmpty()) {
            return stackTraceElement();
        } else if (children.size() == 1) {
            return stackTraceElement() + "/" + children.get(0).getForwardTrace();
        } else {
            // Handles the 'freak case' where one event may forward directly to MORE than one
            // 'child'. I have no idea if this ever happens, but we might as well handle it.
            List<String> childTraces = new ArrayList<String>(children.size());
            for (StepMeasurements child : children) {
                childTraces.add(child.getForwardTrace());
            }
            return stackTraceElement() + "/{" + StringUtils.join(childTraces, '|') + "}";
        }
    }

    private void addChildExecution(StepMeasurements newTiming) {
        children.add(newTiming);
    }

    private String stackTraceElement() {
        return eventClass.getSimpleName() + (StringUtils.isEmpty(action) ? "" : ":" + action);
    }

    Class<?> getEventClass() {
        return eventClass;
    }

    public Collection<MetricMeasurement> getMetricInstances() {
        return metricInstances;
    }

}